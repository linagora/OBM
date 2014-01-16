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
import java.io.Reader;

import org.minig.imap.impl.LineTerminationCorrecter;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.minig.imap.CommandIOException;
import org.obm.push.minig.imap.impl.IMAPResponse;

import com.google.common.base.Charsets;

public class AppendCommand extends Command<Boolean> {

	private final static String IMAP_COMMAND = "APPEND";
	private Reader in;
	private String mailbox;
	private FlagsList flags;

	public AppendCommand(String mailbox, Reader message, FlagsList flags) {
		this.mailbox = mailbox;
		this.in = message;
		this.flags = flags;
	}

	@Override
	public String getImapCommand() {
		return IMAP_COMMAND;
	}
	
	@Override
	protected CommandArgument buildCommand() {
		StringBuilder cmd = new StringBuilder(50);
		cmd.append(IMAP_COMMAND);
		cmd.append(" ");
		cmd.append(toUtf7(mailbox));
		cmd.append(" ");
		if (!flags.isEmpty()) {
			cmd.append(flags.asCommandValue());
			cmd.append(" ");
		}

		try {
			byte[] mailData = LineTerminationCorrecter.correctLineTermination(in).getBytes(Charsets.UTF_8);
			cmd.append("{");
			cmd.append(mailData.length);
			cmd.append("}");
			return new CommandArgument(cmd.toString(), mailData);
		} catch (IOException e) {
			String msg = "Cannot create tmp buffer for append command";
			logger.error(msg, e);
			throw new CommandIOException(msg, e);
		}
	}

	@Override
	public boolean isMatching(IMAPResponse response) {
		return true;
	}
	
	@Override
	public void handleResponse(IMAPResponse response) {
		data = response.isOk();
	}

	@Override
	public void setDataInitialValue() {
		data = false;
	}
}
