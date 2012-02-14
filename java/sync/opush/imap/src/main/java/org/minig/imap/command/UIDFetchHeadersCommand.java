/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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

package org.minig.imap.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.minig.imap.IMAPHeaders;
import org.minig.imap.command.parser.HeadersParser;
import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.MessageSet;

public class UIDFetchHeadersCommand extends Command<Collection<IMAPHeaders>> {

	private Collection<Long> uids;
	private String[] headers;

	public UIDFetchHeadersCommand(Collection<Long> uid, String[] headers) {
		this.uids = uid;
		this.headers = headers;
	}

	@Override
	protected CommandArgument buildCommand() {
		StringBuilder sb = new StringBuilder();
		if (!uids.isEmpty()) {
			sb.append("UID FETCH ");
			sb.append(MessageSet.asString(uids));
			sb.append(" (UID BODY.PEEK[HEADER.FIELDS (");
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
	public void responseReceived(List<IMAPResponse> rs) {
		boolean isOK = isOk(rs);
		
		if (uids.isEmpty()) {
			data = Collections.emptyList();
			return;
		}
		
		if (isOK) {
			data = new ArrayList<IMAPHeaders>(uids.size());
			Iterator<IMAPResponse> it = rs.iterator();
			for (int i = 0; it.hasNext() && i < uids.size(); ) {
				IMAPResponse r = it.next();
				String payload = r.getPayload();
				if (!payload.contains(" FETCH")) {
					logger.warn("not a fetch: "+payload);
					continue;
				}
				int uidIdx = payload.indexOf("(UID ") + "(UID ".length();
				int endUid = payload.indexOf(' ', uidIdx);
				String uidStr = payload.substring(uidIdx, endUid);
				long uid = 0;
				try {
					uid = Long.parseLong(uidStr);
				} catch (NumberFormatException nfe) {
					logger.error("cannot parse uid for string '" + uid
							+ "' (payload: " + payload + ")");
					continue;
				}

				Map<String, String> rawHeaders = Collections.emptyMap();

				InputStream in = r.getStreamData();
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
					logger.warn("cyrus did not return any header for uid " + uid);
				}

				IMAPHeaders imapHeaders = new IMAPHeaders();
				imapHeaders.setUid(uid);
				imapHeaders.setRawHeaders(rawHeaders);
				data.add(imapHeaders);
				i++;
			}
		} else {
			IMAPResponse ok = rs.get(rs.size() - 1);
			logger.warn("error on fetch: " + ok.getPayload());
			data = Collections.emptyList();
		}
	}

}
