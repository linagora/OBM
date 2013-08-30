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

import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchEntityType;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.beans.HttpVerb;
import org.obm.provisioning.beans.Operation;
import org.obm.provisioning.beans.Request;
import org.obm.provisioning.dao.BatchDao;
import org.obm.provisioning.dao.exceptions.BatchNotFoundException;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;

public abstract class AbstractBatchAwareResource {

	public static final String UTF_8 = ";charset=UTF-8";
	public static final String JSON_WITH_UTF8 = MediaType.APPLICATION_JSON + UTF_8;

	@Inject
	protected BatchDao batchDao;

	@Context
	protected Batch batch;
	@Context
	protected UriInfo uriInfo;
	@Context
	protected ObmDomain domain;

	protected Map<String, String> multivaluedMapToMap(MultivaluedMap<String, String> mvMap) {
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();

		for (String key : mvMap.keySet()) {
			builder.put(key, mvMap.getFirst(key));
		}

		return builder.build();
	}

	protected Response addBatchOperation(String entity, HttpVerb httpVerb, BatchEntityType entityType) throws DaoException {
		Operation operation = Operation
				.builder()
				.entityType(entityType)
				.status(BatchStatus.IDLE)
				.request(Request
						.builder()
						.resourcePath(uriInfo.getPath())
						.body(entity)
						.verb(httpVerb)
						.params(multivaluedMapToMap(uriInfo.getQueryParameters()))
						.params(multivaluedMapToMap(uriInfo.getPathParameters()))
						.build())
				.build();

		try {
			batchDao.addOperation(batch.getId(), operation);
		} catch (BatchNotFoundException e) {
			throw new WebApplicationException(e, Status.NOT_FOUND);
		} catch (DomainNotFoundException e) {
			throw new WebApplicationException(e, Status.NOT_FOUND);
		}

		return Response.ok().build();
	}

}