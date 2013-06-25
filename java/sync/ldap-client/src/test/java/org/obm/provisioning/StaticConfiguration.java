package org.obm.provisioning;

import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;

import com.google.common.base.Throwables;

public class StaticConfiguration implements Configuration {

	@Override
	public int maxRequests() {
		return 0;
	}

	@Override
	public LdapConnectionConfig getNetworkConfiguration() {
		LdapConnectionConfig config = new LdapConnectionConfig();
		config.setLdapHost("localhost");
		config.setLdapPort(33389);
		config.setUseSsl(false);
		return config;
	}

	@Override
	public Dn getBindDn() {
		try {
			return new Dn("cn=directory manager");
		} catch (LdapInvalidDnException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public String getBindPassword() {
		return "secret";
	}

	@Override
	public Dn getUserBaseDn() {
		try {
			return new Dn("ou=users,dc=test.obm.org,dc=local");
		} catch (LdapInvalidDnException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public String buildUserFilter(LdapUser.Id userId) {
		return "(uid=" + userId + ")";
	}

	@Override
	public SearchScope getUserSearchScope() {
		return SearchScope.ONELEVEL;
	}

	@Override
	public Dn getGroupBaseDn() {
		try {
			return new Dn("ou=groups,dc=test.obm.org,dc=local");
		} catch (LdapInvalidDnException e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public String buildGroupFilter(LdapGroup.Id groupId) {
		return "(cn=" + groupId.get() + ")";
	}

	@Override
	public SearchScope getGroupSearchScope() {
		return SearchScope.ONELEVEL;
	}

}
