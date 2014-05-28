/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.imap.archive;

import static org.easymock.EasyMock.createControl;

import org.easymock.IMocksControl;
import org.obm.domain.dao.UserSystemDao;
import org.obm.guice.AbstractOverrideModule;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.push.mail.greenmail.GreenMailProviderModule;
import org.obm.server.ServerConfiguration;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.inject.util.Modules;

public class TestImapArchiveModules {
	
	public static class Simple extends AbstractModule {
	
		private final IMocksControl mocks = createControl();
		
		@Override
		protected void configure() {
			ServerConfiguration config = ServerConfiguration.defaultConfiguration();
			bind(IMocksControl.class).toInstance(mocks);
			install(Modules.override(new ImapArchiveModule(config)).with(
				new LocalLocatorModule(),
				new AbstractOverrideModule(mocks) {
					
					@Override
					protected void configureImpl() {
						bindWithMock(UserSystemDao.class);
					}
				}
			));
		}
	}
	
	public static class WithGreenmail extends AbstractModule {

		@Override
		protected void configure() {
			install(Modules.override(new Simple()).with(new AbstractModule() {

				@Override
				protected void configure() {
					install(new GreenMailProviderModule());
					bind(Integer.class).annotatedWith(Names.named("imapTimeout")).toInstance(3600);
				}})
			);
		}
	}
	
	public static class LocalLocatorModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(LocatorService.class).toInstance(new LocatorService() {
				
				@Override
				public String getServiceLocation(String serviceSlashProperty, String loginAtDomain) throws LocatorClientException {
					return "localhost";
				}
			});
		}
	}
	
}