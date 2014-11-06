/* ***** BEGIN LICENSE BLOCK *****
 * 
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
 * 
 * ***** END LICENSE BLOCK ***** */
package com.linagora.obm.ui.ioc;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.google.common.base.Strings;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.google.common.base.Objects;

public class Module extends AbstractModule {

	public static final String SERVER_URL = "serverUrl";
	public static final String CLIENT_URL = "clientUrl";
	// This variable defines the browser default version used for Selenium tests.
	// The empty string means that the default version available on the machine will be used.
	private static final String DEFAULT_VERSION = "";

	@Override
	protected void configure() {
		bind(URL.class).annotatedWith(Names.named(SERVER_URL)).toInstance(readRequiredUrlArg(SERVER_URL));

		URL clientUrl = readUrlArg(CLIENT_URL);
		if (clientUrl != null) {
			bind(WebDriver.class).toInstance(new RemoteWebDriver(clientUrl, buildDriverCapabilities()));
		} else {
			bind(WebDriver.class).toInstance(new FirefoxDriver());
		}
	}

	private Capabilities buildDriverCapabilities() {
		DesiredCapabilities capabilities = DesiredCapabilities.firefox()	;
		String browser_version = Objects.firstNonNull(System.getProperty("browser_version"), DEFAULT_VERSION);
		capabilities.setCapability("version", browser_version);
		capabilities.setCapability("platform", Platform.LINUX);
		return capabilities;
	}
	
	private URL readRequiredUrlArg(String vmArg) {
		URL urlArg = readUrlArg(vmArg);
		if (urlArg != null) {
			return urlArg;
		}
		throw new IllegalStateException(vmArg + " arg is required");
	}

	private URL readUrlArg(String vmArg) {
		String serverUrl = System.getProperty(vmArg);
		if (!Strings.isNullOrEmpty(serverUrl)) {
			try {
				return new URL(serverUrl);
			} catch (MalformedURLException e) {
				throw new IllegalStateException(vmArg + " must be a valid URL, found:" + serverUrl);
			}
		}
		return null;
	}

}
