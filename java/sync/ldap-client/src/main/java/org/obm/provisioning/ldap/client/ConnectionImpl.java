/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014 Linagora
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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.DefaultModification;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.name.Rdn;
import org.apache.directory.api.ldap.model.schema.AttributeType;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.obm.provisioning.ldap.client.bean.LdapDomain;
import org.obm.provisioning.ldap.client.bean.LdapGroup;
import org.obm.provisioning.ldap.client.bean.LdapUser;
import org.obm.provisioning.ldap.client.bean.LdapUser.Uid;
import org.obm.provisioning.ldap.client.bean.LdapUserMembership;
import org.obm.provisioning.ldap.client.exception.ConnectionException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import com.google.inject.Inject;
import com.google.inject.Singleton;

public class ConnectionImpl implements Connection {

	@Singleton
	public static class Factory implements Connection.Factory {
		private final Configuration configuration;
		
		@Inject
		private Factory(Configuration configuration) {
			this.configuration = configuration;
		}

		@Override
		public ConnectionImpl create(LdapConnectionConfig connectionConfig) {
			return new ConnectionImpl(configuration, connectionConfig);
		}
	}
	
	public static final int NO_LIMIT = 0;
	private final Configuration configuration;
	
	private LdapConnection connection;
	private final AtomicInteger requestCounter;
	private final LdapConnectionConfig connectionConfig;

	protected ConnectionImpl(Configuration configuration, LdapConnectionConfig connectionConfig) throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		this.configuration = configuration;
		this.connectionConfig = connectionConfig;
		requestCounter = new AtomicInteger();
		initializeConnection();
	}
	
	private void initializeConnection() throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		try {
			requestCounter.set(0);
			connection = new LdapNetworkConnection(connectionConfig);
			connection.bind(configuration.getBindDn(), configuration.getBindPassword().getStringValue());
		} catch (LdapException e) {
			throw new org.obm.provisioning.ldap.client.exception.LdapException(e);
		} catch (IOException e) {
			throw new org.obm.provisioning.ldap.client.exception.ConnectionException(e);
		}
	}

	private void incrementAndCheckRequestCounter() {
		if (configuration.maxRequests() == NO_LIMIT) {
			return;
		}
		if (requestCounter.incrementAndGet() >= configuration.maxRequests()) {
			synchronized (requestCounter) {
				shutdown();
				initializeConnection();
			}
		}
	}

	@Override
	public void createUser(LdapUser ldapUser) throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		try {
			createEntry(ldapUser.buildEntry());
		} catch (LdapException e) {
			throw new org.obm.provisioning.ldap.client.exception.LdapException(e);
		}
	}

	@Override
	public void modifyUser(Uid ldapUserId, LdapDomain domain, Modification... modifications) throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		try {
			connection.modify(getUserDnFromUserId(ldapUserId, domain), modifications);
			incrementAndCheckRequestCounter();
		} catch (LdapException e) {
			throw new org.obm.provisioning.ldap.client.exception.LdapException(e);
		}
	}

	@Override
	public void createGroup(LdapGroup ldapGroup) throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		try {
			createEntry(ldapGroup.buildEntry());
		} catch (LdapException e) {
			throw new org.obm.provisioning.ldap.client.exception.LdapException(e);
		}
	}
	
	@Override
	public void modifyGroup(LdapGroup.Cn ldapGroupCn, LdapDomain domain, Modification... modifications)
			throws org.obm.provisioning.ldap.client.exception.LdapException {
		try {
			connection.modify(getGroupDnFromGroupCn(ldapGroupCn, domain), modifications);
			incrementAndCheckRequestCounter();
		} catch (LdapException e) {
			throw new org.obm.provisioning.ldap.client.exception.LdapException(e);
		}
	}
	
	private void createEntry(Entry entry) throws org.obm.provisioning.ldap.client.exception.LdapException {
		try {
			connection.add(entry);
			incrementAndCheckRequestCounter();
		} catch (LdapException e) {
			throw new org.obm.provisioning.ldap.client.exception.LdapException(e);
		}
	}
	
	@Override
	public void deleteUser(LdapUser.Uid ldapUserId, LdapDomain domain) throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		try {
			connection.delete(getUserDnFromUserId(ldapUserId, domain));
			incrementAndCheckRequestCounter();
		} catch (LdapException e) {
			throw new org.obm.provisioning.ldap.client.exception.LdapException(e);
		}
	}
	
	@VisibleForTesting Dn getUserDnFromUserId(LdapUser.Uid ldapUserId, LdapDomain domain) throws LdapException {
		return new Dn(
				new Rdn(String.format("uid=%s", ldapUserId.get())),
				configuration.getUserBaseDn(domain));
	}

	@Override
	public void deleteGroup(LdapGroup.Cn ldapGroupCn, LdapDomain domain) throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		try {
			connection.delete(getGroupDnFromGroupCn(ldapGroupCn, domain));
			incrementAndCheckRequestCounter();
		} catch (LdapException e) {
			throw new org.obm.provisioning.ldap.client.exception.LdapException(e);
		}
	}
	
	@VisibleForTesting Dn getGroupDnFromGroupCn(LdapGroup.Cn ldapGroupCn, LdapDomain domain) throws LdapException {
		return new Dn(
				new Rdn(String.format("cn=%s", ldapGroupCn.get())),
				configuration.getGroupBaseDn(domain));
	}

	@Override
	public void addUserToGroup(LdapUserMembership ldapUserMembership, LdapGroup.Cn ldapGroupCn, LdapDomain ldapDomain)
			throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		modifyGroup(ldapGroupCn, ldapUserMembership.buildAddModifications(), ldapDomain);
	}

	@Override
	public void addUserToDefaultGroup(LdapUserMembership ldapUserMembership, LdapGroup.Cn ldapGroupCn, LdapDomain ldapDomain)
			throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		modifyGroup(ldapGroupCn, ldapUserMembership.buildAddModificationsForDefaultGroup(), ldapDomain);
	}
	
	@Override
	public void addUsersToGroup(List<LdapUserMembership> ldapUserMemberships, LdapGroup.Cn ldapGroupCn, LdapDomain ldapDomain)
			throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		Modification[] modifications = new Modification[0];
		for (LdapUserMembership ldapUserMembership: ldapUserMemberships) {
			modifications = ObjectArrays.concat(modifications, ldapUserMembership.buildAddModifications(), Modification.class);
		}
		modifyGroup(ldapGroupCn, modifications, ldapDomain);
	}
	
	private void modifyGroup(LdapGroup.Cn ldapGroupCn, Modification[] modifications, LdapDomain ldapDomain) throws org.obm.provisioning.ldap.client.exception.LdapException {
		try {
			Dn group = getGroupDnFromGroupCn(ldapGroupCn, ldapDomain);
			connection.modify(group, modifications);
			incrementAndCheckRequestCounter();
		} catch (LdapException e) {
			throw new org.obm.provisioning.ldap.client.exception.LdapException(e);
		}
	}

	@Override
	public void removeUserFromGroup(LdapUserMembership ldapUserMembership, LdapGroup.Cn ldapGroupCn, LdapDomain ldapDomain)
			throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		modifyGroup(ldapGroupCn, ldapUserMembership.buildRemoveModifications(), ldapDomain);
	}

	@Override
	public void removeUserFromDefaultGroup(LdapUserMembership ldapUserMembership, LdapGroup.Cn ldapGroupCn, LdapDomain ldapDomain)
			throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		modifyGroup(ldapGroupCn, ldapUserMembership.buildRemoveModificationsForDefaultGroup(), ldapDomain);
	}

	@Override
	public void removeUsersFromGroup(List<LdapUserMembership> ldapUserMemberships, LdapGroup.Cn ldapGroupCn, LdapDomain ldapDomain)
			throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		Modification[] modifications = new Modification[0];
		for (LdapUserMembership ldapUserMembership: ldapUserMemberships) {
			modifications = ObjectArrays.concat(modifications, ldapUserMembership.buildRemoveModifications(), Modification.class);
		}
		modifyGroup(ldapGroupCn, modifications, ldapDomain);
	}

	@Override
	public void shutdown() throws ConnectionException {
		try {
			connection.close();
		} catch (IOException e) {
			throw new ConnectionException(e);
		}
	}

	@Override
	public void addGroupToGroup(LdapGroup.Cn ldapGroupCn, LdapGroup.Cn toLdapGroupCn, LdapDomain ldapDomain)
			throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		try {
			Entry entry = getGroupEntry(ldapGroupCn, ldapDomain);
			Entry toEntry = getGroupEntry(toLdapGroupCn, ldapDomain);
			
			modifyGroupByGroup(toEntry, entry, ModificationOperation.ADD_ATTRIBUTE);
		} catch (LdapException e) {
			throw new org.obm.provisioning.ldap.client.exception.LdapException(e);
		}
	}

	@Override
	public void removeGroupFromGroup(LdapGroup.Cn ldapGroupCn, LdapGroup.Cn fromLdapGroupCn, LdapDomain ldapDomain)
			throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		try {
			Entry entry = getGroupEntry(ldapGroupCn, ldapDomain);
			Entry fromEntry = getGroupEntry(fromLdapGroupCn, ldapDomain);
			
			modifyGroupByGroup(fromEntry, entry, ModificationOperation.REMOVE_ATTRIBUTE);
		} catch (LdapException e) {
			throw new org.obm.provisioning.ldap.client.exception.LdapException(e);
		}
	}
	
	private void modifyGroupByGroup(Entry entry, Entry byEntry, ModificationOperation modificationOperation) throws LdapException {
		List<Modification> modifications = memberModificationsFromEntry(byEntry, modificationOperation);
		
		if (modifications.isEmpty()) {
			return;
		}
		
		connection.modify(entry.getDn(), modifications.toArray(new Modification[] {}));
		incrementAndCheckRequestCounter();
	}

	private List<Modification> memberModificationsFromEntry(Entry entry, ModificationOperation modificationOperation) {
		List<Modification> modifications = Lists.newArrayList();
		final Attribute entries = entry.get(new AttributeType("member"));
		
		if (entries == null) {
			return modifications;
		}
		
		for (Value<?> value : entries) {
			modifications.add(new DefaultModification(modificationOperation, "member", value));
		}
		
		return modifications;
	}
	
	@Override
	public void addGroupsToGroup(List<LdapGroup.Cn> ldapGroupCns, LdapGroup.Cn toLdapGroupCn, LdapDomain ldapDomain)
			throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		try {
			modifyMembersFromGroups(ldapGroupCns, toLdapGroupCn, ModificationOperation.ADD_ATTRIBUTE, ldapDomain);
		} catch (LdapException e) {
			throw new org.obm.provisioning.ldap.client.exception.LdapException(e);
		}
	}

	@Override
	public void removeGroupsFromGroup(List<LdapGroup.Cn> ldapGroupCns, LdapGroup.Cn fromLdapGroupCn, LdapDomain ldapDomain)
			throws org.obm.provisioning.ldap.client.exception.LdapException, ConnectionException {
		try {
			modifyMembersFromGroups(ldapGroupCns, fromLdapGroupCn, ModificationOperation.REMOVE_ATTRIBUTE, ldapDomain);
		} catch (LdapException e) {
			throw new org.obm.provisioning.ldap.client.exception.LdapException(e);
		}
	}
	
	private void modifyMembersFromGroups(List<LdapGroup.Cn> ldapGroupCns, LdapGroup.Cn modifiedLdapGroupCn, 
			ModificationOperation modificationOperation, LdapDomain ldapDomain) throws LdapException {
		
		List<Modification> modifications = Lists.newArrayList();
		for (LdapGroup.Cn ldapGroupCn : ldapGroupCns) {
			Entry entry = getGroupEntry(ldapGroupCn, ldapDomain);
			modifications.addAll(memberModificationsFromEntry(entry, modificationOperation));
		}
		
		if (modifications.isEmpty()) {
			return;
		}
		
		Entry fromEntry = getGroupEntry(modifiedLdapGroupCn, ldapDomain);
		connection.modify(fromEntry.getDn(), modifications.toArray(new Modification[] {}));
		incrementAndCheckRequestCounter();
	}

	@VisibleForTesting
	Entry getGroupEntry(LdapGroup.Cn ldapGroupCn, LdapDomain ldapDomain) throws LdapException {
		return connection.lookup(getGroupDnFromGroupCn(ldapGroupCn, ldapDomain));
	}

	@VisibleForTesting
	Entry getUserEntry(LdapUser.Uid ldapUserUid, LdapDomain ldapDomain) throws LdapException {
		return connection.lookup(getUserDnFromUserId(ldapUserUid, ldapDomain));
	}

}