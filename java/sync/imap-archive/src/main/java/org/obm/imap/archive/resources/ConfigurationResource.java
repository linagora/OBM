/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
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

package org.obm.imap.archive.resources;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.process.internal.RequestScoped;
import org.obm.annotations.transactional.Transactional;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.PersistedResult;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.dto.DomainConfigurationDto;
import org.obm.imap.archive.exception.LoginMismatchException;
import org.obm.imap.archive.services.DomainConfigurationService;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;

import com.google.common.base.Objects;

import fr.aliacom.obm.common.domain.ObmDomain;

@RequestScoped
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConfigurationResource {
	
	@Inject
	private DomainConfigurationDao domainConfigurationDao;
	@Inject
	private DomainConfigurationService domainConfigurationService;

	@Context
	private ObmDomain domain;
	@Inject
	private UriInfo uriInfo;

	@GET
	public DomainConfigurationDto configuration() throws DaoException {
		return DomainConfigurationDto.from(
				Objects.firstNonNull(domainConfigurationDao.get(domain), 
						DomainConfiguration.DEFAULT_VALUES_BUILDER
							.domain(domain)
							.build()));
	}
	
	@PUT
	@Transactional
	public Response update(DomainConfigurationDto domainConfigurationDto) throws DaoException {
		try {
			PersistedResult persistedResult = domainConfigurationService.updateOrCreate(DomainConfiguration.from(domainConfigurationDto, domain));
			if (persistedResult.isUpdate()) {
				return Response.noContent().build();
			} else {
				return Response.created(uriInfo.getAbsolutePath()).build();
			}
		} catch (DomainNotFoundException | LoginMismatchException | UserNotFoundException e) {
			throw new WebApplicationException(e, Status.NOT_FOUND);
		}
	}
}
