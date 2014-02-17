/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.opush.env;

import org.easymock.IMocksControl;
import org.obm.configuration.EmailConfiguration;
import org.obm.guice.AbstractOverrideModule;
import org.obm.opush.CountingMinigStoreClient;
import org.obm.opush.CountingStoreClient;
import org.obm.push.exception.OpushLocatorException;
import org.obm.push.mail.greenmail.GreenMailEmailConfiguration;
import org.obm.push.mail.greenmail.GreenMailProviderModule;
import org.obm.push.mail.imap.MinigStoreClient;
import org.obm.push.mail.smtp.SmtpProvider;
import org.obm.push.minig.imap.StoreClient;
import org.obm.push.service.EventService;
import org.obm.push.service.OpushLocatorService;

import com.google.inject.name.Names;

public class GreenMailEnvModule extends AbstractOverrideModule {

	public GreenMailEnvModule(IMocksControl mocksControl) {
		super(mocksControl);
	}
	
	@Override
	protected void configureImpl() {
		install(new GreenMailProviderModule());
		bindWithMock(EventService.class);
		bind(OpushLocatorService.class).toInstance(new OpushLocatorService() {
			
			@Override
			public String getServiceLocation(String serviceSlashProperty,
					String loginAtDomain) throws OpushLocatorException {
				return "127.0.0.1";
			}
		});
		
		bind(EmailConfiguration.class).to(GreenMailEmailConfiguration.class);
		bindImapTimeout();
		bind(SmtpProvider.class).to(GreenMailSmtpProvider.class);

		bind(MinigStoreClient.Factory.class).to(CountingMinigStoreClient.Factory.class);
		bind(StoreClient.Factory.class).to(CountingStoreClient.Factory.class);
	}
	
	protected void bindImapTimeout() {
		bind(Integer.class).annotatedWith(Names.named("imapTimeout")).toInstance(360000);
	}
}
