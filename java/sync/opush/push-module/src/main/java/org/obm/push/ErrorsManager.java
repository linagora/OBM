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
package org.obm.push;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.address.AddressList;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.field.address.AddressBuilder;
import org.apache.james.mime4j.field.address.ParseException;
import org.apache.james.mime4j.message.BodyPart;
import org.obm.push.backend.IErrorsManager;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.QuotaExceededException;
import org.obm.push.mail.MailboxService;
import org.obm.push.utils.Mime4jUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ErrorsManager implements IErrorsManager {
	
	private final static String errorNameSender = "X-OBM-OPUSH";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final MailboxService manager;
	private final Messages messages;

	private final Mime4jUtils mime4jUtils;
	
	@Inject
	private ErrorsManager(MailboxService manager, Messages messages, Mime4jUtils mime4jUtils) {
		this.manager = manager;
		this.mime4jUtils = mime4jUtils;
		this.messages = messages;
	}

	/**
	 * TODO don't use static text
	 */
	@Override
	public void sendMailHandlerError(BackendSession bs, byte[] errorMail, Throwable error) {
		String subject = "OPUSH - Error lors de l'envoi de mail";
		StringBuilder body = new StringBuilder();
		body.append("Le mail en pièce jointe n'a pas pu être envoyé. Si ce problème ce reproduit veuillez contacter votre administrateur\r\n\r\n");
		body.append("Source de l'erreur: \r\n");
		body.append(error.toString());
		body.append("\r\n");
		if(error.getCause() != null){
			body.append(error.getCause());
		}
		try {
			Message mm = prepareMessage(bs, subject, body.toString(), new ByteArrayInputStream(errorMail));
			InputStream in = mime4jUtils.toInputStream(mm);
			manager.storeInInbox(bs, in, false);
		} catch (Throwable e) {
			logger.error("Error during storing error mail in the inbox folder", e);
		}
	}

	private Message prepareMessage(BackendSession bs, String subject, String body, InputStream errorMail) throws FileNotFoundException, IOException, ParseException {
		Message mm = prepareMessageHeaders(bs, subject);
		
		Multipart multipart = mime4jUtils.createMultipartMixed();

		BodyPart part = mime4jUtils.createTextPart(body.toString(), "plain");
		multipart.addBodyPart(part);
		mime4jUtils.attach(multipart, errorMail, "error_message.eml",
				"message/rfc822");

		mm.setBody(multipart);
		return mm;
	}

	private Message prepareMessage(BackendSession bs, String subject, String body) throws ParseException, UnsupportedEncodingException {
		Message mm = prepareMessageHeaders(bs, subject);
		TextBody part = mime4jUtils.createBody(body);
		mm.setBody(part);
		return mm;
	}
	
	private Message prepareMessageHeaders(BackendSession bs, String subject) throws ParseException {
		Message mm = mime4jUtils.createMessage();
		mm.createMessageId(getHostname());
		mm.setSubject(subject);
		mm.setFrom(new Mailbox(errorNameSender, "postmaster", ""));
		mm.setTo(AddressBuilder.DEFAULT.parseMailbox(bs.getCredentials().getUser().getEmail()));
		return mm;
	}

	private String getHostname() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			logger.error("This server doesn't have a hostname", e);
		}
		return "unknown";
	}

	@Override
	public void sendQuotaExceededError(BackendSession bs,
			QuotaExceededException e) {
		
		try {
			Message mm = buildErrorMessage(bs, e.getLoadedData(), e.getQuota());
			InputStream in = mime4jUtils.toInputStream(mm);
			manager.storeInInbox(bs, in, false);
		} catch (Throwable t) {
			logger.error("Error during storing error mail in the inbox folder", t);
		}
	}

	private Message buildErrorMessage(BackendSession bs, byte[] truncatedData, int maxSize) throws MimeIOException, IOException, MimeException {
		String subject = messages.mailTooLargeTitle();
		String previousMessageReferenceText = buildPreviousMessageReferenceText(truncatedData);
		String errorMessage = messages.mailTooLargeBodyStructure(maxSize, previousMessageReferenceText);
		return prepareMessage(bs, subject, errorMessage);
	}
	
	private String buildPreviousMessageReferenceText(byte[] truncatedData) throws MimeIOException, IOException, MimeException {
		Message message = mime4jUtils.parseMessage(new ByteArrayInputStream(truncatedData));
		String messageId = message.getMessageId();
		String subject = message.getSubject();
		String to = addressListToString(message.getTo());
		String cc = addressListToString(message.getCc());
		String bcc = addressListToString(message.getBcc());
		return messages.mailTooLargeHeaderFormat(messageId, subject, to, cc, bcc);
	}
	
	private String addressListToString(AddressList al) {
		if (al == null) {
			return "";
		} else {
			return al.toString();
		}
	}
}
