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
package org.obm.provisioning.processing.impl.groups;

import java.util.Set;

import org.obm.annotations.transactional.Transactional;
import org.obm.provisioning.Group;
import org.obm.provisioning.GroupExtId;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.ldap.client.LdapManager;
import org.obm.provisioning.processing.impl.OperationUtils;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

public class AddUserToGroupOperationProcessor extends AbstractGroupOperationProcessor {

	protected AddUserToGroupOperationProcessor() {
		super(BatchEntityType.USER_MEMBERSHIP, HttpVerb.PUT);
	}

	@Override
	@Transactional
	public void process(Operation operation, Batch batch) throws ProcessingException {
		GroupExtId groupExtId = getGroupExtIdFromRequest(operation);
		ObmDomain domain = batch.getDomain();
		UserExtId userExtId = OperationUtils.getUserExtIdFromRequest(operation);
		ObmUser userFromDao = getUserFromDao(userExtId, domain);

		addUserToGroupInDao(domain, groupExtId, userFromDao);
		Group groupFromDao = getGroupFromDao(groupExtId, domain);
		updateGroupInPTables(groupFromDao);
		addUserToAllParentGroupsInLdap(domain, groupExtId, userFromDao);
	}

	private void addUserToGroupInDao(ObmDomain domain, GroupExtId groupExtId, ObmUser userFromDao) {
		try {
			groupDao.addUser(domain, groupExtId, userFromDao);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot add user with extId '%s' to group with extId '%s' in database.",
							userFromDao.getExtId().getExtId(), groupExtId.getId()), e);
		}
	}

	private void updateGroupInPTables(Group group) {
		try {
			pGroupDao.delete(group);
			pGroupDao.insert(group);
		} catch (DaoException e) {
			throw new ProcessingException(e);
		}
	}

	private void addUserToAllParentGroupsInLdap(ObmDomain domain, GroupExtId groupExtId, ObmUser userFromDao){
		Set<Group.Id> groupsId = null;

		try {
			groupsId = groupDao.listParents(domain, groupExtId);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot get hierarchy of group with extId '%s' in database.", groupExtId.getId()), e);
		}

		LdapManager ldapManager = buildLdapManager(domain);

		try {
			for (Group.Id id : groupsId) {
				addUserToGroupInLdap(ldapManager, domain, getGroupFromDao(id), userFromDao);
			}
		} finally {
			ldapManager.shutdown();
		}
	}
}
