/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.server.mailer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.server.handler.ErrorMail;
import org.obm.sync.server.template.ITemplateLoader;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.MailService;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import freemarker.template.Template;
import freemarker.template.TemplateException;

@Singleton
public class ErrorMailer extends AbstractMailer {

	private final static int INTERVAL_BETWEEN_NOTIFICATION = 6;
	private ConcurrentMap<String, Date> lastNotificationDateByUser;
	
	@Inject
	protected ErrorMailer(MailService mailService, ObmSyncConfigurationService constantService, ITemplateLoader templateLoader) {
		super(mailService, constantService, templateLoader);
		Cache<String, Date> cache = CacheBuilder.newBuilder().expireAfterWrite(INTERVAL_BETWEEN_NOTIFICATION, TimeUnit.HOURS)
				.build(new CacheLoader<String, Date>() {
					@Override
					public Date load(String key) throws Exception {
			        	return null;
					}
				});
		lastNotificationDateByUser = cache.asMap(); 
	}
	
	public void notifyConnectorVersionError(AccessToken at, String version, Locale locale, TimeZone timezone) throws NotificationException {
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
							newUserBodyTxt(version,locale, timezone));
				sendNotificationMessage(mail, ImmutableList.of(convertAccessTokenToAddresse(at)), at);
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
		return getMessages(locale).connectorVersionErrorTitle();
	}
	
	
	private String newUserBodyTxt(String version, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyOBMConnectorVersionOnTemplate("OBMConnectorErrorVersionPlain.tpl", version, locale, timezone);
	}
	
	private void sendNotificationMessage(ErrorMail mail, List<InternetAddress>  addresses, AccessToken token) throws MessagingException {
		MimeMessage mimeMail = mail.buildMimeMail(session);
		mailService.sendMessage(addresses, mimeMail, token);
	}
	
	private String applyOBMConnectorVersionOnTemplate(String templateName, String version, Locale locale, TimeZone timezone)
			throws IOException, TemplateException {
		Builder<Object, Object> builder = buildOBMConnectorVersionDatamodel(version);
		Template template = templateLoader.getTemplate(templateName, locale, timezone);
		return applyTemplate(builder.build(), template);
	}
	
	private Builder<Object, Object> buildOBMConnectorVersionDatamodel(String version) {
		Builder<Object, Object> datamodel = ImmutableMap.builder().put("version", version);
		return datamodel;
	}
}
