package org.obm.sync;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ObmSmtpProvider {
	
	private ObmSmtpConf conf;
	
	@Inject
	private ObmSmtpProvider(ObmSmtpConf obmSmtpConf) {
		conf = obmSmtpConf;
	}
	
	public void sendMessage(String domain, Charset charset, String from, String subject, 
							String HtmlMessage, Collection<String> recipients) throws MessagingException {
		Session session = buildSession(domain);
		MimeMessage message = prepareMessage(session, charset, from, subject, HtmlMessage, recipients);
		sendEmail(session, message);
	}
	
	/**
	 * Prepare the Mime message with the desired contents
	 */
	private MimeMessage prepareMessage(Session session, Charset charset,
					            String from, String subject,
					            String HtmlMessage, Collection<String> recipients) throws MessagingException {
		//Multipurpose Internet Mail Extensions
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setSubject(subject);
		for (String recipient: recipients) {
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
		}
		message.setContent(HtmlMessage, "text/html; charset=\"" + charset.displayName() + "\"");
		return message;
	}
	
	public void sendEmail(Session session, MimeMessage message) throws MessagingException {
		Transport transport = session.getTransport("smtp");
		try {
			transport.connect();
			transport.sendMessage(message, message.getAllRecipients());
		} finally {
			transport.close();
		}
    }

	private Session buildSession(String domain) {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", conf.getServerAddr(domain));	
		properties.put("mail.smtp.port", conf.getServerPort(domain));
		Session session = Session.getDefaultInstance(properties);
		return session;
	}
}
