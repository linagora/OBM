package org.obm.sync;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.obm.locator.LocatorClientException;
import org.obm.sync.auth.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ObmSmtpProvider {

	private static final Logger logger = LoggerFactory
			.getLogger(ObmSmtpProvider.class);
	private ObmSmtpConf conf;
	
	@Inject
	private ObmSmtpProvider(ObmSmtpConf obmSmtpConf) {
		conf = obmSmtpConf;
	}
	
	public void sendEmail(MimeMessage message, AccessToken token) throws MessagingException {
		Transport transport = null;
		
		try {
			Session session = buildSession(token.getDomain());
			
			transport = session.getTransport("smtp");
			transport.connect();
			transport.sendMessage(message, message.getAllRecipients());
		} catch (LocatorClientException e) {
			logger.error("Couldn't send the message", e);
		} finally {
			if (transport != null) {
				transport.close();
			}
		} 
    }
	
	private Session buildSession(String domain) throws LocatorClientException {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", conf.getServerAddr(domain));	
		properties.put("mail.smtp.port", conf.getServerPort(domain));
		Session session = Session.getDefaultInstance(properties);
		return session;
	}
}
