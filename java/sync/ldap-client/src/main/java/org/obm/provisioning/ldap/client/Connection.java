/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013 Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.provisioning.ldap.client;

import java.util.List;

import org.obm.provisioning.ldap.client.bean.LdapDomain;
import org.obm.provisioning.ldap.client.bean.LdapGroup;
import org.obm.provisioning.ldap.client.bean.LdapUser;
import org.obm.provisioning.ldap.client.bean.LdapUserMembership;
import org.obm.provisioning.ldap.client.exception.ConnectionException;
import org.obm.provisioning.ldap.client.exception.LdapException;

public interface Connection {

	public interface Factory {
		public Connection create();
	}

	void createUser(LdapUser ldapUser) throws LdapException, ConnectionException;
	
	void deleteUser(LdapUser.Uid ldapUser, LdapDomain domain) throws LdapException, ConnectionException;
	
	void createGroup(LdapGroup ldapGroup) throws LdapException, ConnectionException;
	
	void deleteGroup(LdapGroup.Cn ldapGroup) throws LdapException, ConnectionException;
	
	void addUserToGroup(LdapUserMembership ldapUserMembership, LdapGroup.Cn ldapGroupCn) throws LdapException, ConnectionException;
	
	void removeUserFromGroup(LdapUserMembership ldapUserMembership, LdapGroup.Cn ldapGroupCn) throws LdapException, ConnectionException;

	void addUsersToGroup(List<LdapUserMembership> ldapUserMemberships, LdapGroup.Cn ldapGroupCn) throws LdapException, ConnectionException;
	
	void removeUsersFromGroup(List<LdapUserMembership> ldapUserMemberships, LdapGroup.Cn ldapGroupCn) throws LdapException, ConnectionException;

	void addGroupToGroup(LdapGroup.Cn ldapGroupCn, LdapGroup.Cn toLdapGroupCn) throws LdapException, ConnectionException;
	
	void removeGroupFromGroup(LdapGroup.Cn ldapGroupCn, LdapGroup.Cn fromLdapGroupCn) throws LdapException, ConnectionException;

	void addGroupsToGroup(List<LdapGroup.Cn> ldapGroupCns, LdapGroup.Cn toLdapGroupCn) throws LdapException, ConnectionException;
	
	void removeGroupsFromGroup(List<LdapGroup.Cn> ldapGroupCns, LdapGroup.Cn fromLdapGroupCn) throws LdapException, ConnectionException;
	
	void shutdown() throws ConnectionException;
	
}
