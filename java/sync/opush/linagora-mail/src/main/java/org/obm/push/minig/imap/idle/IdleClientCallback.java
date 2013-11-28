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

package org.obm.push.minig.imap.idle;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.obm.push.mail.IMAPException;
import org.obm.push.mail.imap.idle.IIdleCallback;
import org.obm.push.mail.imap.idle.IdleLine;
import org.obm.push.minig.imap.impl.ClientSupport;
import org.obm.push.minig.imap.impl.IMAPResponse;
import org.obm.push.minig.imap.impl.IMAPResponseParser;
import org.obm.push.minig.imap.impl.IResponseCallback;
import org.obm.push.minig.imap.impl.MinaIMAPMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdleClientCallback implements IResponseCallback {

	private static final Logger logger = LoggerFactory
			.getLogger(IdleClientCallback.class);
	
	private final List<IMAPResponse> responses;
	private IIdleCallback observer;
	private final IdleResponseParser rParser;
	private final IMAPResponseParser imrParser;
	private ClientSupport cs;
	private Boolean isStart;

	public IdleClientCallback() {
		this.rParser = new IdleResponseParser();
		this.imrParser = new IMAPResponseParser();
		isStart = false;
		this.responses = new LinkedList<IMAPResponse>();
	}

	@Override
	public void connected() {
		logger.debug("connected() callback called.");
		imrParser.setServerHelloReceived(false);
	}

	@Override
	public void disconnected() {
		logger.debug("disconnected() callback called.");
		if (observer != null) {
			observer.disconnectedCallBack();
		}
		this.isStart = false;
	}

	@Override
	public void imapResponse(MinaIMAPMessage imapResponse) {
		if (isStart) {
			if (observer != null) {
				IdleLine rp = null;
				try {
					rp = rParser.parse(imapResponse);
				} catch (RuntimeException re) {
					logger.warn("Runtime exception on: " + imapResponse);
					throw re;
				}
				observer.receive(rp);
			}
		} else {
			IMAPResponse rp = null;
			try {

				rp = imrParser.parse(imapResponse);
			} catch (RuntimeException re) {
				logger.warn("Runtime exception on: " + imapResponse);
				throw re;
			}
			responses.add(rp);
			if (rp.isClientDataExpected()) {
				ArrayList<IMAPResponse> rs = new ArrayList<IMAPResponse>(
						responses.size());
				rs.addAll(responses);
				responses.clear();
				cs.setResponses(rs);
			}
		}
		if (imapResponse.getMessageLine().toLowerCase().startsWith("+ idling")) {
			isStart = true;
		}
	}

	public void attachIdleCallback(IIdleCallback observer) {
		this.observer = observer;
	}

	public void setClient(ClientSupport cs) {
		this.cs = cs;
	}

	public boolean isStart() {
		return isStart;
	}

	public void stopIdle() {
		this.isStart = false;
	}

	@Override
	public void exceptionCaught(IMAPException cause) throws IMAPException {
		logger.error(cause.getMessage(), cause);
	}

	public void detachIdleCallback() {
		this.observer = null;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

}
