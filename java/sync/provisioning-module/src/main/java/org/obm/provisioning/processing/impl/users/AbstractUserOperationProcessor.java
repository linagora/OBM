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
package org.obm.provisioning.processing.impl.users;

import static fr.aliacom.obm.common.system.ObmSystemUser.CYRUS;

import java.sql.SQLException;
import java.util.Set;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.obm.cyrus.imap.admin.CyrusImapService;
import org.obm.cyrus.imap.admin.CyrusManager;
import org.obm.domain.dao.PUserDao;
import org.obm.domain.dao.UserSystemDao;
import org.obm.provisioning.Group;
import org.obm.provisioning.ProvisioningService;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.SystemUserNotFoundException;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.json.ObmUserJsonDeserializer;
import org.obm.provisioning.ldap.client.LdapManager;
import org.obm.provisioning.mailchooser.ImapBackendChooserSelector;
import org.obm.provisioning.processing.impl.AbstractOperationProcessor;
import org.obm.push.mail.imap.IMAPException;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.inject.Inject;
import com.google.inject.util.Providers;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.system.ObmSystemUser;
import fr.aliacom.obm.common.user.ObmUser;

public abstract class AbstractUserOperationProcessor extends AbstractOperationProcessor {

	@Inject
	protected CyrusImapService cyrusService;
	@Inject
	protected UserSystemDao userSystemDao;
	@Inject
	protected PUserDao pUserDao;
	@Inject
	protected ImapBackendChooserSelector imapBackendChooserSelector;
	private ObmSystemUser cyrusUserSystem = null;
	
	protected AbstractUserOperationProcessor(HttpVerb verb) {
		super(BatchEntityType.USER, verb);
	}

	protected ObmSystemUser findCyrusUser() throws ProcessingException {
		if (this.cyrusUserSystem == null) {
			try {
				this.cyrusUserSystem = userSystemDao.getByLogin(CYRUS);
			} catch (SystemUserNotFoundException e) {
				throw new ProcessingException(e);
			} catch (DaoException e) {
				throw new ProcessingException(e);
			}
		}
		return this.cyrusUserSystem;
	}
	
	protected CyrusManager buildCyrusManager(ObmUser user) throws IMAPException {
		ObmSystemUser cyrusUserSystem = this.findCyrusUser();

		return cyrusService.buildManager(user.getMailHost().getIp(), cyrusUserSystem.getLogin(), cyrusUserSystem.getPassword());
	}

	protected ObmUser inheritDatabaseIdentifiers(ObmUser user, ObmUser existingUser) {
		return ObmUser.builder().from(user).uid(existingUser.getUid()).entityId(existingUser.getEntityId()).build();
	}

	protected ObmUser getUserFromRequestBody(Operation operation, ObjectMapper objectMapper) {
		String requestBody = operation.getRequest().getBody();

		try {
			return objectMapper.readValue(requestBody, ObmUser.class);
		}
		catch (Exception e) {
			throw new ProcessingException(String.format("Cannot parse ObmUser object from request body %s.", requestBody), e);
		}
	}
	
	protected void validateUserEmail(ObmUser user) {
		if (!user.isEmailAvailable()) {
			return;
		}
		try {
			final ImmutableSet<String> userEmails = ImmutableSet.<String>builder()
					.add(user.getEmail())
					.addAll(user.getEmailAlias())
					.build();
			final Set<String> allEmails = userDao.getAllEmailsFrom(user.getDomain(), user.getExtId());
			
			final SetView<String> matchingEmails = Sets.intersection(allEmails, userEmails);
			if(!matchingEmails.isEmpty()) {
				throw new ProcessingException(
						String.format("Cannot create/modify user because similar emails have been found : %s", matchingEmails));
			}
		} catch (SQLException e) {
			throw new ProcessingException(e);
		}
	}

	protected void createUserInLdap(ObmUser user, Group defaultGroup) {
		insertUserInLdap(user, defaultGroup, false);
	}

	protected void createUserInLdapAndAddUserToExistingGroups(ObmUser user, Group defaultGroup) {
		insertUserInLdap(user, defaultGroup, true);
	}

	private void insertUserInLdap(ObmUser user, Group defaultGroup, boolean addUserToExistingGroups) {
		LdapManager ldapManager = buildLdapManager(user.getDomain());

		try {
			ldapManager.createUser(user);
			ldapManager.addUserToGroup(user.getDomain(), defaultGroup, user);

			if (addUserToExistingGroups) {
				addUserToGroupsExceptDefaultOneInLdap(ldapManager, defaultGroup, user);
			}
		} catch (Exception e) {
			throw new ProcessingException(String.format("Cannot insert new user '%s' (%s) in LDAP.", user.getLogin(), user.getExtId()), e);
		} finally {
			ldapManager.shutdown();
		}
	}

	private void addUserToGroupsExceptDefaultOneInLdap(LdapManager ldapManager, Group defaultGroup, ObmUser user) throws SQLException {
		Set<Group> groups = groupDao.getAllPublicGroupsForUserExtId(user.getDomain(), user.getExtId());

		for (Group group: groups) {
			if (!group.equals(defaultGroup)) {
				ldapManager.addUserToGroup(user.getDomain(), group, user);
			}
		}
	}

	protected void deleteUserInLdap(ObmUser user) {
		LdapManager ldapManager = buildLdapManager(user.getDomain());

		try {
			Group defaultGroup = getDefaultGroup(user.getDomain());

			ldapManager.removeUserFromGroup(user.getDomain(), defaultGroup, user);
			deleteUserFromGroupsExceptDefaultOneInLdap(ldapManager, defaultGroup, user);
			ldapManager.deleteUser(user);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot delete user '%s' (%s) in LDAP.", user.getLogin(), user.getExtId().getExtId()), e);
		} finally {
			ldapManager.shutdown();
		}
	}

	protected void deleteUserFromGroupsExceptDefaultOneInLdap(LdapManager ldapManager, Group defaultGroup, ObmUser user) throws SQLException {
		Set<Group> groups = groupDao.getAllPublicGroupsForUserExtId(user.getDomain(), user.getExtId());

		for (Group group: groups) {
			if (!group.equals(defaultGroup)) {
				ldapManager.removeUserFromGroup(user.getDomain(), group, user);
			}
		}
	}

	protected ObjectMapper getDefaultObjectMapper(ObmDomain domain) {
		SimpleModule module = new SimpleModule("InBatch", new Version(0, 0, 0, null)).addDeserializer(ObmUser.class,
				new ObmUserJsonDeserializer(Providers.of(domain), imapBackendChooserSelector));

		return ProvisioningService.createObjectMapper(module);
	}

}
