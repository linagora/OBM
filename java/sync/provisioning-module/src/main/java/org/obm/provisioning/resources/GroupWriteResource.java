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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.obm.annotations.transactional.Transactional;
import org.obm.provisioning.annotations.PATCH;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.dao.exceptions.DaoException;

public class GroupWriteResource extends AbstractBatchAwareResource {

	@POST
	@Consumes(JSON_WITH_UTF8)
	@Produces(JSON_WITH_UTF8)
	@Transactional
	public Response create(String group) throws DaoException {
		return addBatchOperation(group, HttpVerb.POST, BatchEntityType.GROUP);
	}

	@PUT
	@Path("/{groupId}")
	@Consumes(JSON_WITH_UTF8)
	@Produces(JSON_WITH_UTF8)
	@Transactional
	public Response modify(String group) throws DaoException {
		return addBatchOperation(group, HttpVerb.PUT, BatchEntityType.GROUP);
	}

	@DELETE
	@Path("/{groupId}")
	@Produces(JSON_WITH_UTF8)
	@Transactional
	public Response delete() throws DaoException {
		return addBatchOperation(null, HttpVerb.DELETE, BatchEntityType.GROUP);
	}
	
	@PATCH
	@Path("/{groupId}")
	@Consumes(JSON_WITH_UTF8)
	@Produces(JSON_WITH_UTF8)
	@Transactional
	public Response patch(String group) throws DaoException {
		return addBatchOperation(group, HttpVerb.PATCH, BatchEntityType.GROUP);
	}

	@PUT
	@Path("/{groupId}/users/{userId}")
	@Consumes(JSON_WITH_UTF8)
	@Produces(JSON_WITH_UTF8)
	@Transactional
	public Response addUsertoGroup() throws DaoException {
		return addBatchOperation(null, HttpVerb.PUT, BatchEntityType.USER_MEMBERSHIP);
	}

	@DELETE
	@Path("/{groupId}/users/{userId}")
	@Produces(JSON_WITH_UTF8)
	@Transactional
	public Response deleteUserFromGroup() throws DaoException {
		return addBatchOperation(null, HttpVerb.DELETE, BatchEntityType.USER_MEMBERSHIP);
	}

	@PUT
	@Path("/{groupId}/subgroups/{subgroupId}")
	@Consumes(JSON_WITH_UTF8)
	@Produces(JSON_WITH_UTF8)
	@Transactional
	public Response addSubgrouptoGroup() throws DaoException {
		return addBatchOperation(null, HttpVerb.PUT, BatchEntityType.GROUP_MEMBERSHIP);
	}

	@DELETE
	@Path("/{groupId}/subgroups/{subgroupId}")
	@Produces(JSON_WITH_UTF8)
	@Transactional
	public Response deleteSubgroupFromGroup() throws DaoException {
		return addBatchOperation(null, HttpVerb.DELETE, BatchEntityType.GROUP_MEMBERSHIP);
	}
}
