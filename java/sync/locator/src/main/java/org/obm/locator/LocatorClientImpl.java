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
package org.obm.locator;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.naming.ConfigurationException;

import org.apache.commons.io.IOUtils;
import org.obm.configuration.ObmConfigurationService;
import org.obm.locator.store.LocatorService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class LocatorClientImpl implements LocatorService {

	private final String locatorUrl;

	@Inject
	private LocatorClientImpl(ObmConfigurationService obmConfigurationService) throws ConfigurationException {
		locatorUrl = ensureTrailingSlash( obmConfigurationService.getLocatorUrl() );
	}

	private String ensureTrailingSlash(String url) {
		if (url.endsWith("/")) {
			return url;
		}
		return url + "/";
	}
	
	@Override
	public String getServiceLocation(String serviceSlashProperty, String loginAtDomain) throws LocatorClientException {
		String url = buildFullServiceUrl(serviceSlashProperty, loginAtDomain);
		InputStream is = null;
		try {
			is = new URL(url).openStream();
			List<String> lines = IOUtils.readLines(is, "utf-8");
			return lines.get(0);
		} catch (MalformedURLException e) {
			throw new LocatorClientException(e.getMessage(), e);
		} catch (IOException e) {
			throw new LocatorClientException(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}
	
	private String buildFullServiceUrl(String serviceSlashProperty, String loginAtDomain) {
		return locatorUrl + "location/host/" + serviceSlashProperty + "/" + loginAtDomain;
	}
}