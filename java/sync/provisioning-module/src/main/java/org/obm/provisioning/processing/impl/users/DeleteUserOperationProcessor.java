/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2013  Linagora
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
package org.obm.provisioning.processing.impl.users;

import static fr.aliacom.obm.common.system.ObmSystemUser.CYRUS;

import org.obm.annotations.transactional.Transactional;
import org.obm.cyrus.imap.admin.CyrusManager;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.beans.Request;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.ldap.client.LdapManager;
import org.obm.push.mail.bean.Acl;

import com.google.inject.Inject;

import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

public class DeleteUserOperationProcessor extends AbstractUserOperationProcessor {
	
	private final static String DELETE_ACL = "lc";

	@Inject
	DeleteUserOperationProcessor() {
		super(HttpVerb.DELETE);
	}

	@Override
	@Transactional
	public void process(Operation operation, Batch batch) throws ProcessingException {
		final UserExtId extId = getUserExtIdFromRequest(operation);
		final Request request = operation.getRequest();
		final boolean expunge = Boolean.valueOf(request.getParams().get(Request.EXPUNGE_KEY));
		final ObmUser userFromDao = getUserFromDao(extId, batch.getDomain());

		if (expunge == true) {
			deleteUserInDao(userFromDao);
			deleteUserMailBoxes(userFromDao);
		}
		else {
			archiveUserInDao(userFromDao);
		}
		deleteUserInLdap(userFromDao);
	}

	private void archiveUserInDao(ObmUser user) {
		try {
			userDao.archive(user);
		} catch (Exception e) {
			throw new ProcessingException(String.format("Cannot archive user '%s' (%s) in database", user.getLogin(), user.getExtId().getExtId()), e);
		}
	}

	private void deleteUserInLdap(ObmUser user) {
		LdapManager ldapManager = buildLdapManager(user.getDomain());
		
		try {
			ldapManager.deleteUser(user);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot delete user '%s' (%s) in LDAP.", user.getLogin(), user.getExtId().getExtId()), e);
		} finally {
			ldapManager.shutdown();
		}
	}

	private void deleteUserInDao(ObmUser user) {
		try {
			userDao.delete(user);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot delete user with extId '%s' (%s) in database.", user.getExtId().getExtId(), user.getLogin()), e);
		}
	}

	private void deleteUserMailBoxes(ObmUser user) {
		CyrusManager cyrusManager = null;
		
		try {
			cyrusManager = buildCyrusManager(user);
			cyrusManager.setAcl(user, CYRUS, Acl.builder().user(user.getLogin()).rights(DELETE_ACL).build());
			cyrusManager.delete(user);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format(
							"Cannot delete cyrus mailbox for user '%s' (%s).",
							user.getLogin(), user.getExtId().getExtId()), e);
		} finally {
			if (cyrusManager != null) {
				cyrusManager.shutdown();
			}
		}
	}

}
