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

package org.obm.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.utils.IniFile;

public class LocatorConfigurationImplTest {

	private LocatorConfigurationImpl locatorConfigurationImpl;
	private IniFile iniFile;
	private IMocksControl control;

	@Before
	public void setup() {
		control = createControl();
		iniFile = control.createMock(IniFile.class);
		
		locatorConfigurationImpl = new LocatorConfigurationImpl(iniFile);
	}
	
	@Test
	public void testGetLocatorUrl() throws Exception {
		String host = "10.69.1.23";
		expect(iniFile.getStringValue(LocatorConfigurationImpl.LOCATOR_HOST_KEY))
			.andReturn(host);
		
		control.replay();
		String locatorUrl = locatorConfigurationImpl.getLocatorUrl();
		control.verify();
		
		assertThat(locatorUrl).isEqualTo("http://" + host + ":8084/obm-locator/");
	}
	
	@Test (expected=ConfigurationException.class)
	public void testGetLocatorUrlThrowsConfigurationException() throws Exception {
		expect(iniFile.getStringValue(LocatorConfigurationImpl.LOCATOR_HOST_KEY))
			.andReturn(null);
		
		control.replay();
		try {
			locatorConfigurationImpl.getLocatorUrl();
		} catch (Exception e) {
			control.verify();
			throw e;
		}
	}
	
	@Test
	public void testGetLocatorPort() {
		assertThat(locatorConfigurationImpl.getLocatorPort()).isEqualTo(LocatorConfigurationImpl.LOCATOR_PORT);
	}
	
	@Test
	public void testGetLocatorClientTimeoutInSeconds() {
		int expectedLocatorClientTimeoutInSeconds = 10;
		expect(iniFile.getIntValue(LocatorConfigurationImpl.LOCATOR_CLIENT_TIMEOUT_KEY, LocatorConfigurationImpl.LOCATOR_CLIENT_TIMEOUT_DEFAULT))
			.andReturn(expectedLocatorClientTimeoutInSeconds);
		
		control.replay();
		int locatorClientTimeoutInSeconds = locatorConfigurationImpl.getLocatorClientTimeoutInSeconds();
		control.verify();
		
		assertThat(locatorClientTimeoutInSeconds).isEqualTo(expectedLocatorClientTimeoutInSeconds);
	}
	
	@Test
	public void testGetLocatorCacheTimeout() {
		int expectedLocatorCacheTimeout = 10;
		expect(iniFile.getIntValue(LocatorConfigurationImpl.LOCATOR_CACHE_TIMEOUT_KEY, LocatorConfigurationImpl.LOCATOR_CACHE_TIMEOUT_DEFAULT))
			.andReturn(expectedLocatorCacheTimeout);
		
		control.replay();
		int locatorCacheTimeout = locatorConfigurationImpl.getLocatorCacheTimeout();
		control.verify();
		
		assertThat(locatorCacheTimeout).isEqualTo(expectedLocatorCacheTimeout);
	}
	@Test
	public void testGetLocatorCacheTimeUnit() {
		expect(iniFile.getStringValue(LocatorConfigurationImpl.LOCATOR_CACHE_TIMEUNIT_KEY))
			.andReturn("hours");
		
		control.replay();
		TimeUnit locatorCacheTimeUnit = locatorConfigurationImpl.getLocatorCacheTimeUnit();
		control.verify();
		
		assertThat(locatorCacheTimeUnit).isEqualTo(TimeUnit.HOURS);
	}
}
