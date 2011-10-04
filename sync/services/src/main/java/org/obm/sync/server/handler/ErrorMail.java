package org.obm.sync.server.handler;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

public class ErrorMail {

	private final Address from;
	private final Address to;
	private final String subject;
	private final String bodyTxt;
	
	public ErrorMail(Address from, Address to, String subject,
			String bodyTxt) {
		this.from = from;
		this.to = to;
		this.subject = subject;
		this.bodyTxt = bodyTxt;
	}
	
	
	public MimeMessage buildMimeMail(Session session) throws MessagingException {
		MimeMessage message = new MimeMessage(session);
		message.setFrom(from);
		message.setRecipient(RecipientType.TO, to);
		message.setSubject(subject);
		message.setText(bodyTxt);
		return message;
	}
}
