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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.minig.imap.impl.IMAPResponse;
import org.minig.imap.impl.MailThread;

public class UIDThreadCommand extends Command<List<MailThread>> {

	@Override
	protected CommandArgument buildCommand() {
		String cmd = "UID THREAD REFERENCES UTF-8 NOT DELETED";
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		boolean isOK = isOk(rs);
		data = new LinkedList<MailThread>();
		if (isOK) {
			String threads = null;
			Iterator<IMAPResponse> it = rs.iterator();
			for (int j = 0; j < rs.size() - 1; j++) {
				String resp = it.next().getPayload();
				if (resp.startsWith("* THREAD ")) {
					threads = resp;
					break;
				}
			}

			if (threads != null) {
				parseParenList(data, threads.substring("* THREAD ".length()));
				logger.debug("extracted " + data.size() + " threads");
			}
		}
	}

	private void parseParenList(List<MailThread> data, String substring) {
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

}
