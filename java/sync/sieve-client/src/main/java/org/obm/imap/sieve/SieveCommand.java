/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2015  Linagora
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

package org.obm.imap.sieve;

import java.util.List;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

public abstract class SieveCommand<T> {

	protected static final Logger logger = LoggerFactory
			.getLogger(SieveCommand.class);
	
	protected T retVal;
	private static final byte[] CRLF = "\r\n".getBytes(Charsets.UTF_8);

	public void execute(IoSession session) {

		List<SieveArg> cmd = buildCommand();

		for (int i = 0; i < cmd.size(); i++) {
			SieveArg arg = cmd.get(i);
			if (!arg.isLiteral()) {
				StringBuilder sb = new StringBuilder(new String(arg.getRaw(), Charsets.UTF_8));
				if (i < cmd.size() - 1 && cmd.get(i + 1).isLiteral()) {
					SieveArg next = cmd.get(i + 1);
					sb.append(" {");
					sb.append(next.getRaw().length);
					sb.append("+}");
				}

				session.write(sb.toString().getBytes(Charsets.UTF_8));
			} else {
				session.write(arg.getRaw());
			}
			session.write(CRLF);
		}
	}

	public abstract void responseReceived(List<SieveResponse> rs);

	protected abstract List<SieveArg> buildCommand();

	protected boolean commandSucceeded(List<SieveResponse> rs) {
		return rs.size() > 0 && rs.get(0).getData().endsWith("OK");
	}

	protected void reportErrors(List<SieveResponse> rs) {
		for (SieveResponse sr : rs) {
			logger.error(sr.getData());
		}
	}

	public T getReceivedData() {
		return retVal;
	}

}
