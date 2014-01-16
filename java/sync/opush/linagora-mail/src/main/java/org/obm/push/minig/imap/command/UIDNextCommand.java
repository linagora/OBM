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

import org.obm.push.minig.imap.impl.IMAPParsingTools;
import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.base.Strings;

public class UIDNextCommand extends Command<Long> {

	private final static String IMAP_COMMAND = "UIDNEXT";
	private final static String STATUS_COMMAND = "STATUS";
	
	private String mailbox;

	public UIDNextCommand(String mailbox) {
		this.mailbox = mailbox;
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = STATUS_COMMAND + " " 
				+ toUtf7(mailbox) 
				+  " (" + IMAP_COMMAND + ")";
		return new CommandArgument(cmd, null);
	}

	@Override
	public String getImapCommand() {
		return IMAP_COMMAND;
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		String fullPayload = response.getFullResponse();
		
		if (!fullPayload.contains(IMAP_COMMAND)) {
			return false;
		}
		
		String uidNextHasString = getUIDNextHasString(fullPayload);
		if (Strings.isNullOrEmpty(uidNextHasString)) {
			return false;
		}
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		String fullPayload = response.getFullResponse();
		String uidNextHasString = getUIDNextHasString(fullPayload);
		
		data = Long.valueOf(uidNextHasString);
	}

	private String getUIDNextHasString(String fullPayload) {
		return IMAPParsingTools.getStringHasNumberForField(fullPayload, IMAP_COMMAND + " ");
	}
}
