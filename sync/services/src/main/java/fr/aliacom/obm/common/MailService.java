package fr.aliacom.obm.common;

import java.util.Iterator;
import java.util.List;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.ObmSmtpProvider;
import org.obm.sync.server.mailer.AbstractMailer.NotificationException;

import com.google.inject.Inject;

public class MailService {

	private static final Log logger = LogFactory.getLog(MailService.class);
	private final ObmSmtpProvider provider;

	@Inject
	public MailService(ObmSmtpProvider provider) {
		this.provider = provider;
	}
	
	public void sendMessage(Session session, List<InternetAddress> to, MimeMessage message) {
		
		try{
			message.setRecipients(RecipientType.TO, to.toArray(new InternetAddress[0]));
			provider.sendEmail(session, message);
		} catch (Throwable e) {
			logger.error(getErrorLog(to, message), e);
		}
	}
	
	private String getErrorLog(List<InternetAddress> to, MimeMessage message){
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("Error while sending mail[subject:");
			sb.append(message.getSubject());
			sb.append("; from:");
			sb.append(message.getFrom() != null && message.getFrom().length> 0 ? ""+message.getFrom()[0] : "null");
			sb.append("; to:");
			for(Iterator<InternetAddress> it = to.iterator(); it.hasNext();){
				sb.append(it.next().toString());
				if(it.hasNext()){
					sb.append(",");
				}
			}
			return sb.toString();
		} catch (Exception e) {
			return e.getMessage();
		}
		
	}
	
}
