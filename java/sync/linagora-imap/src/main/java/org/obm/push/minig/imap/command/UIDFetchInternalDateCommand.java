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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.obm.push.mail.bean.InternalDate;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.minig.imap.impl.IMAPResponse;
import org.obm.push.minig.imap.impl.ImapMessageSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class UIDFetchInternalDateCommand extends Command<List<InternalDate>> {

	private final static String IMAP_COMMAND = "UID FETCH";
	private final static String IMAP_SUB_COMMAND = "UID INTERNALDATE";
	private final ImapMessageSet imapMessageSet;
	DateFormat df;

	public UIDFetchInternalDateCommand(MessageSet messages) {
		imapMessageSet = ImapMessageSet.wrap(messages);
		//22-Mar-2010 14:26:18 +0100
		df = new SimpleDateFormat("d-MMM-yyyy HH:mm:ss Z", Locale.ENGLISH);
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
		return IMAP_COMMAND + " " +IMAP_SUB_COMMAND;
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		String payload = response.getPayload();
		int fidx = payload.indexOf("INTERNALDATE \"") + "INTERNALDATE \"".length();
		if (fidx == -1 + "INTERNALDATE \"".length()) {
			return false;
		}
		return true;
	}

	@Override
	public void handleResponse(IMAPResponse response) {
		if (imapMessageSet.isEmpty()) {
			data = ImmutableList.of();
			return;
		}
		
		String payload = response.getPayload();
		int fidx = payload.indexOf("INTERNALDATE \"") + "INTERNALDATE \"".length();
		int endDate = payload.indexOf("\"", fidx);
		String internalDate = "";
		if (fidx > 0 && endDate >= fidx) {
			internalDate = payload.substring(fidx, endDate);
		} else {
			logger.error("Failed to get flags in fetch response: {}", payload);
		}

		int uidIdx = payload.indexOf("UID ") + "UID ".length();
		int endUid = uidIdx;
		while (Character.isDigit(payload.charAt(endUid))) {
			endUid++;
		}
		long uid = Long.parseLong(payload.substring(uidIdx, endUid));

		// logger.info("payload: " + r.getPayload()+" uid: "+uid);

		if (data == null || data.isEmpty()) {
			data = Lists.newArrayList();
		}
		data.add(new InternalDate(uid,parseDate(internalDate)));
	}

	private Date parseDate(String date) {
		try {
			return df .parse(date);
		} catch (ParseException e) {
			logger.error("Can't parse date {}", date);
		}
		return new Date();
	}
	
	@Override
	public void setDataInitialValue() {
		data = ImmutableList.of();
	}
}
