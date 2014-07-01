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
package org.obm.sync.locators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.DomainConfiguration;
import org.obm.locator.store.LocatorService;


public class LocatorTest {

	private IMocksControl mocksControl;
	private LocatorService locatorService;
	private DomainConfiguration domainConfiguration;
	private Locator locator;

	@Before
	public void setup() {
		mocksControl = createControl();
		locatorService = mocksControl.createMock(LocatorService.class);
		domainConfiguration = mocksControl.createMock(DomainConfiguration.class);
		locator = new Locator(domainConfiguration, locatorService);
		unsetObmSyncVMArgument();
	}
	
	private void unsetObmSyncVMArgument() {
		System.clearProperty(Locator.OBM_SYNC_HOST);
	}
	
	@Test
	public void testGetObmSyncHostWithVMArgument() {
		String loginAtDomain = "user@linagora.com";
		String expectedObmSyncHost = "10.69.0.172";
		setObmSyncVMArgument(expectedObmSyncHost);
		
		mocksControl.replay();
		
		String obmSyncHost = locator.getObmSyncHost(loginAtDomain);
		
		mocksControl.verify();
		
		assertThat(obmSyncHost).isEqualTo(expectedObmSyncHost);
	}

	@Test
	public void testGetObmSyncHostWithEmptyVMArgument() {
		String loginAtDomain = "user@linagora.com";
		String expectedObmSyncHost = "10.69.0.172";
		expect(locatorService.getServiceLocation(Locator.OBM_SYNC_SERVICE, loginAtDomain))
			.andReturn(expectedObmSyncHost);
		setObmSyncVMArgument("");
		
		mocksControl.replay();
		
		String obmSyncHost = locator.getObmSyncHost(loginAtDomain);
		
		mocksControl.verify();
		
		assertThat(obmSyncHost).isEqualTo(expectedObmSyncHost);
	}

	@Test
	public void testGetObmSyncHostWithNullVMArgument() {
		String loginAtDomain = "user@linagora.com";
		String expectedObmSyncHost = "10.69.0.172";
		expect(locatorService.getServiceLocation(Locator.OBM_SYNC_SERVICE, loginAtDomain))
			.andReturn(expectedObmSyncHost);
		
		mocksControl.replay();
		
		String obmSyncHost = locator.getObmSyncHost(loginAtDomain);
		
		mocksControl.verify();
		
		assertThat(obmSyncHost).isEqualTo(expectedObmSyncHost);
	}
	
	private void setObmSyncVMArgument(String obmSyncHost) {
		System.setProperty(Locator.OBM_SYNC_HOST, obmSyncHost);
	}

	@Test
	public void testGetObmSyncBaseUrl() {
		String loginAtDomain = "user@linagora.com";
		String obmSyncHost = "10.69.0.172";
		expect(locatorService.getServiceLocation(Locator.OBM_SYNC_SERVICE, loginAtDomain))
			.andReturn(obmSyncHost);
		String expectBaseUrl = "http://" + obmSyncHost + ":8080";
		expect(domainConfiguration.getObmSyncBaseUrl(obmSyncHost))
			.andReturn(expectBaseUrl);
		
		mocksControl.replay();
		
		String baseUrl = locator.backendBaseUrl(loginAtDomain);
		
		mocksControl.verify();
		
		assertThat(baseUrl).isEqualTo(expectBaseUrl);
	}
}
