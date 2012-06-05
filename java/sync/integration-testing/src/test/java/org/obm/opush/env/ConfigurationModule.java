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
package org.obm.opush.env;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;

import java.util.Locale;

import org.obm.configuration.ConfigurationService;
import org.obm.configuration.EmailConfiguration;
import org.obm.configuration.EmailConfigurationImpl;
import org.obm.configuration.SyncPermsConfigurationService;
import org.obm.configuration.TestTransactionConfiguration;
import org.obm.configuration.TransactionConfiguration;

public final class ConfigurationModule extends AbstractOverrideModule {

	private final Configuration configuration;

	public ConfigurationModule(Configuration configuration) {
		super();
		this.configuration = configuration;
	}
	
	@Override
	protected void configureImpl() {
		bind(TransactionConfiguration.class).to(TestTransactionConfiguration.class);
		bindWithMock(ConfigurationService.class);
		bindWithMock(EmailConfigurationImpl.class);
		bindWithMock(SyncPermsConfigurationService.class);
		defineBehavior();
	}

	private void defineBehavior() {
		ConfigurationService configurationService = getMock(ConfigurationService.class);
		expect(configurationService.getResourceBundle(anyObject(Locale.class)))
			.andReturn(configuration.bundle).anyTimes();
		
		SyncPermsConfigurationService syncPerms = getMock(SyncPermsConfigurationService.class);
		expect(syncPerms.getBlackListUser()).andReturn(configuration.syncPerms.blacklist).anyTimes();
		expect(syncPerms.allowUnknownPdaToSync()).andReturn(configuration.syncPerms.allowUnkwownDevice).anyTimes();
		
		EmailConfiguration emailConfiguration = getMock(EmailConfigurationImpl.class);
		expect(emailConfiguration.activateTls()).andReturn(configuration.mail.activateTls).anyTimes();
		expect(emailConfiguration.loginWithDomain()).andReturn(configuration.mail.loginWithDomain).anyTimes();
	}
}