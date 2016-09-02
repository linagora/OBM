/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.provisioning.processing.impl;

import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.obm.configuration.ConfigurationService;
import org.obm.domain.dao.UserDao;
import org.obm.provisioning.Group;
import org.obm.provisioning.GroupExtId;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.dao.GroupDao;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.ldap.client.LdapManager;
import org.obm.provisioning.ldap.client.LdapService;
import org.obm.provisioning.ldap.client.NoopLdapManagerImpl;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

public abstract class AbstractOperationProcessor extends HttpVerbBasedOperationProcessor {

	@Inject
	protected LdapService ldapService;
	@Inject
	protected UserDao userDao;
	@Inject
	protected GroupDao groupDao;
	@Inject
	private ConfigurationService configurationService;

	protected AbstractOperationProcessor(BatchEntityType entityType, HttpVerb verb) {
		super(entityType, verb);
	}

	protected LdapManager buildLdapManager(ObmDomain domain) {
		return configurationService.isLdapModuleEnabled() ?
				buildRealLdapManager(domain) : NoopLdapManagerImpl.of();
	}
	
	private LdapManager buildRealLdapManager(ObmDomain domain) {
		LdapConnectionConfig connectionConfig = new LdapConnectionConfig();
		ObmHost ldapHost = Iterables.getFirst(domain.getHosts().get(ServiceProperty.LDAP), null);

		if (ldapHost == null) {
			throw new ProcessingException(
					String.format("Domain %s has no linked %s host.",
							domain.getName(),
							ServiceProperty.LDAP));
		}

		connectionConfig.setLdapHost(ldapHost.getIp());
		connectionConfig.setLdapPort(LdapConnectionConfig.DEFAULT_LDAP_PORT);

		return ldapService.buildManager(connectionConfig);
	}

	protected ObmUser getUserFromDao(UserExtId extId, ObmDomain domain) {
		try {
			return userDao.getByExtId(extId, domain);
		}
		catch (Exception e) {
			throw new ProcessingException(String.format("Cannot fetch existing user %s from database.", extId), e);
		}
	}

	protected ObmUser getUserFromDao(String userEmail, ObmDomain domain) throws UserNotFoundException {
		ObmUser user = userDao.findUser(userEmail, domain);
		if (user == null) {
			throw new UserNotFoundException(userEmail, domain.getUuid());
		}
		return user;
	}

	protected Group getExistingGroupFromDao(GroupExtId extId, ObmDomain domain) {
		try {
			return groupDao.get(domain, extId);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot fetch existing group with extId '%s' from database.", extId), e);
		}
	}

	protected Group getDefaultGroup(ObmDomain domain) {
		Group defaultGroup;
		try {
			defaultGroup = groupDao.getByGid(domain, UserDao.DEFAULT_GID);
		} catch (DaoException e) {
			throw new ProcessingException(e);
		}

		if (defaultGroup == null) {
			throw new ProcessingException(
					String.format("Default group with GID %d not found for domain %s.",
					UserDao.DEFAULT_GID, domain.getName()));
		}

		return defaultGroup;
	}

	protected Group getGroupFromDao(Group.Id id) {
		try {
			return groupDao.get(id);
		} catch (Exception e) {
			throw new ProcessingException(String.format("Cannot fetch existing group with id '%s' from database.", id), e);
		}
	}

	protected void addUserToGroupInLdap(ObmDomain domain, Group group, ObmUser userToAdd) {
		LdapManager ldapManager = buildLdapManager(domain);

		try {
			addUserToGroupInLdap(ldapManager, domain, group, userToAdd);
		} finally {
			ldapManager.shutdown();
		}
	}

	protected void addUserToGroupInLdap( LdapManager ldapManager, ObmDomain domain, Group group, ObmUser userToAdd) {
		try {
			ldapManager.addUserToGroup(domain, group, userToAdd);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot add user with extId '%s' to group with extId '%s' in ldap.",
							userToAdd.getExtId().getExtId(), group.getExtId()), e);
		}
	}

	protected void deleteUserFromGroupInLdap(LdapManager ldapManager,ObmDomain domain, Group group, ObmUser userToDelete) {
		try {
			ldapManager.removeUserFromGroup(domain, group, userToDelete);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot delete user with extId '%s' from group with extId '%s' in ldap.",
							userToDelete.getExtId().getExtId(), group.getGid()), e);
		}
	}
}
