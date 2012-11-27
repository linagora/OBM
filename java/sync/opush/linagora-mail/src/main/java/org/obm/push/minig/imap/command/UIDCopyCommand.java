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

package org.obm.push.minig.imap.command;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.obm.push.mail.bean.MessageSet;
import org.obm.push.minig.imap.impl.IMAPResponse;
import org.obm.push.minig.imap.impl.ImapMessageSet;

public class UIDCopyCommand extends Command<Collection<Long>> {

	private ImapMessageSet imapMessageSet;
	private String destMailbox;

	public UIDCopyCommand(Collection<Long> uid, String destMailbox) {
		MessageSet messageSet = MessageSet.builder().addAll(uid).build();
		imapMessageSet = ImapMessageSet.wrap(messageSet);
		this.destMailbox = destMailbox;
	}

	@Override
	protected CommandArgument buildCommand() {
		StringBuilder sb = new StringBuilder();
		sb.append("UID COPY ");
		sb.append(imapMessageSet.asString());
		sb.append(' ');
		sb.append(toUtf7(destMailbox));
		String cmd = sb.toString();
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		boolean isOK = isOk(rs);

		IMAPResponse ok = rs.get(rs.size() - 1);
		if (isOK && ok.getPayload().contains("[")) {
			logger.debug("ok: " + ok.getPayload());
			data = parseMessageSet(ok.getPayload());
		} else {
			if (isOK) {
				logger.warn("cyrus did not send [COPYUID ...] token: "
						+ ok.getPayload());
			} else {
				logger.error("error on uid copy: " + ok.getPayload());
			}
			data = Collections.emptyList();
		}
	}

	private Collection<Long> parseMessageSet(String payload) {
		int idx = payload.lastIndexOf("]");
		int space = payload.lastIndexOf(" ", idx);
		String set = payload.substring(space + 1, idx);
		logger.debug("set to parse: " + set);
		return ImapMessageSet.wrap(MessageSet.parseMessageSet(set)).asLongCollection();
	}

}
