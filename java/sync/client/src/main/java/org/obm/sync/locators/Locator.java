package org.obm.sync.locators;

import org.obm.configuration.ObmConfigurationService;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Locator {

	private final LocatorService locatorService;
	private final ObmConfigurationService configurationService;

	@Inject
	private Locator(ObmConfigurationService configurationService, LocatorService locatorService) {
		this.configurationService = configurationService;
		this.locatorService = locatorService;
	}
	
	public String backendUrl(String loginAtDomain) throws LocatorClientException {
		String obmSyncHost = getObmSyncHost(loginAtDomain);
		return configurationService.getObmSyncUrl(obmSyncHost);
	}
	
	private String getObmSyncHost(String loginAtDomain) throws LocatorClientException {
		return locatorService.getServiceLocation("sync/obm_sync", loginAtDomain);
	}
	
}
