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

import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.obm.provisioning.Group;
import org.obm.provisioning.ldap.client.bean.LdapDomain;
import org.obm.provisioning.ldap.client.bean.LdapGroup;
import org.obm.provisioning.ldap.client.bean.LdapGroup.Cn;
import org.obm.provisioning.ldap.client.bean.LdapUser;
import org.obm.provisioning.ldap.client.bean.LdapUserMembership;
import org.obm.provisioning.ldap.client.exception.ConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

public class LdapManagerImpl implements LdapManager {

	private Connection conn;
	private Provider<LdapUser.Builder> userBuilderProvider;
	private Provider<LdapGroup.Builder> groupBuilderProvider;
	private Provider<LdapUserMembership.Builder> userMembershipBuilderProvider;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Singleton
	public static class Factory implements LdapManager.Factory {
		private Provider<LdapUser.Builder> userBuilderProvider;
		private Provider<LdapGroup.Builder> groupBuilderProvider;
		private Provider<LdapUserMembership.Builder> userMembershipBuilderProvider;
		private Connection.Factory connectionFactory;

		@Inject
		public Factory(Provider<LdapUser.Builder> userBuilderProvider, Provider<LdapGroup.Builder> groupBuilderProvider,
				Provider<LdapUserMembership.Builder> userMembershipBuilderProvider,
				Connection.Factory connectionFactory) {
			this.userBuilderProvider = userBuilderProvider;
			this.userMembershipBuilderProvider = userMembershipBuilderProvider;
			this.groupBuilderProvider = groupBuilderProvider;
			this.connectionFactory = connectionFactory;
		}

		@Override
		public LdapManager create(LdapConnectionConfig connectionConfig) {
			return new LdapManagerImpl(
					connectionFactory.create(connectionConfig),
					userBuilderProvider,
					groupBuilderProvider,
					userMembershipBuilderProvider);
		}

	}

	public LdapManagerImpl(Connection conn, Provider<LdapUser.Builder> userBuilderProvider,
			Provider<LdapGroup.Builder> groupBuilderProvider,
			Provider<LdapUserMembership.Builder> userMembershipBuilderProvider) {
		this.conn = conn;
		this.userBuilderProvider = userBuilderProvider;
		this.groupBuilderProvider = groupBuilderProvider;
		this.userMembershipBuilderProvider = userMembershipBuilderProvider;
	}

	@Override
	public void createUser(ObmUser obmUser) {
		LdapUser ldapUser = userBuilderProvider.get().fromObmUser(obmUser)
				.build();
		conn.createUser(ldapUser);
	}

	@Override
	public void deleteUser(ObmUser obmUser) {
		LdapUser ldapUser = userBuilderProvider.get().fromObmUser(obmUser)
				.build();
		conn.deleteUser(ldapUser.getUid(), ldapUser.getDomain());
	}

	@Override
	public void modifyUser(ObmUser obmUser, ObmUser oldObmUser) {
		LdapUser ldapUser = userBuilderProvider.get().fromObmUser(obmUser).build();
		LdapUser oldLdapUser = userBuilderProvider.get().fromObmUser(oldObmUser).build();
		Modification[] modifications = ldapUser.buildDiffModifications(oldLdapUser);

		if (modifications.length > 0) {
			conn.modifyUser(ldapUser.getUid(), ldapUser.getDomain(), modifications);
		} else {
			logger.info(String.format("LDAP attributes of user %s (%s) weren't changed. Doing nothing.", obmUser.getLogin(), obmUser.getExtId())); 
		}
	}

	@Override
	public void deleteGroup(ObmDomain domain, Group group) {
		conn.deleteGroup(Cn.valueOf(group.getName()), LdapDomain.valueOf(domain.getName()));
	}
	
	@Override
	public void addUserToGroup(ObmDomain domain, Group group, ObmUser user) {
		conn.addUserToGroup(
				userMembershipBuilderProvider.get().fromObmUser(user).build(),
				Cn.valueOf(group.getName()),
				LdapDomain.valueOf(domain.getName()));
	}

	@Override
	public void addUserToDefaultGroup(ObmDomain domain, Group defaultGroup, ObmUser user) {
		conn.addUserToDefaultGroup(
				userMembershipBuilderProvider.get().fromObmUser(user).build(),
				Cn.valueOf(defaultGroup.getName()),
				LdapDomain.valueOf(domain.getName()));
		
	}
	
	@Override
	public void removeUserFromGroup(ObmDomain domain, Group group, ObmUser user) {
		conn.removeUserFromGroup(
				userMembershipBuilderProvider.get().fromObmUser(user).build(),
				Cn.valueOf(group.getName()),
				LdapDomain.valueOf(domain.getName()));
	}

	@Override
	public void removeUserFromDefaultGroup(ObmDomain domain, Group defaultGroup, ObmUser user) {
		conn.removeUserFromDefaultGroup(
				userMembershipBuilderProvider.get().fromObmUser(user).build(),
				Cn.valueOf(defaultGroup.getName()),
				LdapDomain.valueOf(domain.getName()));
	}
	
	@Override
	public void addSubgroupToGroup(ObmDomain domain, Group group, Group subgroup) {
		conn.addGroupToGroup(
				Cn.valueOf(subgroup.getName()),
				Cn.valueOf(group.getName()),
				LdapDomain.valueOf(domain.getName()));
	}
	
	@Override
	public void removeSubgroupFromGroup(ObmDomain domain, Group group, Group subgroup) {
		conn.removeGroupFromGroup(
				Cn.valueOf(subgroup.getName()),
				Cn.valueOf(group.getName()),
				LdapDomain.valueOf(domain.getName()));
	}
	
	@Override
	public void modifyGroup(ObmDomain domain, Group group, Group oldGroup) {
		LdapGroup ldapGroup = groupBuilderProvider.get().fromObmGroup(group, domain).build();
		LdapGroup oldLdapGroup = groupBuilderProvider.get().fromObmGroup(oldGroup, domain).build();
		Modification[] modifications = ldapGroup.buildDiffModifications(oldLdapGroup);

		if (modifications.length > 0) {
			conn.modifyGroup(oldLdapGroup.getCn(), ldapGroup.getDomain(), modifications);
		} else {
			logger.info(String.format(
							"LDAP attributes of group %s (%s) weren't changed. Doing nothing.",
							group.getName(),
							group.getExtId())); 
		}
	}
	
	@Override
	public void createGroup(Group group, ObmDomain domain) {
		LdapGroup ldapGroup = groupBuilderProvider.get().fromObmGroup(group, domain).build();
		conn.createGroup(ldapGroup);
	}

	@Override
	public void shutdown() throws ConnectionException {
		conn.shutdown();
	}
}
