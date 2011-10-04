package org.obm.sync.server.mailer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;


import org.obm.sync.auth.AccessToken;
import org.obm.sync.server.template.ITemplateLoader;

import com.google.common.collect.ImmutableMap;

import fr.aliacom.obm.common.MailService;
import fr.aliacom.obm.services.constant.ConstantService;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class AbstractMailer {

	
	protected final Session session;
	protected MailService mailService;
	protected ITemplateLoader templateLoader;
		
	private ConstantService constantService;
	
	public static class NotificationException extends RuntimeException {
		private static final long serialVersionUID = -7984056189522385977L;

		public NotificationException(Exception e) {
			super(e);
		}
	}
	
	protected AbstractMailer(MailService mailService, ConstantService constantService, ITemplateLoader templateLoader) {
		this.mailService = mailService;
		this.constantService = constantService;
		this.templateLoader = templateLoader;
		session = Session.getDefaultInstance(new Properties());
	}
	
	protected Address getSystemAddress(AccessToken at) throws AddressException {
		return  new InternetAddress(constantService.getObmSyncMailer(at));
	}

	protected InternetAddress convertAccessTokenToAddresse(AccessToken at) throws AddressException {
		return new InternetAddress(at.getEmail());
	}
	
	protected String applyTemplate(ImmutableMap<Object, Object> datamodel, Template template) 
		throws TemplateException, IOException {
		StringWriter stringWriter = new StringWriter();
		template.process(datamodel, stringWriter);
		stringWriter.flush();
		return stringWriter.toString();
	}
	
}
