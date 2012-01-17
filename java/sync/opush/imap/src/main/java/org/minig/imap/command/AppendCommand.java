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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.minig.imap.FlagsList;
import org.minig.imap.impl.IMAPResponse;
import org.obm.push.utils.FileUtils;

public class AppendCommand extends Command<Boolean> {

	private InputStream in;
	private String mailbox;
	private FlagsList flags;

	public AppendCommand(String mailbox, InputStream message, FlagsList flags) {
		this.mailbox = mailbox;
		this.in = message;
		this.flags = flags;
	}

	@Override
	protected CommandArgument buildCommand() {
		StringBuilder cmd = new StringBuilder(50);
		cmd.append("APPEND ");
		cmd.append(toUtf7(mailbox));
		cmd.append(" ");
		if (!flags.isEmpty()) {
			cmd.append(flags.toString());
			cmd.append(" ");
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			FileUtils.transfer(in, out, true);
		} catch (IOException e) {
			logger.error("Cannot create tmp buffer for append command", e);
		}

		cmd.append("{");
		cmd.append(out.toByteArray().length);
		cmd.append("}");
		return new CommandArgument(cmd.toString(), out.toByteArray());
	}

	@Override
	public void responseReceived(List<IMAPResponse> rs) {
		IMAPResponse r = rs.get(rs.size() - 1);
		data = r.isOk();
	}

}
