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

import org.obm.push.backend.ErrorsManager;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.configuration.LoggerModule;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ItemNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.mail.MailBackend;
import org.obm.push.protocol.MailProtocol;
import org.obm.push.protocol.bean.MailRequest;
import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class SmartReplyHandler extends MailRequestHandler {

	@Inject
	/* package */ SmartReplyHandler(MailBackend mailBackend, ErrorsManager errorManager, 
			MailProtocol mailProtocol,
			@Named(LoggerModule.MAIL_DATA)Logger mailDataLogger,
			@Named(LoggerModule.FULL_REQUEST)Logger fullRequestLogger,
			@Named(LoggerModule.TRIMMED_REQUEST)Logger trimmedRequestLogger) {
		
		super(mailBackend, errorManager, mailProtocol, mailDataLogger, fullRequestLogger, trimmedRequestLogger);
	}

	@Override
	public void doTheJob(MailRequest mailRequest, UserDataRequest udr) 
			throws ProcessingEmailException, CollectionNotFoundException, ItemNotFoundException {
		
		mailBackend.replyEmail(udr, mailRequest.getMailContent(), mailRequest.isSaveInSent(),
				Integer.valueOf(mailRequest.getCollectionId()), mailRequest.getServerId());
	}

}
