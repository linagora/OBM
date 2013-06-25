package org.obm.provisioning;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;

public interface LdapGroup {

	public interface Id {
		String get();
	}
	
	String[] getObjectClasses();
	String getCn();
	int getGidNumber();
	String getMailAccess();
	String getMail();
	String getObmDomain();
	
	Entry buildEntry() throws LdapException;

}
