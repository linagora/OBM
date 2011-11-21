package org.obm.locator.store;

import org.obm.locator.LocatorClientException;

public interface LocatorService {
	
	String getServiceLocation(String serviceSlashProperty, String loginAtDomain) throws LocatorClientException;

}
