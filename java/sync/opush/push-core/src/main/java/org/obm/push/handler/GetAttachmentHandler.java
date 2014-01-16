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

import javax.servlet.http.HttpServletResponse;

import org.obm.push.backend.IContinuation;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.activesync.AttachementNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.impl.Responder;
import org.obm.push.mail.MailBackend;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GetAttachmentHandler implements IRequestHandler {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final MailBackend mailBackend;

	@Inject
	protected GetAttachmentHandler(MailBackend mailBackend) {
		this.mailBackend = mailBackend;
	}

	@Override
	public void process(IContinuation continuation, UserDataRequest udr,
			ActiveSyncRequest request, Responder responder) {

		String AttachmentName = request.getParameter("AttachmentName");

		try {
			MSAttachementData attachment = getAttachment(udr, AttachmentName);
			responder.sendResponseFile(attachment.getContentType(),	attachment.getFile());
		} catch (AttachementNotFoundException e) {
			sendErrorResponse(responder, e);
		} catch (CollectionNotFoundException e) {
			sendErrorResponse(responder, e);
		} catch (ProcessingEmailException e) {
			sendErrorResponse(responder, e);
		}
	}

	private void sendErrorResponse(Responder responder, Exception exception) {
		logger.error(exception.getMessage(), exception);
		responder.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

	private MSAttachementData getAttachment(UserDataRequest udr, String AttachmentName) 
			throws AttachementNotFoundException, CollectionNotFoundException, ProcessingEmailException {
		return mailBackend.getAttachment(udr, AttachmentName);
	}
	
}
