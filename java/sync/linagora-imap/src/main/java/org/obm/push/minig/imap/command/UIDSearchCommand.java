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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.minig.imap.impl.IMAPResponse;
import org.obm.push.minig.imap.impl.ImapMessageSet;

import com.google.common.base.Splitter;

public class UIDSearchCommand extends Command<MessageSet> {

	// Mon, 7 Feb 1994 21:52:25 -0800

	private final static String IMAP_COMMAND = "UID SEARCH";
	private final SearchQuery sq;
	private DateFormat dateFormat;

	public UIDSearchCommand(SearchQuery sq) {
		this.sq = sq;
		this.dateFormat = new SimpleDateFormat("d-MMM-yyyy", Locale.ENGLISH);
	}

	@Override
	protected CommandArgument buildCommand() {
		String cmd = IMAP_COMMAND;
		if (!sq.isMatchDeleted()) {
			cmd += " NOT DELETED";
		}
		if (sq.isBetween()) {
			cmd += " OR";
			cmd += " BEFORE " + dateFormat.format(sq.getBefore());
			cmd += " SINCE " + dateFormat.format(sq.getAfter());
			
		} else {
			if (sq.getAfter() != null) {
				cmd += " NOT BEFORE " + dateFormat.format(sq.getAfter());
			}
			if (sq.getBefore() != null) {
				cmd += " BEFORE " + dateFormat.format(sq.getBefore());
			}
		}
		if (sq.getMessageSet() != null) {
			cmd += " UID " + ImapMessageSet.wrap(sq.getMessageSet()).asString(); 
		}
		if (sq.getMatchingFlag().isPresent()) {
			cmd += " KEYWORD " + sq.getMatchingFlag().get().asCommandValue();
		}
		if (sq.getUnmatchingFlag().isPresent()) {
			cmd += " UNKEYWORD " + sq.getUnmatchingFlag().get().asCommandValue();
		}
		
		// logger.info("cmd "+cmd);
		CommandArgument args = new CommandArgument(cmd, null);
		return args;
	}


	@Override
	public String getImapCommand() {
		return IMAP_COMMAND;
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		if (!response.getPayload().startsWith("* SEARCH ")) {
			return false;
		}
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		String uidString = response.getPayload();
		
		// 9 => '* SEARCH '.length
		String uidList = uidString.substring(9);
		Iterable<String> uids = Splitter.on(' ').omitEmptyStrings().split(uidList);
		MessageSet.Builder builder = MessageSet.builder();
		for (String uid : uids) {
			builder.add(Long.valueOf(uid));
		}
		
		if (data == null) {
			data = builder.build();
		} else {
			data = builder.add(data).build();
		}
	}
	
	@Override
	public void setDataInitialValue() {
		data = MessageSet.empty();
	}
}
