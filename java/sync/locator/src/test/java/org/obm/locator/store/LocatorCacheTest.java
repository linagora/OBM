package org.obm.locator.store;

import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obm.configuration.ObmConfigurationService;
import org.obm.locator.LocatorClientException;
import org.obm.locator.LocatorClientImpl;
import org.obm.locator.store.LocatorCache;

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
