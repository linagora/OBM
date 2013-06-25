package org.obm.provisioning;

public interface Configuration {

	/**
	 * @return the max number of requests before creating a new underlying connection to the LDAP server
	 */
	int maxRequests();
	
}
