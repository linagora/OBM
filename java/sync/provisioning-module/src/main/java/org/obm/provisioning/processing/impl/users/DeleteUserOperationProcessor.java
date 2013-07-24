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

import java.sql.SQLException;

import org.obm.annotations.transactional.Transactional;
import org.obm.cyrus.imap.admin.CyrusImapService;
import org.obm.cyrus.imap.admin.CyrusManager;
import org.obm.domain.dao.UserDao;
import org.obm.domain.dao.UserSystemDao;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.beans.Request;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.ldap.client.LdapManager;
import org.obm.provisioning.ldap.client.LdapService;
import org.obm.provisioning.processing.impl.HttpVerbBasedOperationProcessor;
import org.obm.push.mail.bean.Acl;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import fr.aliacom.obm.common.system.ObmSystemUser;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

public class DeleteUserOperationProcessor extends HttpVerbBasedOperationProcessor {
	
	private final static String DELETE_ACL = "lc";
	
	private final UserDao userDao;
	private final CyrusImapService cyrusService;
	private final LdapService ldapService;
	private final UserSystemDao userSystemDao;

	@Inject
	public DeleteUserOperationProcessor(UserDao userDao, LdapService ldapService, CyrusImapService cyrusService, UserSystemDao userSystemDao) {
		super(BatchEntityType.USER, HttpVerb.DELETE);

		this.userDao = userDao;
		this.cyrusService = cyrusService;
		this.ldapService = ldapService;
		this.userSystemDao = userSystemDao;
	}

	@Override
	@Transactional
	public void process(Operation operation, Batch batch) throws ProcessingException {
		final UserExtId extId = getExtIdFromRequestParams(operation);
		final Request request = operation.getRequest();
		final boolean expunge = Boolean.valueOf(request.getParams().get(Request.EXPUNGE_KEY));
		final ObmUser userFromDao = getUserFromDao(batch, extId);
		
		deleteUserInDao(extId);
		if (expunge == true) {
			deleteUserMailBoxes(userFromDao);
		}
		deleteUserInLdap(userFromDao);
	}

	private void deleteUserInLdap(ObmUser user) {
		LdapManager ldapManager = ldapService.buildManager();
		
		try {
			ldapManager.deleteUser(user);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot delete new user '%s' (%s) in LDAP.", user.getLogin(), user.getExtId()), e);
		} finally {
			ldapManager.shutdown();
		}
	}

	private ObmUser getUserFromDao(Batch batch, final UserExtId extId) {
		try {
			return userDao.getByExtId(extId, batch.getDomain());
		} catch (SQLException e) {
			throw new ProcessingException(e);
		} catch (UserNotFoundException e) {
			throw new ProcessingException(e);
		}
	}

	private void deleteUserInDao(UserExtId extId) {
		try {
			userDao.delete(extId);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot delete user with extId '%s' in database.", extId.getExtId()), e);
		}
	}

	private UserExtId getExtIdFromRequestParams(Operation operation) {
		final Request request = operation.getRequest();
		final String itemId = request.getParams().get(Request.ITEM_ID_KEY);
		UserExtId extId  = UserExtId.valueOf(itemId);
		
		if (Strings.isNullOrEmpty(extId.getExtId())) {
			throw new ProcessingException(
					String.format("Cannot get extId parameter from request url %s.", request.getUrl()));
		}
		
		return extId;
	}
	
	private void deleteUserMailBoxes(ObmUser user) {
		CyrusManager cyrusManager = null;
		
		try {
			ObmSystemUser cyrusUserSystem = userSystemDao.getByLogin(CYRUS);
			cyrusManager = cyrusService.buildManager(
					user.getMailHost().getIp(), cyrusUserSystem.getLogin(), cyrusUserSystem.getPassword());
			cyrusManager.setAcl(user, CYRUS, Acl.builder().user(user.getLogin()).rights(DELETE_ACL).build());
			cyrusManager.delete(user);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format(
							"Cannot delete cyrus mailbox for user '%s' (%s).",
							user.getLogin(), user.getExtId()), e);
		} finally {
			if (cyrusManager != null) {
				cyrusManager.shutdown();
			}
		}
	}

}
