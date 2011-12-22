/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obm.configuration.ObmConfigurationService;
import org.obm.locator.LocatorClientException;
import org.obm.locator.LocatorClientImpl;

public class LocatorCacheTest {

	@Test
	public void testGetServiceLocation() throws LocatorClientException {
		String serviceSlashProperty = "obm-sync";
		String loginAtDomain = "test@test.obm.lng";
		String returnValue = "127.0.0.1";

		ObmConfigurationService configurationService = EasyMock.createStrictMock(ObmConfigurationService.class);
		LocatorClientImpl locatorClientImpl = EasyMock.createStrictMock(LocatorClientImpl.class);
		
		EasyMock.expect(configurationService.getLocatorCacheTimeout()).andReturn(30);
		EasyMock.expect(configurationService.getLocatorCacheTimeUnit()).andReturn(TimeUnit.MINUTES);
		
		EasyMock.expect(locatorClientImpl.getServiceLocation(serviceSlashProperty, loginAtDomain)).andReturn(returnValue);
		
		EasyMock.replay(configurationService, locatorClientImpl);
		
		LocatorCache locatorCache = new LocatorCache(configurationService, locatorClientImpl);
		String value = locatorCache.getServiceLocation(serviceSlashProperty, loginAtDomain);
		
		EasyMock.verify(configurationService, locatorClientImpl);
		
		Assert.assertEquals(returnValue, value);
	}
	
	@Test(expected=LocatorClientException.class)
	public void testGetServiceLocationReturnNullValue() throws LocatorClientException {
		String service = "obm-sync";
		String loginAtDomain = "test@test.obm.lng";
		String returnNullValue = null;

		ObmConfigurationService configurationService = EasyMock.createStrictMock(ObmConfigurationService.class);
		LocatorClientImpl locatorClientImpl = EasyMock.createStrictMock(LocatorClientImpl.class);
		
		EasyMock.expect(configurationService.getLocatorCacheTimeout()).andReturn(30);
		EasyMock.expect(configurationService.getLocatorCacheTimeUnit()).andReturn(TimeUnit.MINUTES);
		
		EasyMock.expect(locatorClientImpl.getServiceLocation(service, loginAtDomain)).andReturn(returnNullValue);
		
		EasyMock.replay(configurationService, locatorClientImpl);
		
		LocatorCache locatorCache = new LocatorCache(configurationService, locatorClientImpl);
		locatorCache.getServiceLocation(service, loginAtDomain);
		
		EasyMock.verify(configurationService, locatorClientImpl);
		
		Assert.assertTrue(false);
	}
	
	@Test
	public void testGetServiceLocationWithNullParameters() throws LocatorClientException {
		String service = null;
		String loginAtDomain = null;
		String returnValue = "return value";

		ObmConfigurationService configurationService = EasyMock.createStrictMock(ObmConfigurationService.class);
		LocatorClientImpl locatorClientImpl = EasyMock.createStrictMock(LocatorClientImpl.class);
		
		EasyMock.expect(configurationService.getLocatorCacheTimeout()).andReturn(30);
		EasyMock.expect(configurationService.getLocatorCacheTimeUnit()).andReturn(TimeUnit.MINUTES);
		
		EasyMock.expect(locatorClientImpl.getServiceLocation(service, loginAtDomain)).andReturn(returnValue);
		
		EasyMock.replay(configurationService, locatorClientImpl);
		
		LocatorCache locatorCache = new LocatorCache(configurationService, locatorClientImpl);
		String value = locatorCache.getServiceLocation(service, loginAtDomain);
		
		EasyMock.verify(configurationService, locatorClientImpl);
		
		Assert.assertEquals(returnValue, value);
	}
	
	@Test
	public void testSeveralGetServiceLocationCall() throws LocatorClientException {
		String loginAtDomain = "test@test.obm.lng";

		String obmSyncService = "obm-sync";
		String returnObmSyncValue = "localhost obm-sync";

		String opushService = "opush";
		String returnOpushValue = "localhost opush";

		ObmConfigurationService configurationService = EasyMock.createStrictMock(ObmConfigurationService.class);
		LocatorClientImpl locatorClientImpl = EasyMock.createStrictMock(LocatorClientImpl.class);
		
		EasyMock.expect(configurationService.getLocatorCacheTimeout()).andReturn(30);
		EasyMock.expect(configurationService.getLocatorCacheTimeUnit()).andReturn(TimeUnit.MINUTES);
		
		EasyMock.expect(locatorClientImpl.getServiceLocation(obmSyncService, loginAtDomain)).andReturn(returnObmSyncValue);
		EasyMock.expect(locatorClientImpl.getServiceLocation(opushService, loginAtDomain)).andReturn(returnOpushValue);
		
		EasyMock.replay(configurationService, locatorClientImpl);
		
		LocatorCache locatorCache = new LocatorCache(configurationService, locatorClientImpl);
		String obmSyncValue = locatorCache.getServiceLocation(obmSyncService, loginAtDomain);
		String obmSyncValueCache = locatorCache.getServiceLocation(obmSyncService, loginAtDomain);
		String opushValue = locatorCache.getServiceLocation(opushService, loginAtDomain);
		
		EasyMock.verify(configurationService, locatorClientImpl);
		
		Assert.assertEquals(returnObmSyncValue, obmSyncValue);
		Assert.assertEquals(returnObmSyncValue, obmSyncValueCache);
		
		Assert.assertEquals(returnOpushValue, opushValue);
	}
	
}
