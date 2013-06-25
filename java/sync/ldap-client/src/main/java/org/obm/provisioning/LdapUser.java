package org.obm.provisioning;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;

public interface LdapUser {

	public interface Id {

	}

	Entry buildEntry() throws LdapException;

}
