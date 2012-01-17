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

package org.minig.imap.impl;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IMAPResponseParser {

	private final static Logger logger = LoggerFactory
			.getLogger(IMAPResponseParser.class);

	private boolean serverHelloReceived;

	public IMAPResponseParser() {
		serverHelloReceived = false;
	}

	public IMAPResponse parse(MinaIMAPMessage msg) {
		String response = msg.getMessageLine();
		IMAPResponse r = new IMAPResponse();
		int idx = response.indexOf(' ');
		if (idx == -1 && response.length() == 1) {
			idx = 1;
		}
		if (idx < 0)  {
			logger.warn("response without space (forcing bad status): "+response);
			r.setStatus("BAD");
			r.setPayload(response);
			return r;
		}
		
		String tag = response.substring(0, idx);
		r.setTag(tag);
		int statusIdx = response.indexOf(' ', idx + 1);
		if (statusIdx < 0) {
			statusIdx = response.length();
		}
		if (idx + 1 < statusIdx) {
			String status = response.substring(idx + 1, statusIdx);
			if (logger.isDebugEnabled()) {
				logger.debug("TAG: " + tag + " STATUS: " + status);
			}
			r.setStatus(status);
		}
		

		boolean clientDataExpected = false;
		if ("+".equals(tag) || !"*".equals(tag)) {
			clientDataExpected = true;
		}

		if (!serverHelloReceived) {
			clientDataExpected = true;
			serverHelloReceived = true;
		}
		r.setClientDataExpected(clientDataExpected);

		r.setPayload(response);

		if (msg.hasFragments()) {
			List<byte[]> all = msg.getFragments();
			int len = 0;
			for (byte[] b : all) {
				len += b.length;
			}
			byte[] data = new byte[len];
			int copyIdx = 0;
			for (int i = 0; i < all.size(); i++) {
				// remove closing paren on last response...
				byte[] b = all.get(i);
				int copySize = b.length;
				if (b.length > 0 && i == (all.size() - 1) && b[b.length - 1] == ')') {
					copySize = copySize - 1;
				}
				System.arraycopy(b, 0, data, copyIdx, copySize);
				copyIdx += b.length;
			}
			r.setStreamData(new ByteArrayInputStream(data));
		}

		return r;
	}

	public void setServerHelloReceived(boolean serverHelloReceived) {
		this.serverHelloReceived = serverHelloReceived;
	}

}
