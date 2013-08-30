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

import java.util.Collections;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.obm.annotations.transactional.Transactional;
import org.obm.domain.dao.DomainDao;
import org.obm.provisioning.beans.ObmDomainEntry;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Path("domains")
public class DomainResource {

	@Inject
	private DomainDao domainDao;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(readOnly = true)
	public List<ObmDomainEntry> list() throws DaoException {
		List<ObmDomain> domains = domainDao.list();
		
		if (domains == null) {
			return Collections.emptyList();
		}

		return Lists.transform(domains, new Function<ObmDomain, ObmDomainEntry>() {
			@Override
			public ObmDomainEntry apply(ObmDomain domain) {
				return ObmDomainEntry.builder().id(domain.getUuid().get()).build();
			}
		});
	}

	@GET
	@Path("{domainUuid}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(readOnly = true)
	public ObmDomain get(@PathParam("domainUuid") ObmDomainUuid domainUuid) throws DaoException {
		ObmDomain domain;
		
		try {
			domain = domainDao.findDomainByUuid(domainUuid);
		} catch (DomainNotFoundException e) {
			throw new WebApplicationException(e, Status.NOT_FOUND);
		}

		return domain;
	}
}
