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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

/**
 * Client API to cyrus sieve server
 * 
 * <code>http://www.ietf.org/proceedings/06mar/slides/ilemonade-1.pdf</code>
 */
public class SieveClient {

	private static final Logger logger = LoggerFactory
			.getLogger(SieveClient.class);
	
	private final SieveClientSupport cs;
	private final String host;
	private final int port;
	
	public SieveClient(String hostname, int port, AuthenticationIdentity authIdentity,
			AuthorizationIdentity autzIdentity) {
		this.host = hostname;
		this.port = port;

		cs = new SieveClientSupport(authIdentity, autzIdentity);
	}

	public boolean login() {
		if (logger.isDebugEnabled()) {
			logger.debug("login called");
		}
		SieveClientHandler handler = new SieveClientHandler(cs);
		SocketAddress sa = new InetSocketAddress(host, port);
		if (cs.login(sa, handler)) {
			return true;
		}
		return false;
	}

	public List<SieveScript> listscripts() {
		return cs.listscripts();
	}

	public boolean putscript(String name, String scriptContent) {
		return cs.putscript(name, new ByteArrayInputStream(scriptContent.getBytes(Charsets.UTF_8)));
	}

	public boolean putscript(String name, InputStream scriptContent) {
		return cs.putscript(name, scriptContent);
	}

	public void unauthenticate() {
		cs.unauthenticate();
	}
	
	public void logout() throws InterruptedException {
		cs.logout();
	}

	public boolean deletescript(String name) {
		return cs.deletescript(name);
	}

	public String getScript() {
		return "require [ \"fileinto\", \"imapflags\", \"vacation\" ];\n";
	}

	public String getScriptContent(String name) {
		return cs.getScriptContent(name);
	}

	public void activate(String newName) {
		cs.activate(newName);
	}

}
