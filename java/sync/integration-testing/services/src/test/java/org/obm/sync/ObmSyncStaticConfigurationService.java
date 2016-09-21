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
package org.obm.sync;

import java.util.Locale;
import java.util.ResourceBundle;

import org.obm.Configuration;
import org.obm.Configuration.ObmSync;
import org.obm.StaticConfigurationService;
import org.obm.sync.auth.AccessToken;

import fr.aliacom.obm.common.calendar.CalendarEncoding;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;


public class ObmSyncStaticConfigurationService {

	public static class ObmSyncConfiguration extends StaticConfigurationService implements ObmSyncConfigurationService {

		private final ObmSync obmSyncConfiguration;

		public ObmSyncConfiguration(Configuration configuration, Configuration.ObmSync obmSyncConfiguration) {
			super(configuration);
			this.obmSyncConfiguration = obmSyncConfiguration;
		}

		@Override
		public String getDefaultTemplateFolder() {
			return obmSyncConfiguration.defaultTemplateFolder;
		}

		@Override
		public String getOverrideTemplateFolder() {
			return obmSyncConfiguration.overrideTemplateFolder;
		}

		@Override
		public String getObmSyncMailer(AccessToken accessToken) {
			return obmSyncConfiguration.obmSyncMailer;
		}

		@Override
		public String getLdapServer() {
			return obmSyncConfiguration.ldapServer;
		}

		@Override
		public String getLdapBaseDn() {
			return obmSyncConfiguration.ldapBaseDn;
		}

		@Override
		public String getLdapFilter() {
			return obmSyncConfiguration.ldapFilter;
		}

		@Override
		public String getLdapBindDn() {
			return obmSyncConfiguration.ldapBindDn;
		}

		@Override
		public String getLdapBindPassword() {
			return obmSyncConfiguration.ldapBindPassword;
		}

		@Override
		public Iterable<String> getLemonLdapIps() {
			return obmSyncConfiguration.lemonLdapIps;
		}

		@Override
		public String getRootAccounts() {
			return obmSyncConfiguration.rootAccounts;
		}

		@Override
		public String getAppliAccounts() {
			return obmSyncConfiguration.appliAccounts;
		}

		@Override
		public String getAnyUserAccounts() {
			return obmSyncConfiguration.anyUserAccounts;
		}

		@Override
		public boolean syncUsersAsAddressBook() {
			return obmSyncConfiguration.syncUsersAsAddressBook;
		}

		@Override
		public CalendarEncoding getEmailCalendarEncoding() {
			return CalendarEncoding.valueOf(obmSyncConfiguration.emailCalendarEncoding);
		}
		
		@Override
		public ResourceBundle getResourceBundle(Locale locale) {
			return obmSyncConfiguration.bundle;
		}

	}
}