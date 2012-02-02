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
package org.obm.push.handler;

import java.io.IOException;

import org.eclipse.jetty.http.HttpStatus;
import org.obm.push.backend.IContinuation;
import org.obm.push.backend.IErrorsManager;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.QuotaExceededException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.Responder;
import org.obm.push.mail.MailBackend;
import org.obm.push.protocol.MailProtocol;
import org.obm.push.protocol.bean.MailRequest;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MailRequestHandler implements IRequestHandler {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	protected final MailBackend mailBackend;
	private final IErrorsManager errorManager;
	protected final MailProtocol mailProtocol;

	protected abstract void doTheJob(MailRequest mailRequest, BackendSession bs) 
			throws ProcessingEmailException, CollectionNotFoundException, ItemNotFoundException;
	
	protected MailRequestHandler(MailBackend mailBackend, IErrorsManager errorManager, MailProtocol mailProtocol) {
		this.mailBackend = mailBackend;
		this.errorManager = errorManager;
		this.mailProtocol = mailProtocol;
	}

	@Override
	public void process(IContinuation continuation, BackendSession bs, ActiveSyncRequest request, Responder responder) {
		MailRequest mailRequest = null;
		try {
			mailRequest = mailProtocol.getRequest(request);
			if (logger.isDebugEnabled()) {
				logger.debug("Mail content:\n" + new String(mailRequest.getMailContent()));
			}
			doTheJob(mailRequest, bs);

		} catch (ProcessingEmailException pe) {	
			notifyUser(bs,  mailRequest.getMailContent(), pe);
		} catch (IOException e) {
			responder.sendError(HttpStatus.BAD_REQUEST_400);
			return;
		} catch (CollectionNotFoundException e) {
			notifyUser(bs, mailRequest.getMailContent(), e);
		} catch (QuotaExceededException e) {
			notifyUserQuotaExceeded(bs, e);
		} catch (ItemNotFoundException e) {
			notifyUser(bs, mailRequest.getMailContent(), e);
		}
	}

	private void notifyUserQuotaExceeded(BackendSession bs,
			QuotaExceededException e) {
		errorManager.sendQuotaExceededError(bs, e);
	}

	private void notifyUser(BackendSession bs, byte[] mailContent, Throwable t) {
		logger.error("Error while sending mail. A mail with the error will be sent at the sender.", t);
		errorManager.sendMailHandlerError(bs, mailContent, t);
	}

}
