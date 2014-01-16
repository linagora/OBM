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
package org.obm.sync.push.client.commands;

import java.util.concurrent.Future;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Async;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.message.BasicHeader;
import org.obm.sync.push.client.IEasCommand;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.OptionsResponse;
import org.obm.sync.push.client.beans.AccountInfos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class Options implements IEasCommand<OptionsResponse> {

	private static final Logger logger = LoggerFactory.getLogger(Options.class);
	
	@Override
	public OptionsResponse run(AccountInfos ai, OPClient opc, HttpClient hc) throws Exception {
		HttpOptions request = new HttpOptions(opc.buildUrl(ai.getUrl(), ai.getLogin(), ai.getDevId(), ai.getDevType()));
		request.setHeaders(new Header[] { 
				new BasicHeader("User-Agent", ai.getUserAgent()),
				new BasicHeader("Authorization", ai.authValue())
			});
		synchronized (hc) {
			try {
				HttpResponse response = hc.execute(request);
				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode != HttpStatus.SC_OK) {
					logger.error("method failed:{}\n{}\n",  statusLine, response.getEntity());
				}
				Header[] hs = response.getAllHeaders();
				for (Header h : hs) {
					logger.info("resp head[" + h.getName() + "] => "+ h.getValue());
				}
				return new OptionsResponse(ImmutableSet.copyOf(hs));
			} finally {
				request.releaseConnection();
			}
		}
	}

	@Override
	public Future<OptionsResponse> runASync(AccountInfos ai, OPClient opc, Async async) throws Exception {
		throw new RuntimeException("Not implements for Options");
	}
}
