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

import java.util.Map;

import org.obm.push.exception.MailException;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.minig.imap.impl.IMAPParsingTools;
import org.obm.push.minig.imap.impl.IMAPResponse;
import org.obm.push.minig.imap.impl.ImapMessageSet;

import com.google.common.collect.Maps;

public class UIDFetchFlagsCommand extends Command<Map<Long, FlagsList>> {

	private final static String IMAP_COMMAND = "UID FETCH";
	private final static String IMAP_SUB_COMMAND = "UID FLAGS";
	private final ImapMessageSet imapMessageSet;

	public UIDFetchFlagsCommand(MessageSet messages) {
		this.imapMessageSet = ImapMessageSet.wrap(messages);
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
			sb.append(")");
		} else {
			sb.append("NOOP");
		}
		String cmd = sb.toString();
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public String getImapCommand() {
		return IMAP_COMMAND + " " + IMAP_SUB_COMMAND;
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		String payload = response.getPayload();
		if (payload == null) {
			return false;
		}
		
		int fidx = payload.indexOf("FLAGS (") + "FLAGS (".length();
		if (fidx == -1 + "FLAGS (".length()) {
			return false;
		}
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		String payload = response.getPayload();
		long uid = getUid(payload);
		
		if (imapMessageSet.contains(uid)) {
			FlagsList flags = parseResponse(payload);
			if (flags != null) {
				data.put(uid, flags);
			} else {
				logger.error("Failed to get flags in fetch response: {}", payload);
			}
		}
	}

	public static FlagsList parseResponse(String payload) {
		int fidx = payload.indexOf("FLAGS (") + "FLAGS (".length();
		
		int endFlags = payload.indexOf(")", fidx);
		String flags = "";
		if (fidx > 0 && endFlags >= fidx) {
			flags = payload.substring(fidx, endFlags);
		} else {
			return null;
		}
		FlagsList flagsList = new FlagsList();
		parseFlags(flags, flagsList);
		return flagsList;
	}

	private long getUid(String fullPayload) {
		try {
			String longAsString = IMAPParsingTools.getStringHasNumberForField(fullPayload, "UID ");
			return Long.valueOf(longAsString);
		} catch(NumberFormatException e) {
			throw new MailException("Cannot find UID in response : " + fullPayload);
		}
	}

	private static void parseFlags(String flags, FlagsList flagsList) {
		// TODO this is probably slow as hell
		if (flags.contains("\\Seen")) {
			flagsList.add(Flag.SEEN);
		}
		if (flags.contains("\\Flagged")) {
			flagsList.add(Flag.FLAGGED);
		}
		if (flags.contains("\\Deleted")) {
			flagsList.add(Flag.DELETED);
		}
		if (flags.contains("\\Answered")) {
			flagsList.add(Flag.ANSWERED);
		}
		if (flags.contains("\\Draft")) {
			flagsList.add(Flag.DRAFT);
		}
	}

	@Override
	public void setDataInitialValue() {
		data = Maps.newHashMap();
	}
}
