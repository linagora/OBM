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
package org.obm.locator.store;

import java.util.concurrent.TimeUnit;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.LocatorConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.locator.LocatorClientImpl;
import org.slf4j.Logger;

public class LocatorCacheTest {

	private String loginAtDomain;
	private Logger logger;

	@Before
	public void setUp() {
		loginAtDomain = "test@test.obm.lng";
		logger = EasyMock.createNiceMock(Logger.class);
	}
	
	@Test
	public void testGetServiceLocation() throws LocatorClientException {
		String serviceSlashProperty = "obm-sync";
		String returnValue = "127.0.0.1";

		LocatorConfiguration configurationService = mockLocatorCacheWithExpiration(30, TimeUnit.MINUTES);
		LocatorClientImpl locatorClientImpl = mockLocatorServiceGivenVariousValues(serviceSlashProperty, returnValue);
		
		EasyMock.replay(configurationService, locatorClientImpl);
		
		LocatorCache locatorCache = new LocatorCache(configurationService, locatorClientImpl, logger);
		String value = locatorCache.getServiceLocation(serviceSlashProperty, loginAtDomain);
		
		EasyMock.verify(configurationService, locatorClientImpl);
		
		Assert.assertEquals(returnValue, value);
	}
	
	@Test(expected=LocatorClientException.class)
	public void testGetServiceLocationReturnNullValue() throws LocatorClientException {
		String service = "obm-sync";
		String returnNullValue = null;

		LocatorConfiguration configurationService = mockLocatorCacheWithExpiration(30, TimeUnit.MINUTES);
		LocatorClientImpl locatorClientImpl = mockLocatorServiceGivenVariousValues(service, returnNullValue);
		
		EasyMock.replay(configurationService, locatorClientImpl);
		
		LocatorCache locatorCache = new LocatorCache(configurationService, locatorClientImpl, logger);
		locatorCache.getServiceLocation(service, loginAtDomain);
		
		EasyMock.verify(configurationService, locatorClientImpl);
		
		Assert.assertTrue(false);
	}
	
	@Test(expected=LocatorClientException.class)
	public void testExceptionIsTriggeredWhenLoadingValueInCache() throws LocatorClientException, InterruptedException {
		String obmSyncService = "obm-sync";

		LocatorConfiguration configurationService = mockLocatorCacheWithExpiration(3, TimeUnit.SECONDS);
		LocatorClientImpl locatorClientImpl = mockLocatorServiceGivenVariousValuesThenException(
				obmSyncService, new LocatorClientException("No message"), "first value");
		
		EasyMock.replay(configurationService, locatorClientImpl);
		
		LocatorCache locatorCache = new LocatorCache(configurationService, locatorClientImpl, logger);
		locatorCache.getServiceLocation(obmSyncService, loginAtDomain); // load value
		locatorCache.getServiceLocation(obmSyncService, loginAtDomain); // get value from cache
		Thread.sleep(5000);
		locatorCache.getServiceLocation(obmSyncService, loginAtDomain); // exception is thrown
	}
	
	@Test
	public void testGetServiceLocationWithNullParameters() throws LocatorClientException {
		loginAtDomain = null;
		String service = null;
		String returnValue = "return value";

		LocatorConfiguration configurationService = mockLocatorCacheWithExpiration(30, TimeUnit.MINUTES);
		LocatorClientImpl locatorClientImpl = mockLocatorServiceGivenVariousValues(service, returnValue);
		
		EasyMock.replay(configurationService, locatorClientImpl);
		
		LocatorCache locatorCache = new LocatorCache(configurationService, locatorClientImpl, logger);
		String value = locatorCache.getServiceLocation(service, loginAtDomain);
		
		EasyMock.verify(configurationService, locatorClientImpl);
		
		Assert.assertEquals(returnValue, value);
	}
	
	@Test
	public void testSeveralGetServiceLocationCall() throws LocatorClientException {
		String obmSyncService = "obm-sync";
		String returnObmSyncValue = "localhost obm-sync";

		String opushService = "opush";
		String returnOpushValue = "localhost opush";

		LocatorConfiguration configurationService = mockLocatorCacheWithExpiration(30, TimeUnit.MINUTES);
		LocatorClientImpl locatorClientImpl = EasyMock.createStrictMock(LocatorClientImpl.class);
		EasyMock.expect(locatorClientImpl.getServiceLocation(obmSyncService, loginAtDomain)).andReturn(returnObmSyncValue);
		EasyMock.expect(locatorClientImpl.getServiceLocation(opushService, loginAtDomain)).andReturn(returnOpushValue);
		
		EasyMock.replay(configurationService, locatorClientImpl);
		
		LocatorCache locatorCache = new LocatorCache(configurationService, locatorClientImpl, logger);
		String obmSyncValue = locatorCache.getServiceLocation(obmSyncService, loginAtDomain);
		String obmSyncValueCache = locatorCache.getServiceLocation(obmSyncService, loginAtDomain);
		String opushValue = locatorCache.getServiceLocation(opushService, loginAtDomain);
		
		EasyMock.verify(configurationService, locatorClientImpl);
		
		Assert.assertEquals(returnObmSyncValue, obmSyncValue);
		Assert.assertEquals(returnObmSyncValue, obmSyncValueCache);
		
		Assert.assertEquals(returnOpushValue, opushValue);
	}
	
	@Test
	public void testCacheExpireWithRegularKeys() throws LocatorClientException, InterruptedException {
		String obmSyncService = "obm-sync";
		
		assertCacheExpireWithServiceKey(obmSyncService); 
	}

	@Test
	public void testCacheExpireWithNullLoginAtDomainKey() throws LocatorClientException, InterruptedException {
		loginAtDomain = null;
		String serviceKey = "obm-sync";
		
		assertCacheExpireWithServiceKey(serviceKey); 
	}
	
	@Test
	public void testCacheExpireWithNullServiceKey() throws LocatorClientException, InterruptedException {
		String nullServiceKey = null;
		
		assertCacheExpireWithServiceKey(nullServiceKey); 
	}
	
	@Test
	public void testCacheExpireWithNullKeys() throws LocatorClientException, InterruptedException {
		loginAtDomain = null;
		String nullServiceKey = null;
		
		assertCacheExpireWithServiceKey(nullServiceKey);
	}
	
	private void assertCacheExpireWithServiceKey(String serviceKey) throws InterruptedException {
		String firstServiceValue = "first localhost obm-sync";
		String secondServiceValue = "second localhost obm-sync";

		assertCacheExpireWithServiceKey(serviceKey, firstServiceValue, secondServiceValue);
	}

	private void assertCacheExpireWithServiceKey(String serviceKey, String firstServiceValue, String secondServiceValue)
			throws InterruptedException {
		
		LocatorConfiguration configurationService = mockLocatorCacheWithExpiration(3, TimeUnit.SECONDS);
		LocatorClientImpl locatorClientImpl = mockLocatorServiceGivenVariousValues(
				serviceKey, firstServiceValue, secondServiceValue);
		
		EasyMock.replay(configurationService, locatorClientImpl);
		
		LocatorCache locatorCache = new LocatorCache(configurationService, locatorClientImpl, logger);
		String firstValue = locatorCache.getServiceLocation(serviceKey, loginAtDomain);
		String firstValueCache = locatorCache.getServiceLocation(serviceKey, loginAtDomain);
		Thread.sleep(5000);
		String refreshedValue = locatorCache.getServiceLocation(serviceKey, loginAtDomain);
		
		EasyMock.verify(configurationService, locatorClientImpl);
		
		Assert.assertEquals(firstServiceValue, firstValue);
		Assert.assertEquals(firstServiceValue, firstValueCache);
		Assert.assertEquals(secondServiceValue, refreshedValue);
	}

	private LocatorClientImpl mockLocatorServiceGivenVariousValues(String serviceKey, String... orderedReturnedValues) {
		LocatorClientImpl locatorClientImpl = EasyMock.createStrictMock(LocatorClientImpl.class);
		for (String returnedValue : orderedReturnedValues) {
			EasyMock.expect(locatorClientImpl.getServiceLocation(serviceKey, loginAtDomain)).andReturn(returnedValue);
		}
		return locatorClientImpl;
	}

	private LocatorClientImpl mockLocatorServiceGivenVariousValuesThenException(
			String serviceKey, Throwable exception, String... orderedReturnedValues) {
		
		LocatorClientImpl locatorClientImpl = mockLocatorServiceGivenVariousValues(serviceKey, orderedReturnedValues);
		EasyMock.expect(locatorClientImpl.getServiceLocation(serviceKey, loginAtDomain)).andThrow(exception);
		return locatorClientImpl;
	}

	private LocatorConfiguration mockLocatorCacheWithExpiration(int expiration, TimeUnit unit) {
		LocatorConfiguration configurationService = EasyMock.createStrictMock(LocatorConfiguration.class);
		EasyMock.expect(configurationService.getLocatorCacheTimeout()).andReturn(expiration);
		EasyMock.expect(configurationService.getLocatorCacheTimeUnit()).andReturn(unit);
		return configurationService;
	}
}
