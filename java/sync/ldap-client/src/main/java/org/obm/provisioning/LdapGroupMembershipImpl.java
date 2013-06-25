package org.obm.provisioning;

import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.name.Dn;

public class LdapGroupMembershipImpl implements LdapGroupMembership {

	@Override
	public String getMemberUid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dn getMember() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMailBox() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Modification[] buildAddModifications() {
		return buildModifications(ModificationOperation.ADD_ATTRIBUTE);
	}

	@Override
	public Modification[] buildRemoveModifications() {
		return buildModifications(ModificationOperation.REMOVE_ATTRIBUTE);
	}

	private Modification[] buildModifications(ModificationOperation operation) {
		return new Modification[] {
				new DefaultModification(operation, "memberUid", getMemberUid()),
				new DefaultModification(operation, "member", getMember().getName()),
				new DefaultModification(operation, "mailBox", getMailBox())
		};
	}

}
