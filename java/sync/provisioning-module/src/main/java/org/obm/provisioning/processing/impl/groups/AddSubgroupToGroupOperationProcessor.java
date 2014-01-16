/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2014  Linagora
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

import org.obm.annotations.transactional.Transactional;
import org.obm.provisioning.Group;
import org.obm.provisioning.GroupExtId;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.exception.ProcessingException;
import org.obm.provisioning.ldap.client.LdapManager;

import fr.aliacom.obm.common.domain.ObmDomain;

public class AddSubgroupToGroupOperationProcessor extends AbstractGroupOperationProcessor {

	protected AddSubgroupToGroupOperationProcessor() {
		super(BatchEntityType.GROUP_MEMBERSHIP, HttpVerb.PUT);
	}

	@Override
	@Transactional
	public void process(Operation operation, Batch batch) throws ProcessingException {
		GroupExtId groupExtId = getGroupExtIdFromRequest(operation);
		ObmDomain domain = batch.getDomain();
		GroupExtId subgroupExtId = getSubgroupExtIdFromRequest(operation);
		
		addSubgroupToGroupInDao(domain, groupExtId, subgroupExtId);
		addSubgroupToGroupInLdap(domain, groupExtId, subgroupExtId);
	}

	private void addSubgroupToGroupInDao(ObmDomain domain, GroupExtId groupExtId, GroupExtId subgroupExtId) {
		try {
			groupDao.addSubgroup(domain, groupExtId, subgroupExtId);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot add group with extId '%s' to group with extId '%s' in database.",
							subgroupExtId.getId(), groupExtId.getId()), e);
		}
	}
	
	private void addSubgroupToGroupInLdap(ObmDomain domain, GroupExtId groupExtId, GroupExtId subgroupExtId) {
		LdapManager ldapManager = buildLdapManager(domain);
		Group group = getExistingGroupFromDao(groupExtId, domain);
		Group subgroup = getExistingGroupFromDao(subgroupExtId, domain);
		
		try {
			ldapManager.addSubgroupToGroup(domain, group, subgroup);
		} catch (Exception e) {
			throw new ProcessingException(
					String.format("Cannot add group with extId '%s' to group with extId '%s' in ldap.",
							subgroupExtId.getId(), groupExtId.getId()), e);
		} finally {
			ldapManager.shutdown();
		}
	}

}
