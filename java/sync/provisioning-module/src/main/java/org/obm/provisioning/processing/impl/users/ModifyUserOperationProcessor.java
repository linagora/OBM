/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2013  Linagora
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

import org.obm.annotations.transactional.Transactional;
import org.obm.cyrus.imap.admin.CyrusManager;
import org.obm.domain.dao.PUserDao;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.ldap.client.LdapManager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.inject.Inject;

import fr.aliacom.obm.common.user.ObmUser;

public class ModifyUserOperationProcessor extends AbstractUserOperationProcessor {
	@Inject
	private PUserDao pUserDao;

	@Inject
	ModifyUserOperationProcessor() {
		super(HttpVerb.PUT);
	}
	
	ModifyUserOperationProcessor(HttpVerb verb) {
		super(verb);
	}

	@VisibleForTesting void validateUserChanges(ObmUser modifiedUser, ObmUser existingUser) {

		if (!Objects.equal(modifiedUser.getMailHost(), existingUser.getMailHost())) {
			throw new ProcessingException("Cannot change user mail host");
		}
		if (!Objects.equal(modifiedUser.getLogin(), existingUser.getLogin())) {
			throw new ProcessingException("Cannot change user login");
		}
		if (modifiedUser.isArchived() != existingUser.isArchived()) {
			throw new ProcessingException("Cannot change user archived state");
		}

	}

	@Override
	@Transactional
	public void process(Operation operation, Batch batch) throws ProcessingException {
		ObmUser user = getUserFromRequestBody(operation, batch);
		ObmUser existingUser = getUserFromDao(user.getExtId(), batch.getDomain());
		
		validateUserChanges(user, existingUser);
		
		ObmUser newUser = modifyUserInDao(inheritDatabaseIdentifiers(user, existingUser));

		if (newUser.isEmailAvailable()) {
			updateUserMailbox(newUser);
		}

		modifyUserInLdap(newUser, existingUser);
		updateUserInPTables(newUser);
	}

	protected void updateUserMailbox(ObmUser user) {
		CyrusManager cyrusManager = null;

		try {
			cyrusManager = buildCyrusManager(user);
			cyrusManager.applyQuota(user);
		} catch (Exception e) {
			throw new ProcessingException(String.format("Cannot update Cyrus mailbox for user '%s' (%s).", user.getLogin(), user.getExtId()), e);
		} finally {
			if (cyrusManager != null) {
				cyrusManager.shutdown();
			}
		}
	}

	protected ObmUser modifyUserInDao(ObmUser user) {
		try {
			return userDao.update(user);
		}
		catch (Exception e) {
			throw new ProcessingException(String.format("Cannot modify user '%s' (%s) in database.", user.getLogin(), user.getExtId()), e);
		}
	}

	protected void modifyUserInLdap(ObmUser user, ObmUser oldObmUser) {
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

	protected void updateUserInPTables(ObmUser user) throws ProcessingException {
		try {
			pUserDao.delete(user);
			pUserDao.insert(user);
		} catch (DaoException e) {
			throw new ProcessingException(e);
		}
	}
}
