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

import org.codehaus.jackson.map.ObjectMapper;
import org.obm.annotations.transactional.Transactional;
import org.obm.cyrus.imap.admin.CyrusManager;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.ldap.client.LdapManager;
import org.obm.provisioning.processing.impl.OperationUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

public abstract class AbstractModifyUserOperationProcessor extends AbstractUserOperationProcessor {

	private ObmUser existingUser;

	AbstractModifyUserOperationProcessor(HttpVerb verb) {
		super(verb);
	}

	protected abstract ObjectMapper getObjectMapper(ObmDomain domain);

	protected final ObmUser getExistingUser() {
		return existingUser;
	}

	@Override
	@Transactional
	public void process(Operation operation, Batch batch) throws ProcessingException {
		final UserExtId extId = OperationUtils.getUserExtIdFromRequest(operation);
		final ObmDomain domain = batch.getDomain();

		existingUser = getUserFromDao(extId, domain);
		ObmUser user = getUserFromRequestBody(operation, getObjectMapper(domain));

		validateUserChanges(user, existingUser);

		ObmUser newUser = modifyUserInDao(inheritDatabaseIdentifiers(user, existingUser));

		if (newUser.isEmailAvailable()) {
			updateUserMailbox(newUser);
		}

		if (existingUser.isArchived() && !newUser.isArchived()) {
			createUserInLdapAndAddUserToExistingGroups(newUser, getDefaultGroup(domain));
		} else if (!existingUser.isArchived() && newUser.isArchived()) {
			deleteUserInLdap(newUser);
		} else {
			modifyUserInLdap(newUser, existingUser);
		}

		updateUserInPTables(newUser);
	}

	@VisibleForTesting void validateUserChanges(ObmUser modifiedUser, ObmUser existingUser) {

		if (!Objects.equal(modifiedUser.getMailHost(), existingUser.getMailHost())) {
			throw new ProcessingException("Cannot change user mail host");
		}
		if (!Objects.equal(modifiedUser.getLogin(), existingUser.getLogin())) {
			throw new ProcessingException("Cannot change user login");
		}
		if (!Objects.equal(modifiedUser.getEmail(), existingUser.getEmail()) ||
			!Objects.equal(modifiedUser.getEmailAlias(), existingUser.getEmailAlias())) {
			validateUserEmail(modifiedUser);
		}
	}

	protected void updateUserMailbox(ObmUser user) {
		try (CyrusManager cyrusManager = buildCyrusManager(user)) {
			cyrusManager.applyQuota(user);
		} catch (Exception e) {
			throw new ProcessingException(String.format("Cannot update Cyrus mailbox for user '%s' (%s).", user.getLogin(), user.getExtId()), e);
		}
	}

	private ObmUser modifyUserInDao(ObmUser user) {
		try {
			return userDao.update(user);
		}
		catch (Exception e) {
			throw new ProcessingException(String.format("Cannot modify user '%s' (%s) in database.", user.getLogin(), user.getExtId()), e);
		}
	}

	private void modifyUserInLdap(ObmUser user, ObmUser oldObmUser) {
		LdapManager ldapManager = buildLdapManager(user.getDomain());

		try {
			ldapManager.modifyUser(user, oldObmUser);
		}
		catch (Exception e) {
			throw new ProcessingException(String.format("Cannot modify user '%s' (%s) in LDAP.", user.getLogin(), user.getExtId()), e);
		}
		finally {
			ldapManager.shutdown();
		}
	}

	private void updateUserInPTables(ObmUser user) throws ProcessingException {
		try {
			pUserDao.delete(user);
			pUserDao.insert(user);
		} catch (DaoException e) {
			throw new ProcessingException(e);
		}
	}
}