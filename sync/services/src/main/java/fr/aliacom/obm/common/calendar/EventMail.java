package fr.aliacom.obm.common.calendar;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.obm.sync.calendar.Attendee;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;

public class EventMail {

	private final Address from;
	private final List<Attendee> recipients;
	private final String subject;
	private final String bodyTxt;
	private final String bodyHtml;
	private final String icsContent;
	private final String icsMethod;
	
	public EventMail(Address from, List<Attendee> recipients, String subject,
			String bodyTxt, String bodyHtml) {
		this(from, recipients,subject,bodyTxt,bodyHtml, null, null);
	}
	
	
	public EventMail(Address from, List<Attendee> recipients, String subject,
			String bodyTxt, String bodyHtml, String icsContent, String icsMethod) {
				this.from = from;
				this.recipients = recipients;
				this.subject = subject;
				this.bodyTxt = bodyTxt;
				this.bodyHtml = bodyHtml;
				this.icsContent = icsContent;
				this.icsMethod = icsMethod;
	}
	
	public MimeMessage buildMimeMail(Session session) throws MessagingException, IOException {
		MimeMessage message = new MimeMessage(session);
		message.setFrom(from);
		message.setRecipients(RecipientType.TO, getRecipients());
		message.setSubject(subject);
		if(icsContent != null){
			message.setContent(buildParts());
		} else {
			message.setContent(buildAlternativePart());
		}
		return message;
	}
	
	private Address[] getRecipients() throws UnsupportedEncodingException {
		List<Address> addresses = Lists.newArrayList();
		for (Attendee recipient: recipients) {
			addresses.add(new InternetAddress(recipient.getEmail(), recipient.getDisplayName()));
		}
		return addresses.toArray(new Address[0]);
	}
	
	private MimeMultipart buildParts() throws MessagingException, IOException {
		MimeMultipart mainPart = new MimeMultipart("mixed");
		MimeBodyPart mimeBodyPart = new MimeBodyPart();
		mimeBodyPart.setContent(buildAlternativePart());
		mainPart.addBodyPart(mimeBodyPart);
		mainPart.addBodyPart(createIcsPart());
		return mainPart;
	}
	
	private BodyPart createIcsPart() throws MessagingException, IOException {
		MimeBodyPart part = new MimeBodyPart();
    	ByteArrayDataSource fds = new ByteArrayDataSource(icsContent, "application/ics");   	
        part.setDataHandler(new DataHandler(fds));
        part.addHeader("Content-Transfer-Encoding", "base64");
        part.setFileName("meeting.ics");
		return part;
	}

	private MimeMultipart buildAlternativePart() throws MessagingException {
		MimeMultipart alternativePart = new MimeMultipart("alternative");
		alternativePart.addBodyPart(createTextPart());
		alternativePart.addBodyPart(createHtmlPart());
		if(icsContent != null){
			alternativePart.addBodyPart(createCalendarPart());
		}
		return alternativePart;
	}
	
	private BodyPart createCalendarPart() throws MessagingException {
		MimeBodyPart part = new MimeBodyPart();
		part.setText(icsContent);
		part.setHeader("Content-Type", "text/calendar; charset=UTF-8; method=" + icsMethod + ";");
		return part;
	}

	private BodyPart createHtmlPart() throws MessagingException {
		MimeBodyPart part = new MimeBodyPart();
		part.setText(bodyHtml, Charsets.UTF_8.displayName(), "html");
		return part;
	}

	private MimeBodyPart createTextPart() throws MessagingException {
		MimeBodyPart part = new MimeBodyPart();
		part.setText(bodyTxt, Charsets.UTF_8.displayName(), "plain");
		return part;
	}
}
