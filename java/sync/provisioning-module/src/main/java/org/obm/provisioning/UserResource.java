/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.provisioning;

import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obm.provisioning.bean.UserIdentifier;
import org.obm.provisioning.dao.UserDao;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;

import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

public class UserResource {

	private final static String UTF_8 = ";charset=UTF-8";

	@Inject
	private UserDao userDao;

	@Context
	private ObmDomain domain;

	@GET
	@Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON + UTF_8)
	public ObmUser get(@PathParam("userId") int userId) {
		return userDao.get(userId);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON + UTF_8)
	public Set<UserIdentifier> listAll() {
		return userDao.listAll();
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON + UTF_8)
	@Produces(MediaType.APPLICATION_JSON + UTF_8)
	public Response create(ObmUser user) {
		try {
			userDao.create(user);
		} catch (DaoException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}
		return Response.status(Status.CREATED).build();
	}

	
	
	@PUT @Path("/{userId}")
	@Consumes(MediaType.APPLICATION_JSON + UTF_8)
	@Produces(MediaType.APPLICATION_JSON + UTF_8)
	public Response modify(@PathParam("userId") int userId, ObmUser user) {
		try {
			userDao.modify(userId, user);
		} catch(UserNotFoundException e) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.status(Status.OK).build();
	}
	
	@DELETE @Path("/{userId}")
	@Produces(MediaType.APPLICATION_JSON + UTF_8)
	public Response delete(@PathParam("userId") int userId, @QueryParam("expunge") boolean expunge) {
		try {
			userDao.delete(userId, expunge);
		} catch (UserNotFoundException e) {
			return Response.status(Status.NOT_FOUND).build();
		}
		return Response.status(Status.OK).build();
	}
}
