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

import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.annotations.VisibleForTesting;

public class AbstractListCommand extends SimpleCommand<ListResult> {

	private static final int CHAR_SEPARATOR_BLOCK_SIZE = ") \".\" ".length();
	private static final String DOUBLE_QUOTE = "\"";
	private static final String EMPTY_REFERENCE_NAME = DOUBLE_QUOTE + DOUBLE_QUOTE;
	protected boolean subscribedOnly;

	protected AbstractListCommand(boolean subscribedOnly, String referenceName) {
		super((subscribedOnly ? "LSUB " : "LIST ") + referenceName(referenceName) + " \"*\"");
		this.subscribedOnly = subscribedOnly;
	}
	
	private static String referenceName(String referenceName) {
		if (referenceName != null) {
			return String.format("%s%s%s", DOUBLE_QUOTE, referenceName, DOUBLE_QUOTE);
		}
		return EMPTY_REFERENCE_NAME;
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		String p = response.getPayload();
		if (!p.contains( subscribedOnly? "* LSUB " : "* LIST ")) {
			return false;
		}
		return true;
	}
	
	@Override
	public void handleResponse(IMAPResponse response) {
		String p = response.getPayload();
		int oParen = p.indexOf('(', 5);
		int cPren = p.indexOf(')', oParen);
		String flags = p.substring(oParen + 1, cPren);
		
		String mbox = parseMailboxName(p, cPren);
		ListInfo listInfo = new ListInfo(mbox, !flags.contains("\\Noselect"), !flags.contains("\\Noinferiors"));
			
		if (null == data || data.isEmpty()) {
			ListResult lr = new ListResult(1);
			char imapSep = p.charAt(cPren + 3);
			lr.setImapSeparator(imapSep);
			
			lr.add(listInfo);
			data = lr;
		} else {
			data.add(listInfo);
		}
	}

	@VisibleForTesting String parseMailboxName(String responseLine, int flagsEndingIndex) {
		String mailboxName = fromUtf7(responseLine.trim().substring(flagsEndingIndex + CHAR_SEPARATOR_BLOCK_SIZE));
		return unquote(mailboxName);
	}

	private String unquote(String mailboxName) {
		if (mailboxName.charAt(0) == '\"' && 
				mailboxName.charAt(mailboxName.length() - 1) == '\"') {
			return mailboxName.substring(1, mailboxName.length() - 1);
		} else {
			return mailboxName;
		}
	}

	@Override
	public void setDataInitialValue() {
		data = new ListResult(0);
	}
}
