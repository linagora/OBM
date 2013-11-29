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
package org.obm.push.handler;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.jetty.http.HttpStatus;
import org.obm.push.backend.ErrorsManager;
import org.obm.push.backend.IContinuation;
import org.obm.push.bean.UserDataRequest;
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

import com.google.common.base.Charsets;

public abstract class MailRequestHandler implements IRequestHandler {

	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	protected final MailBackend mailBackend;
	private final ErrorsManager errorManager;
	protected final MailProtocol mailProtocol;

	private final Logger mailDataLogger;
	private final Logger trimmedRequestLogger;
	private final Logger fullRequestLogger;

	protected abstract void doTheJob(MailRequest mailRequest, UserDataRequest udr) 
			throws ProcessingEmailException, CollectionNotFoundException, ItemNotFoundException;
	
	protected MailRequestHandler(MailBackend mailBackend, ErrorsManager errorManager, MailProtocol mailProtocol, Logger mailDataLogger,
			Logger fullRequestLogger, Logger trimmedRequestLogger) {

		this.mailBackend = mailBackend;
		this.errorManager = errorManager;
		this.mailProtocol = mailProtocol;
		this.mailDataLogger = mailDataLogger;
		this.fullRequestLogger = fullRequestLogger;
		this.trimmedRequestLogger = trimmedRequestLogger;
	}

	@Override
	public void process(IContinuation continuation, UserDataRequest udr, ActiveSyncRequest request, Responder responder) {
		try {
			MailRequest mailRequest = mailProtocol.getRequest(request);
			logRequest(mailRequest);
			process(udr, mailRequest);
		} catch (IOException e) {
			responder.sendError(HttpStatus.BAD_REQUEST_400);
			return;
		} catch (QuotaExceededException e) {
			notifyUserQuotaExceeded(udr, e);
		}
	}

	private void logRequest(MailRequest mailRequest) {
		for (Logger logger: Arrays.asList(trimmedRequestLogger, fullRequestLogger)) {
			logger.debug("MailRequest : collection='{}', serverId='{}', saveInSent='{}'", 
					mailRequest.getCollectionId(), mailRequest.getServerId(), mailRequest.isSaveInSent());
		}
	}

	private void process(UserDataRequest udr, MailRequest mailRequest) {
		try {
			if (mailDataLogger.isInfoEnabled()) {
				mailDataLogger.info("Mail content : \n" + new String(mailRequest.getMailContent(), Charsets.UTF_8));
			}
			doTheJob(mailRequest, udr);

		} catch (ProcessingEmailException pe) {	
			notifyUser(udr,  mailRequest.getMailContent(), pe);
		} catch (CollectionNotFoundException e) {
			notifyUser(udr, mailRequest.getMailContent(), e);
		} catch (ItemNotFoundException e) {
			notifyUser(udr, mailRequest.getMailContent(), e);
		}
	}

	private void notifyUserQuotaExceeded(UserDataRequest udr,
			QuotaExceededException e) {
		errorManager.sendQuotaExceededError(udr, e);
	}

	private void notifyUser(UserDataRequest udr, byte[] mailContent, Throwable t) {
		logger.error("Error while sending mail. A mail with the error will be sent at the sender.", t);
		errorManager.sendMailHandlerError(udr, mailContent, t);
	}

}
