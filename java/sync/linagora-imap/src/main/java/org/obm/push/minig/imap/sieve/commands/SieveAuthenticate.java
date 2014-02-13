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

package org.obm.push.minig.imap.sieve.commands;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.obm.push.minig.imap.sieve.SieveArg;
import org.obm.push.minig.imap.sieve.SieveCommand;
import org.obm.push.minig.imap.sieve.SieveResponse;

import com.google.common.base.Charsets;

public class SieveAuthenticate extends SieveCommand<Boolean> {

	private final String login;
	private final String password;
	private final byte[] encoded;

	public SieveAuthenticate(String login, String password) {
		this.login = login;
		this.password = password;
		this.encoded = encodeAuthString(login, password);
		this.retVal = Boolean.FALSE;
	}

	@Override
	public void responseReceived(List<SieveResponse> rs) {
		if (commandSucceeded(rs)) {
			retVal = true;
		}
		if (logger.isDebugEnabled()) {
			logger.debug("response received for login " + login + " " + password
					+ " => " + retVal);
		}
	}

	@Override
	protected List<SieveArg> buildCommand() {
		List<SieveArg> ret = new ArrayList<SieveArg>(2);

		ret.add(new SieveArg("AUTHENTICATE \"PLAIN\"".getBytes(Charsets.UTF_8), false));
		ret.add(new SieveArg(encoded, true));

		return ret;
	}

	private byte[] encodeAuthString(String login, String password) {
		byte[] log = login.getBytes(Charsets.UTF_8);
		byte[] pass = password.getBytes();
		byte[] data = new byte[log.length * 2 + pass.length + 2];
		int i = 0;
		for (int j = 0; j < log.length; j++) {
			data[i++] = log[j];
		}
		data[i++] = 0x0;
		for (int j = 0; j < log.length; j++) {
			data[i++] = log[j];
		}
		data[i++] = 0x0;

		for (int j = 0; j < pass.length; j++) {
			data[i++] = pass[j];
		}

		ByteBuffer encoded = ByteBuffer.allocate(data.length);
		encoded.put(data);
		encoded.flip();

		String ret = Base64.encodeBase64String(encoded.array());
		if (logger.isDebugEnabled()) {
			logger.info("encoded auth string: " + ret);
		}
		return ret.getBytes();
	}

}
