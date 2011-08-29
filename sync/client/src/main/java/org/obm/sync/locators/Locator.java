package org.obm.sync.locators;

import javax.naming.ConfigurationException;

import org.obm.configuration.ObmConfigurationService;
import org.obm.locator.LocatorClient;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Locator {

	private final LocatorClient locatorClient;
	private final ObmConfigurationService configurationService;

	@Inject
	private Locator(ObmConfigurationService configurationService) throws ConfigurationException {
		this.configurationService = configurationService;
		this.locatorClient = new LocatorClient(configurationService.getLocatorUrl());
	}
	
	public String backendUrl(String loginAtDomain) {
		String obmSyncHost = getObmSyncHost(loginAtDomain);
		return configurationService.getObmSyncUrl(obmSyncHost);
	}
	
	private String getObmSyncHost(String loginAtDomain) {
		return locatorClient.getServiceLocation("sync/obm_sync", loginAtDomain);
	}
	
}
