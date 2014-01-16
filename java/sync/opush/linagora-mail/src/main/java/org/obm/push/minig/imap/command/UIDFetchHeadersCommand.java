/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */

package org.obm.push.minig.imap.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.minig.imap.command.parser.HeadersParser;
import org.obm.push.mail.bean.IMAPHeaders;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.minig.imap.impl.IMAPResponse;
import org.obm.push.minig.imap.impl.ImapMessageSet;

import com.google.common.collect.ImmutableList;

public class UIDFetchHeadersCommand extends Command<Collection<IMAPHeaders>> {

	private final static String IMAP_COMMAND = "UID FETCH";
	private final static String IMAP_SUB_COMMAND = "UID BODY.PEEK[HEADER.FIELDS";
	private ImapMessageSet imapMessageSet;
	private String[] headers;

	public UIDFetchHeadersCommand(Collection<Long> uid, String[] headers) {
		MessageSet messageSet = MessageSet.builder().addAll(uid).build();
		imapMessageSet = ImapMessageSet.wrap(messageSet);
		this.headers = headers;
	}

	@Override
	protected CommandArgument buildCommand() {
		StringBuilder sb = new StringBuilder();
		if (!imapMessageSet.isEmpty()) {
			sb.append(IMAP_COMMAND);
			sb.append(" ");
			sb.append(imapMessageSet.asString());
			sb.append(" (");
			sb.append(IMAP_SUB_COMMAND);
			sb.append(" (");
			for (int i = 0; i < headers.length; i++) {
				if (i > 0) {
					sb.append(" ");
				}
				sb.append(headers[i].toUpperCase());
			}
			sb.append(")])");
		} else {
			sb.append("NOOP");
		}
		String cmd = sb.toString();
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public String getImapCommand() {
		return IMAP_COMMAND + " " +IMAP_SUB_COMMAND;
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		String payload = response.getPayload();
		if (!payload.contains("[HEADER.FIELDS")) {
			logger.warn("not a fetch: {}", payload);
			return false;
		}
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		String payload = response.getPayload();
		int uidIdx = payload.indexOf("(UID ") + "(UID ".length();
		int endUid = payload.indexOf(' ', uidIdx);
		String uidStr = payload.substring(uidIdx, endUid);
		long uid = 0;
		try {
			uid = Long.parseLong(uidStr);
		} catch (NumberFormatException nfe) {
			logger.error("cannot parse uid for string '{}' (payload: {})", uid, payload);
			return;
		}

		Map<String, String> rawHeaders = Collections.emptyMap();

		InputStream in = response.getStreamData();
		if (in != null) {
			try {
				InputStreamReader reader = new InputStreamReader(in);
				rawHeaders = new HeadersParser().parseRawHeaders(reader);
			} catch (IOException e) {
				logger.error("Error reading headers stream", e);
			} catch (Throwable t) {
				logger.error("error parsing headers stream", t);
			}
		} else {
			// cyrus search command can return uid's that no longer exist in the mailbox
			logger.warn("cyrus did not return any header for uid {}", uid);
		}

		IMAPHeaders imapHeaders = new IMAPHeaders();
		imapHeaders.setRawHeaders(rawHeaders);
		
		if (data == null || data.isEmpty()) {
			data = new ArrayList<IMAPHeaders>(imapMessageSet.size());
		}
		data.add(imapHeaders);
	}

	@Override
	public void setDataInitialValue() {
		data = ImmutableList.of();
	}
}
