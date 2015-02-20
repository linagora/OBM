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
package org.obm.provisioning.resources;

import static org.obm.provisioning.resources.AbstractBatchAwareResource.JSON_WITH_UTF8;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import org.obm.annotations.transactional.Transactional;
import org.obm.provisioning.Group;
import org.obm.provisioning.GroupExtId;
import org.obm.provisioning.bean.GroupIdentifier;
import org.obm.provisioning.dao.GroupDao;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.GroupNotFoundException;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

public class GroupResource {
	
	@Inject
	private GroupDao groupDao;

	@Context
	private ObmDomain domain;

	@GET
	@Produces(JSON_WITH_UTF8)
	@Transactional(readOnly = true)
	public Collection<GroupIdentifier> listPublicGroups() throws DaoException {
		Set<Group> groups = groupDao.listPublicGroups(domain);

		if (groups == null) {
			return Collections.emptySet();
		}

		return Collections2.transform(groups, new Function<Group, GroupIdentifier>() {
			@Override
			public GroupIdentifier apply(Group group) {
				return GroupIdentifier.builder().id(group.getExtId()).domainUuid(domain.getUuid()).build();
			}
		});
	}

	@GET
	@Path("/{groupExtId}")
	@Produces(JSON_WITH_UTF8)
	public Group get(@PathParam("groupExtId") GroupExtId groupExtId, @QueryParam("expandDepth") @DefaultValue("0") int expandDepth)
			throws DaoException {
		try {
			return groupDao.getRecursive(domain, groupExtId, true, expandDepth);
		} catch (GroupNotFoundException e) {
			throw new WebApplicationException(e, Status.NOT_FOUND);
		}
	}

	@GET
	@Path("/{groupExtId}/users")
	@Produces(JSON_WITH_UTF8)
	public Set<ObmUser> getUserMembers(@PathParam("groupExtId") GroupExtId groupExtId) throws DaoException {
		try {
			return groupDao.getRecursive(domain, groupExtId, true, 0).getUsers();
		} catch (GroupNotFoundException e) {
			throw new WebApplicationException(e, Status.NOT_FOUND);
		}
	}

	@GET
	@Path("/{groupExtId}/subgroups")
	@Produces(JSON_WITH_UTF8)
	public Set<Group> getUserMembersOfSubgroups(@PathParam("groupExtId") GroupExtId groupExtId) throws DaoException {
		try {
			return groupDao.getRecursive(domain, groupExtId, true, 1).getSubgroups();
		} catch (GroupNotFoundException e) {
			throw new WebApplicationException(e, Status.NOT_FOUND);
		}
	}
}
