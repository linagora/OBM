package org.obm.provisioning;

import java.util.List;

public interface Connection {

	void createUser(LdapUser ldapUser) throws LdapException, ConnectionException;
	
	void deleteUser(LdapUser.Id ldapUser) throws LdapException, ConnectionException;
	
	void createGroup(LdapGroup ldapGroup) throws LdapException, ConnectionException;
	
	void deleteGroup(LdapGroup.Id ldapGroup) throws LdapException, ConnectionException;
	
	void addUserToGroup(LdapUserMembership ldapUserMembership, LdapGroup.Id ldapGroupId) throws LdapException, ConnectionException;
	
	void removeUserFromGroup(LdapUserMembership ldapUserMembership, LdapGroup.Id ldapGroupId) throws LdapException, ConnectionException;

	void addUsersToGroup(List<LdapUserMembership> ldapUserMemberships, LdapGroup.Id ldapGroupId) throws LdapException, ConnectionException;
	
	void removeUsersFromGroup(List<LdapUserMembership> ldapUserMemberships, LdapGroup.Id ldapGroupId) throws LdapException, ConnectionException;

	void addGroupToGroup(LdapGroupMembership ldapGroupMembership, LdapGroup.Id ldapGroupId) throws LdapException, ConnectionException;
	
	void removeGroupFromGroup(LdapGroupMembership ldapGroupMembership, LdapGroup.Id ldapGroupId) throws LdapException, ConnectionException;

	void addGroupsToGroup(List<LdapGroupMembership> ldapGroupMemberships, LdapGroup.Id ldapGroupId) throws LdapException, ConnectionException;
	
	void removeGroupsFromGroup(List<LdapGroupMembership> ldapGroupMemberships, LdapGroup.Id ldapGroupId) throws LdapException, ConnectionException;
	
	void shutdown() throws ConnectionException;
	
}
