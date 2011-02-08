package fr.aliacom.obm.common;

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.obm.sync.ObmSmtpProvider;

import com.google.inject.Inject;

public class MailService {

	private final ObmSmtpProvider provider;

	@Inject
	public MailService(ObmSmtpProvider provider) {
		this.provider = provider;
	}
	
	public void sendMessage(Session session, List<InternetAddress> to, MimeMessage message) throws MessagingException {
		message.setRecipients(RecipientType.TO, to.toArray(new InternetAddress[0]));
		provider.sendEmail(session, message);
	}
	
}
