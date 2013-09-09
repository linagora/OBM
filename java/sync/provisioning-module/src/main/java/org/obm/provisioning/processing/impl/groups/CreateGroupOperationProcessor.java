/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2013  Linagora
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
package org.obm.provisioning.processing.impl.groups;

import org.obm.annotations.transactional.Transactional;
import org.obm.provisioning.Group;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.ldap.client.LdapManager;

import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;

public class CreateGroupOperationProcessor extends AbstractGroupOperationProcessor {

	@Inject
	CreateGroupOperationProcessor() {
		super(HttpVerb.POST);
	}

	@Override
	@Transactional
	public void process(Operation operation, Batch batch) throws ProcessingException {
		Group group = getGroupFromRequestBody(operation, getDefaultObjectMapper());
		Group groupFromDao = createGroupInDao(group, batch.getDomain());

		createGroupInLdap(groupFromDao, batch.getDomain());
		createGroupInPTables(groupFromDao);
	}

	private void createGroupInLdap(Group group, ObmDomain domain) {
		LdapManager ldapManager = buildLdapManager(domain);

		try {
			ldapManager.createGroup(group, domain);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot insert new group '%s' (%s) in LDAP.", group.getExtId().getId(), group.getName()), e);
		} finally {
			ldapManager.shutdown();
		}
	}

	private Group createGroupInDao(Group group, ObmDomain domain) {
		try {
			return groupDao.create(domain, group);
		} catch (Exception e) {
			throw new ProcessingException(String.format("Cannot insert new Group '%s' (%s) in database.", group.getName(), group.getExtId().getId()), e);
		}
	}
	
	private void createGroupInPTables(Group group) {
		try {
			pGroupDao.insert(group);
		} catch (DaoException e) {
			throw new ProcessingException(e);
		}
	}
}
