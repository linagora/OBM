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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.cursor.Cursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.obm.provisioning.LdapGroup.Id;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ObjectArrays;

public class ConnectionImpl implements Connection {
	
	public static final int NO_LIMIT = 0;
	
	private final Configuration configuration;
	private LdapConnection connection;
	
	private AtomicInteger requestCounter;

	public ConnectionImpl(Configuration configuration) throws org.obm.provisioning.LdapException, ConnectionException {
		this.configuration = configuration;
		initializeConnection();
	}
	
	private void initializeConnection() throws org.obm.provisioning.LdapException, ConnectionException {
		try {
			requestCounter = new AtomicInteger(0);
			connection = new LdapNetworkConnection(configuration.getNetworkConfiguration());
			connection.bind(configuration.getBindDn(), configuration.getBindPassword());
		} catch (LdapException e) {
			throw new org.obm.provisioning.LdapException(e);
		} catch (IOException e) {
			throw new org.obm.provisioning.ConnectionException(e);
		}
	}

	private void incrementAndCheckRequestCounter() {
		if (configuration.maxRequests() == NO_LIMIT) {
			return;
		}
		if (requestCounter.incrementAndGet() >= configuration.maxRequests()) {
			shutdown();
			initializeConnection();
		}
	}

	@Override
	public void createUser(LdapUser ldapUser) throws org.obm.provisioning.LdapException, ConnectionException {
		try {
			createEntry(ldapUser.buildEntry());
		} catch (LdapException e) {
			throw new org.obm.provisioning.LdapException(e);
		}
	}
	
	@Override
	public void createGroup(LdapGroup ldapGroup) throws org.obm.provisioning.LdapException, ConnectionException {
		try {
			createEntry(ldapGroup.buildEntry());
		} catch (LdapException e) {
			throw new org.obm.provisioning.LdapException(e);
		}
	}
	
	private void createEntry(Entry entry) throws org.obm.provisioning.LdapException {
		try {
			connection.add(entry);
			incrementAndCheckRequestCounter();
		} catch (LdapException e) {
			throw new org.obm.provisioning.LdapException(e);
		}
	}
	
	@Override
	public void deleteUser(LdapUser.Id ldapUserId) throws org.obm.provisioning.LdapException, ConnectionException {
		try {
			connection.delete(getUserDnFromUserId(ldapUserId));
			incrementAndCheckRequestCounter();
		} catch (LdapException e) {
			throw new org.obm.provisioning.LdapException(e);
		}
	}
	
	private Dn getUserDnFromUserId(LdapUser.Id ldapUserId) throws LdapException {
		return getDn(configuration.getUserBaseDn(), configuration.buildUserFilter(ldapUserId), configuration.getUserSearchScope());
	}

	@Override
	public void deleteGroup(LdapGroup.Id ldapGroupId) throws org.obm.provisioning.LdapException, ConnectionException {
		try {
			connection.delete(getGroupDnFromGroupId(ldapGroupId));
			incrementAndCheckRequestCounter();
		} catch (LdapException e) {
			throw Throwables.propagate(e);
		}
	}
	
	@VisibleForTesting
	Dn getGroupDnFromGroupId(LdapGroup.Id ldapGroupId) throws LdapException {
		return getDn(configuration.getGroupBaseDn(), configuration.buildGroupFilter(ldapGroupId), configuration.getGroupSearchScope());
	}
	
	private Dn getDn(Dn baseDn, String filter, SearchScope scope) throws LdapException {
		Cursor<Entry> entries = connection.search(baseDn, filter, scope, SchemaConstants.NO_ATTRIBUTE);
		incrementAndCheckRequestCounter();
		ImmutableList<Entry> entriesList = FluentIterable.from(entries).toList();
		Preconditions.checkState(entriesList.size() == 1,
				"Entry has not been found or too many group found for the filter " + filter + " in the base " + baseDn.getName() + " with scope " + scope);
		return entriesList.get(0).getDn();
	}

	@Override
	public void addGroupToGroup(LdapGroupMembership ldapGroupMembership, LdapGroup.Id ldapGroupId) throws org.obm.provisioning.LdapException, ConnectionException {
		modifyGroup(ldapGroupId, ldapGroupMembership.buildAddModifications());
	}
	
	@Override
	public void addGroupsToGroup(List<LdapGroupMembership> ldapGroupMemberships, Id ldapGroupId) throws org.obm.provisioning.LdapException, ConnectionException {
		Modification[] modifications = new Modification[0];
		for (LdapGroupMembership ldapGroupMembership: ldapGroupMemberships) {
			modifications = ObjectArrays.concat(modifications, ldapGroupMembership.buildAddModifications(), Modification.class);
		}
		modifyGroup(ldapGroupId, modifications);
	}

	@Override
	public void addUserToGroup(LdapUserMembership ldapUserMembership, LdapGroup.Id ldapGroupId) throws org.obm.provisioning.LdapException, ConnectionException {
		modifyGroup(ldapGroupId, ldapUserMembership.buildAddModifications());
	}
	
	@Override
	public void addUsersToGroup(List<LdapUserMembership> ldapUserMemberships, LdapGroup.Id ldapGroupId) throws org.obm.provisioning.LdapException, ConnectionException {
		Modification[] modifications = new Modification[0];
		for (LdapUserMembership ldapUserMembership: ldapUserMemberships) {
			modifications = ObjectArrays.concat(modifications, ldapUserMembership.buildAddModifications(), Modification.class);
		}
		modifyGroup(ldapGroupId, modifications);
	}
	
	@Override
	public void removeGroupFromGroup(LdapGroupMembership ldapGroupMembership, LdapGroup.Id ldapGroupId) throws org.obm.provisioning.LdapException, ConnectionException {
		modifyGroup(ldapGroupId, ldapGroupMembership.buildRemoveModifications());
	}
	
	private void modifyGroup(LdapGroup.Id ldapGroupId, Modification[] modifications) throws org.obm.provisioning.LdapException {
		try {
			Dn group = getGroupDnFromGroupId(ldapGroupId);
			connection.modify(group, modifications);
			incrementAndCheckRequestCounter();
		} catch (LdapException e) {
			throw new org.obm.provisioning.LdapException(e);
		}
	}
	
	@Override
	public void removeGroupsFromGroup(List<LdapGroupMembership> ldapGroupMemberships, Id ldapGroupId) throws org.obm.provisioning.LdapException, ConnectionException {
		Modification[] modifications = new Modification[0];
		for (LdapGroupMembership ldapGroupMembership: ldapGroupMemberships) {
			modifications = ObjectArrays.concat(modifications, ldapGroupMembership.buildRemoveModifications(), Modification.class);
		}
		modifyGroup(ldapGroupId, modifications);
	}

	@Override
	public void removeUserFromGroup(LdapUserMembership ldapUserMembership, LdapGroup.Id ldapGroupId) throws org.obm.provisioning.LdapException, ConnectionException {
		modifyGroup(ldapGroupId, ldapUserMembership.buildRemoveModifications());
	}

	@Override
	public void removeUsersFromGroup(List<LdapUserMembership> ldapUserMemberships, LdapGroup.Id ldapGroupId) throws org.obm.provisioning.LdapException, ConnectionException {
		Modification[] modifications = new Modification[0];
		for (LdapUserMembership ldapUserMembership: ldapUserMemberships) {
			modifications = ObjectArrays.concat(modifications, ldapUserMembership.buildRemoveModifications(), Modification.class);
		}
		modifyGroup(ldapGroupId, modifications);
	}

	@Override
	public void shutdown() throws ConnectionException {
		try {
			connection.close();
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
	}
}