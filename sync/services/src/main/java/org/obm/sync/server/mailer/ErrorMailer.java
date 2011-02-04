package org.obm.sync.server.mailer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.obm.sync.Messages;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.server.handler.ErrorMail;
import org.obm.sync.server.template.ITemplateLoader;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Inject;

import fr.aliacom.obm.common.MailService;
import fr.aliacom.obm.services.constant.ConstantService;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class ErrorMailer extends AbstractMailer{

	@Inject
	protected ErrorMailer(MailService mailService, ConstantService constantService, ITemplateLoader templateLoader)
			throws IOException {
		super(mailService, constantService, templateLoader);
	}
	
	public void notifyConnectorVersionError(AccessToken at, String minMajor, String minMinor, String minRelease, Locale locale) throws NotificationException {
		try {
			ErrorMail mail = 
				new ErrorMail(
						getSystemAddress(at), 
						convertAccessTokenToAddresse(at), 
						connectorVersionErrorTitle(locale), 
						newUserBodyTxt(minMajor, minMinor, minRelease,locale));
			sendNotificationMessage(mail, ImmutableList.of(convertAccessTokenToAddresse(at)));
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		} catch (AddressException e) {
			throw new NotificationException(e);
		} catch (MessagingException e) {
			throw new NotificationException(e);
		}
	}


	private String connectorVersionErrorTitle(Locale locale) {
		return new Messages(locale).connectorVersionErrorTitle();
	}
	
	
	private String newUserBodyTxt(String major, String minor, String release, Locale locale) throws IOException, TemplateException {
		return applyOBMConnectorVersionOnTemplate("OBMConnectorErrorVersionPlain.tpl", major, minor, release, locale);
	}
	
	private void sendNotificationMessage(ErrorMail mail, List<InternetAddress>  addresses) throws MessagingException, IOException{
		MimeMessage mimeMail = mail.buildMimeMail(session);
		mailService.sendMessage(session, addresses, mimeMail);
	}
	
	private String applyOBMConnectorVersionOnTemplate(String templateName, String major, String minor, String release, Locale locale)
			throws IOException, TemplateException {
		Builder<Object, Object> builder = buildOBMConnectorVersionDatamodel(major, minor, release);
		Template template = templateLoader.getTemplate(templateName, locale);
		return applyTemplate(builder.build(), template);
	}
	
	private Builder<Object, Object> buildOBMConnectorVersionDatamodel(String major, String minor, String release) {
		Builder<Object, Object> datamodel = ImmutableMap.builder()
			.put("major", Strings.nullToEmpty(major))
			.put("minor", Strings.nullToEmpty(minor))
			.put("release", Strings.nullToEmpty(release));
		return datamodel;
	}
}
