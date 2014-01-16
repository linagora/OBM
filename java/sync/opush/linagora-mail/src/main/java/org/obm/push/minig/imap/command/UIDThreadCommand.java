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

import java.util.LinkedList;
import java.util.List;

import org.obm.push.minig.imap.impl.IMAPResponse;
import org.obm.push.minig.imap.impl.MailThread;

import com.google.common.collect.ImmutableList;

public class UIDThreadCommand extends Command<List<MailThread>> {

	private final static String IMAP_COMMAND = "UID THREAD REFERENCES UTF-8 NOT DELETED";
	
	@Override
	protected CommandArgument buildCommand() {
		CommandArgument args = new CommandArgument(IMAP_COMMAND, null);
		return args;
	}

	@Override
	public String getImapCommand() {
		return IMAP_COMMAND;
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		if (!response.getPayload().startsWith("* THREAD ")) {
			return false;
		}
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		parseParenList(response.getPayload().substring("* THREAD ".length()));
		logger.debug("extracted {} threads", data.size());
	}

	private void parseParenList(String substring) {
		int parentCnt = 0;
		MailThread m = new MailThread();
		StringBuilder sb = new StringBuilder();
		for (char c : substring.toCharArray()) {
			if (c == '(') {
				if (parentCnt == 0) {
					m = new MailThread();
				}
				parentCnt++;
			} else if (c == ')') {
				parentCnt--;
				if (sb.length() > 0) {
					sb = addUid(m, sb);
				}
				if (m.size() > 0 && parentCnt == 0) {
					if (data == null || data.isEmpty()) {
						data = new LinkedList<MailThread>();
					}
					data.add(m);
				}
			} else if (Character.isDigit(c)) {
				sb.append(c);
			} else if (sb.length() > 0) {
				sb = addUid(m, sb);
			}
		}
	}

	private StringBuilder addUid(MailThread m, StringBuilder sb) {
		long l = Long.parseLong(sb.toString());
		m.add(l);
		sb = new StringBuilder();
		return sb;
	}

	@Override
	public void setDataInitialValue() {
		data = ImmutableList.of();
	}
}
