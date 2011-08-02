package org.obm.push;

import java.io.InputStream;

import org.apache.james.mime4j.field.address.Mailbox;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.Message;
import org.apache.james.mime4j.message.Multipart;
import org.obm.push.backend.IErrorsManager;
import org.obm.push.bean.BackendSession;
import org.obm.push.mail.IEmailManager;
import org.obm.push.utils.Mime4jUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ErrorsManager implements IErrorsManager {
	
	private final static String errorNameSender = "X-OBM-OPUSH";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final IEmailManager manager;
	
	@Inject
	private ErrorsManager(IEmailManager manager) {
		this.manager = manager;
	}

	/**
	 * TODO don't use static text
	 */
	@Override
	public void sendMailHandlerError(BackendSession bs, InputStream errorMail, Throwable error) {
		String subject = "OPUSH - Error lors de l'envoi de mail";
		
		StringBuilder body = new StringBuilder();
		body.append("Le mail en pièce jointe n'a pas pu être envoyé. Si ce problème ce reproduit veuillez contacter votre administrateur\r\n\r\n");
		body.append("Source de l'erreur: \r\n");
		body.append(error.toString());
		body.append("\r\n");
		if(error.getCause() != null){
			body.append(error.getCause());
		}
		
		Message mm;
		try {
			mm = Mime4jUtils.getNewMessage();
			mm.setSubject(subject);
			mm.setFrom(new Mailbox(errorNameSender, "postmaster", ""));
			mm.setTo(Mailbox.parse(bs.getLoginAtDomain()));
			
			Multipart multipart = new Multipart("mixed");

			BodyPart part = Mime4jUtils.createTextPart(body.toString(), "plain");
			multipart.addBodyPart(part);
			Mime4jUtils.attach(multipart, errorMail, "error_message.eml",
					"message/rfc822");

			mm.setMultipart(multipart);

			InputStream in = Mime4jUtils.toInputStream(mm);
			manager.storeInInbox(bs, in, false);
		} catch (Throwable e) {
			logger.error("Error during storing error mail in the inbox folder", e);
		}
	}

}
