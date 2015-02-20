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
package org.obm.provisioning.resources;

import static org.obm.provisioning.resources.AbstractBatchAwareResource.JSON_WITH_UTF8;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import org.obm.annotations.transactional.Transactional;
import org.obm.domain.dao.UserDao;
import org.obm.provisioning.bean.UserIdentifier;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

public class UserResource {

	@Inject
	private UserDao userDao;

	@Context
	private ObmDomain domain;

	@GET
	@Path("{userExtId}")
	@Produces(JSON_WITH_UTF8)
	@Transactional(readOnly = true)
	public ObmUser get(@PathParam("userExtId") UserExtId userExtId) throws SQLException {
		try {
			return userDao.getByExtIdWithGroups(userExtId, domain);
		} catch (UserNotFoundException e) {
			throw new WebApplicationException(e, Status.NOT_FOUND);
		}
	}

	@GET
	@Produces(JSON_WITH_UTF8)
	@Transactional(readOnly = true)
	public List<UserIdentifier> listAll() throws SQLException {
		List<ObmUser> users = userDao.list(domain);
		if (users == null) {
			return Collections.emptyList();
		}

		return Lists.transform(users, new Function<ObmUser, UserIdentifier>() {
			@Override
			public UserIdentifier apply(ObmUser user) {
				return UserIdentifier.builder().id(user.getExtId()).domainUuid(domain.getUuid()).build();
			}
		});
	}
}
