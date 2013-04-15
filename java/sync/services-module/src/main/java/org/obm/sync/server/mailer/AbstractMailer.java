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
import java.io.StringWriter;
import java.util.Locale;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.obm.sync.Messages;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.server.template.ITemplateLoader;

import com.google.common.collect.ImmutableMap;

import fr.aliacom.obm.common.MailService;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public abstract class AbstractMailer {

	
	protected final Session session;
	protected MailService mailService;
	protected ITemplateLoader templateLoader;
		
	private ObmSyncConfigurationService constantService;
	
	public static class NotificationException extends RuntimeException {
		private static final long serialVersionUID = -7984056189522385977L;

		public NotificationException(Exception e) {
			super(e);
		}
	}
	
	protected AbstractMailer(MailService mailService, ObmSyncConfigurationService constantService, ITemplateLoader templateLoader) {
		this.mailService = mailService;
		this.constantService = constantService;
		this.templateLoader = templateLoader;
		session = Session.getDefaultInstance(new Properties());
	}
	
	protected Address getSystemAddress(AccessToken at) throws AddressException {
		return  new InternetAddress(constantService.getObmSyncMailer(at));
	}

	protected InternetAddress convertAccessTokenToAddresse(AccessToken at) throws AddressException {
		return new InternetAddress(at.getUserEmail());
	}
	
	protected String applyTemplate(ImmutableMap<Object, Object> datamodel, Template template) 
		throws TemplateException, IOException {
		StringWriter stringWriter = new StringWriter();
		template.process(datamodel, stringWriter);
		stringWriter.flush();
		return stringWriter.toString();
	}
	
	public Messages getMessages(Locale locale) {
		return new Messages(constantService, locale);
	}
	
}
