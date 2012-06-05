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

package org.obm.dbcp;

import java.nio.charset.Charset;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.obm.configuration.ConfigurationService;
import org.obm.configuration.DatabaseSystem;
import org.obm.configuration.resourcebundle.Control;

import com.google.common.base.Charsets;

public class ConfigurationServiceFixturePostgreSQL implements ConfigurationService {

	@Override
	public String getLocatorUrl() throws ConfigurationException {
		return "http://localhost.localdomain:8084/obm-locator/";
	}

	@Override
	public String getObmUIBaseUrl() {
		return "http://" + getExternalUrl();
	}

	@Override
	public String getObmSyncUrl(String obmSyncHost) {
		return "http://" + getExternalUrl() + ":8080/obm-sync/services";
	}

	@Override
	public int getLocatorCacheTimeout() {
		return 30;
	}

	@Override
	public TimeUnit getLocatorCacheTimeUnit() {
		return TimeUnit.MINUTES;
	}

	@Override
	public ResourceBundle getResourceBundle(Locale locale) {
		return ResourceBundle.getBundle("Messages", locale, new Control());
	}

	@Override
	public String getActiveSyncServletUrl() {
		return "https://" + getExternalUrl() + "/Microsoft-Server-ActiveSync";
	}

	private String getExternalUrl() {
		return "localhost.localdomain";
	}

	@Override
	public Charset getDefaultEncoding() {
		return Charsets.UTF_8;
	}

	@Override
	public Integer getDataBaseMaxConnectionPoolSize() {
		return 10;
	}

	@Override
	public DatabaseSystem getDataBaseSystem() {
		return DatabaseSystem.PGSQL;
	}

	@Override
	public String getDataBaseName() {
		return "obm";
	}

	@Override
	public String getDataBaseHost() {
		return getExternalUrl();
	}

	@Override
	public String getDatabaseLogin() {
		return "obm";
	}

	@Override
	public String getDatabasePassword() {
		return "obm";
	}

	@Override
	public int transactionTimeoutInSeconds() {
		return 60;
	}
	
}
