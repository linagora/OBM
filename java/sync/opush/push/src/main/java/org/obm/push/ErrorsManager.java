package org.obm.push;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.james.mime4j.MimeIOException;
import org.apache.james.mime4j.field.address.AddressList;
import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.Message;
import org.apache.james.mime4j.message.Multipart;
import org.apache.james.mime4j.message.TextBody;
import org.obm.push.backend.IErrorsManager;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.QuotaExceededException;
import org.obm.push.mail.IEmailManager;
import org.obm.push.utils.Mime4jUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ErrorsManager implements IErrorsManager {
	
	private final static String errorNameSender = "X-OBM-OPUSH";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final IEmailManager manager;
	private final Messages messages;
	
	@Inject
	private ErrorsManager(IEmailManager manager, Messages messages) {
		this.manager = manager;
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
			InputStream in = Mime4jUtils.toInputStream(mm);
			manager.storeInInbox(bs, in, false);
		} catch (Throwable e) {
			logger.error("Error during storing error mail in the inbox folder", e);
		}
	}

	private Message prepareMessage(BackendSession bs, String subject, String body, InputStream errorMail) throws FileNotFoundException, IOException {
		Message mm = prepareMessageHeaders(bs, subject);
		
		Multipart multipart = new Multipart("mixed");

		BodyPart part = Mime4jUtils.createTextPart(body.toString(), "plain");
		multipart.addBodyPart(part);
		Mime4jUtils.attach(multipart, errorMail, "error_message.eml",
				"message/rfc822");

		mm.setMultipart(multipart);
		return mm;
	}

	private Message prepareMessage(BackendSession bs, String subject, String body) {
		Message mm = prepareMessageHeaders(bs, subject);
		TextBody part = Mime4jUtils.createBody(body);
		mm.setBody(part, "text/plain", ImmutableMap.of("charset", "UTF-8"));
		return mm;
	}
	
	private Message prepareMessageHeaders(BackendSession bs, String subject) {
		Message mm = Mime4jUtils.getNewMessage();
		mm.createMessageId(getHostname());
		mm.setSubject(subject);
		mm.setFrom(new Mailbox(errorNameSender, "postmaster", ""));
		mm.setTo(Mailbox.parse(bs.getCredentials().getUser().getEmail()));
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
			InputStream in = Mime4jUtils.toInputStream(mm);
			manager.storeInInbox(bs, in, false);
		} catch (Throwable t) {
			logger.error("Error during storing error mail in the inbox folder", t);
		}
	}

	private Message buildErrorMessage(BackendSession bs, byte[] truncatedData, int maxSize) throws MimeIOException, IOException {
		String subject = messages.mailTooLargeTitle();
		String previousMessageReferenceText = buildPreviousMessageReferenceText(truncatedData);
		String errorMessage = messages.mailTooLargeBodyStructure(maxSize, previousMessageReferenceText);
		return prepareMessage(bs, subject, errorMessage);
	}
	
	private String buildPreviousMessageReferenceText(byte[] truncatedData) throws MimeIOException, IOException {
		Message message = new Message(new ByteArrayInputStream(truncatedData));
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
