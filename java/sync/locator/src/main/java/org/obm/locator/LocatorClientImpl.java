/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.locator;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.apache.http.client.fluent.Request;
import org.obm.configuration.LocatorConfiguration;
import org.obm.locator.store.LocatorService;
import org.obm.logger.LoggerModule;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class LocatorClientImpl implements LocatorService {

	private static final char RESPONSE_SEPARATOR = '\n';
	private final String locatorUrl;
	private final int timeoutInMS;

	@Inject
	@VisibleForTesting LocatorClientImpl(LocatorConfiguration locatorConfigurationService,
			@Named(LoggerModule.CONFIGURATION)Logger configurationLogger) throws ConfigurationException {
		locatorUrl = ensureTrailingSlash( locatorConfigurationService.getLocatorUrl() );
		timeoutInMS = Ints.checkedCast(TimeUnit.SECONDS.toMillis(locatorConfigurationService.getLocatorClientTimeoutInSeconds()));
		configurationLogger.info("Locator client service url : {}", locatorUrl);
		configurationLogger.info("Locator client timeout : {} seconds", locatorConfigurationService.getLocatorClientTimeoutInSeconds());
	}

	private String ensureTrailingSlash(String url) {
		if (url.endsWith("/")) {
			return url;
		}
		return url + "/";
	}
	
	@Override
	public String getServiceLocation(String serviceSlashProperty, String loginAtDomain) throws LocatorClientException {
		try {
			String response = Request.Get(buildFullServiceUrl(serviceSlashProperty, loginAtDomain))
				.connectTimeout(timeoutInMS)
				.socketTimeout(timeoutInMS)
				.execute().returnContent().asString();
			return Iterables.get(Splitter.on(RESPONSE_SEPARATOR).split(response), 0);
		} catch (MalformedURLException e) {
			throw new LocatorClientException(e.getMessage(), e);
		} catch (SocketTimeoutException e) {
			throw new LocatorClientException(e.getMessage(), e);
		} catch (IOException e) {
			throw new LocatorClientException(e.getMessage(), e);
		}
	}
	
	private String buildFullServiceUrl(String serviceSlashProperty, String loginAtDomain) {
		return locatorUrl + "location/host/" + serviceSlashProperty + "/" + loginAtDomain;
	}
}
