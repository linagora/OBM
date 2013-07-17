/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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

import static org.obm.provisioning.bean.Permissions.users_create;
import static org.obm.provisioning.bean.Permissions.users_delete;
import static org.obm.provisioning.bean.Permissions.users_patch;
import static org.obm.provisioning.bean.Permissions.users_update;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.obm.annotations.transactional.Transactional;
import org.obm.provisioning.annotations.PATCH;
import org.obm.provisioning.authorization.ResourceAuthorizationHelper;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.dao.exceptions.DaoException;

public class UserWriteResource extends AbstractBatchAwareResource {

	@POST
	@Consumes(JSON_WITH_UTF8)
	@Produces(JSON_WITH_UTF8)
	@Transactional
	public Response create(String user) throws DaoException {
		ResourceAuthorizationHelper.assertAuthorized(domain, users_create);
		return addBatchOperation(user, HttpVerb.POST, BatchEntityType.USER);
	}

	@PUT
	@Path("/{userId}")
	@Consumes(JSON_WITH_UTF8)
	@Produces(JSON_WITH_UTF8)
	@Transactional
	public Response modify(String user) throws DaoException {
		ResourceAuthorizationHelper.assertAuthorized(domain, users_update);
		return addBatchOperation(user, HttpVerb.PUT, BatchEntityType.USER);
	}

	@DELETE
	@Path("/{userId}")
	@Produces(JSON_WITH_UTF8)
	@Transactional
	public Response delete() throws DaoException {
		ResourceAuthorizationHelper.assertAuthorized(domain, users_delete);
		return addBatchOperation(null, HttpVerb.DELETE, BatchEntityType.USER);
	}
	
	@PATCH
	@Path("/{userId}")
	@Consumes(JSON_WITH_UTF8)
	@Produces(JSON_WITH_UTF8)
	@Transactional
	public Response patch(String user) throws DaoException {
		ResourceAuthorizationHelper.assertAuthorized(domain, users_patch);
		return addBatchOperation(user, HttpVerb.PATCH, BatchEntityType.USER);
	}
}
