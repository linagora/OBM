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
package org.obm.sync.push.client.commands;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.obm.sync.push.client.IEasCommand;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.beans.AccountInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Options implements IEasCommand<Void> {

	private static final Logger logger = LoggerFactory.getLogger(Options.class);
	
	@Override
	public Void run(AccountInfos ai, OPClient opc, HttpClient hc) throws Exception {
		OptionsMethod pm = new OptionsMethod(ai.getUrl() + "?User=" + ai.getLogin()
				+ "&DeviceId=" + ai.getDevId() + "&DeviceType=" + ai.getDevType());
		pm.setRequestHeader("User-Agent", ai.getUserAgent());
		pm.setRequestHeader("Authorization", ai.authValue());
		synchronized (hc) {
			try {
				int ret = hc.executeMethod(pm);
				if (ret != HttpStatus.SC_OK) {
					logger.error("method failed:\n" + pm.getStatusLine()
							+ "\n" + pm.getResponseBodyAsString());
				}
				Header[] hs = pm.getResponseHeaders();
				for (Header h : hs) {
					logger.error("resp head[" + h.getName() + "] => "
							+ h.getValue());

				}
			} finally {
				pm.releaseConnection();
			}
		}
		return null;
	}

}
