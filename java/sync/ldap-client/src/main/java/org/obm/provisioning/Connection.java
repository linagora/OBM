/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
