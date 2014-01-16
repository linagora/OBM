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

package org.obm.push.minig.imap.sieve;

import java.util.ArrayList;

import org.apache.mina.core.filterchain.IoFilter;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SieveClientHandler extends IoHandlerAdapter {

	private static final Logger logger = LoggerFactory
			.getLogger(SieveClientHandler.class);

	private SieveResponseParser srp = new SieveResponseParser();

	private SieveClientSupport cs;

	private IoFilter getSieveFilter() {
		ProtocolCodecFactory pcf = new SieveCodecFactory();
		return new ProtocolCodecFilter(pcf);
	}

	public SieveClientHandler(SieveClientSupport cb) {
		// TODO Auto-generated constructor stub
		this.cs = cb;
	}

	public void sessionCreated(IoSession session) throws Exception {
		session.getFilterChain().addLast("codec", getSieveFilter());
	}

	public void sessionOpened(IoSession session) throws Exception {
	}

	public void messageReceived(IoSession session, Object message)
			throws Exception {
		SieveMessage msg = (SieveMessage) message;
		ArrayList<SieveResponse> copy = new ArrayList<SieveResponse>(10);
		srp.parse(copy, msg);
		cs.setResponses(copy);
	}

	public void sessionClosed(IoSession session) throws Exception {
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
			throws Exception {
		logger.error(cause.getMessage(), cause);
		throw new SieveException(cause.getMessage(), cause);
	}

}
