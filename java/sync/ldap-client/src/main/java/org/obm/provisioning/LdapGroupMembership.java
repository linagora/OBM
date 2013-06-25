package org.obm.provisioning;

import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.name.Dn;

public interface LdapGroupMembership {

	Modification[] buildAddModifications();
	Modification[] buildRemoveModifications();
	
	String getMemberUid();
	Dn getMember();
	String getMailBox();


}
