package org.obm.provisioning;

import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;

public interface Configuration {

	/**
	 * @return the max number of requests before creating a new underlying connection to the LDAP server
	 */
	int maxRequests();
	LdapConnectionConfig getNetworkConfiguration();
	Dn getBindDn();
	String getBindPassword();

	Dn getUserBaseDn();
	String buildUserFilter(LdapUser.Id userId);
	SearchScope getUserSearchScope();

	Dn getGroupBaseDn();
	String buildGroupFilter(LdapGroup.Id groupId);
	SearchScope getGroupSearchScope();

}
