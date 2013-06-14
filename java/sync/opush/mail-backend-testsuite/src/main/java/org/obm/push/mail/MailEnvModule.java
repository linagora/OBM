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
package org.obm.push.mail;

import org.easymock.EasyMock;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.push.mail.greenmail.GreenMailEmailConfiguration;
import org.obm.push.mail.greenmail.GreenMailSmtpProvider;
import org.obm.push.mail.smtp.SmtpProvider;
import org.obm.push.mail.transformer.Identity;
import org.obm.push.mail.transformer.Transformer;
import org.obm.push.resource.ResourceCloser;
import org.obm.push.resource.ResourceCloserImpl;
import org.obm.push.service.EventService;
import org.obm.sync.client.login.LoginService;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

public class MailEnvModule extends AbstractModule {

	private final int imapTimeout;

	public MailEnvModule(int imapTimeoutInMilliseconds) {
		this.imapTimeout = imapTimeoutInMilliseconds;
	}
	
	@Override
	protected void configure() {
		bind(EventService.class).toInstance(EasyMock.createMock(EventService.class));
		bind(LoginService.class).toInstance(EasyMock.createMock(LoginService.class));
		bind(LocatorService.class).toInstance(new LocatorService() {
			
			@Override
			public String getServiceLocation(String serviceSlashProperty,
					String loginAtDomain) throws LocatorClientException {
				return "127.0.0.1";
			}
		});

		bind(EmailConfiguration.class).to(GreenMailEmailConfiguration.class);
		bind(Integer.class).annotatedWith(Names.named("imapTimeout")).toInstance(imapTimeout);
		bind(SmtpProvider.class).to(GreenMailSmtpProvider.class);

		bind(MailViewToMSEmailConverter.class).to(MailViewToMSEmailConverterImpl.class);
		Multibinder<Transformer.Factory> transformers = 
				Multibinder.newSetBinder(binder(), Transformer.Factory.class);
		transformers.addBinding().to(Identity.Factory.class);
		
		bind(ResourceCloser.class).to(ResourceCloserImpl.class);
	}
}
