package org.obm.sync.server.mailer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.obm.sync.Messages;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.server.handler.ErrorMail;
import org.obm.sync.server.template.ITemplateLoader;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.MailService;
import fr.aliacom.obm.services.constant.ConstantService;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Singleton
public class ErrorMailer extends AbstractMailer {

	private final static int INTERVAL_BETWEEN_NOTIFICATION = 6;
	private ConcurrentMap<String, Date> lastNotificationDateByUser;
	
	@Inject
	protected ErrorMailer(MailService mailService, ConstantService constantService, ITemplateLoader templateLoader) {
		super(mailService, constantService, templateLoader);
		lastNotificationDateByUser = 
			new MapMaker()
				.expireAfterWrite(INTERVAL_BETWEEN_NOTIFICATION, TimeUnit.HOURS)
				.makeMap();
	}
	
	public void notifyConnectorVersionError(AccessToken at, String version, Locale locale) throws NotificationException {
		try {
			Date now = new Date();
			Date lastNotificationDate = lastNotificationDateByUser.putIfAbsent(at.getUserWithDomain(), now);
			if (isNotificationNeeded(lastNotificationDate, now)) {
				lastNotificationDateByUser.put(at.getUserWithDomain(), now);
				ErrorMail mail = 
					new ErrorMail(
							getSystemAddress(at), 
							convertAccessTokenToAddresse(at), 
							connectorVersionErrorTitle(locale), 
							newUserBodyTxt(version,locale));
				sendNotificationMessage(mail, ImmutableList.of(convertAccessTokenToAddresse(at)));
			}
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

	private boolean isNotificationNeeded(Date lastNotificationDate, Date now) {
		if (lastNotificationDate == null) {
			return true;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastNotificationDate);
		cal.add(Calendar.HOUR, INTERVAL_BETWEEN_NOTIFICATION);
		if (cal.getTime().before(now)) {
			return true;
		}
		return false;
	}


	private String connectorVersionErrorTitle(Locale locale) {
		return new Messages(locale).connectorVersionErrorTitle();
	}
	
	
	private String newUserBodyTxt(String version, Locale locale) throws IOException, TemplateException {
		return applyOBMConnectorVersionOnTemplate("OBMConnectorErrorVersionPlain.tpl", version, locale);
	}
	
	private void sendNotificationMessage(ErrorMail mail, List<InternetAddress>  addresses) throws MessagingException {
		MimeMessage mimeMail = mail.buildMimeMail(session);
		mailService.sendMessage(session, addresses, mimeMail);
	}
	
	private String applyOBMConnectorVersionOnTemplate(String templateName, String version, Locale locale)
			throws IOException, TemplateException {
		Builder<Object, Object> builder = buildOBMConnectorVersionDatamodel(version);
		Template template = templateLoader.getTemplate(templateName, locale);
		return applyTemplate(builder.build(), template);
	}
	
	private Builder<Object, Object> buildOBMConnectorVersionDatamodel(String version) {
		Builder<Object, Object> datamodel = ImmutableMap.builder().put("version", version);
		return datamodel;
	}
}
