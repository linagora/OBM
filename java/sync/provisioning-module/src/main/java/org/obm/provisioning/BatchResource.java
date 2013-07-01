package org.obm.provisioning;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.dao.BatchDao;
import org.obm.provisioning.dao.exceptions.BatchNotFoundException;
import org.obm.provisioning.dao.exceptions.DaoException;

import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;

public class BatchResource {

	@Context
	private ObmDomain domain;

	@Inject
	private BatchDao batchDao;

	@GET
	@Path("{batchId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Batch get(@PathParam("batchId") Batch.Id batchId) throws DaoException {
		Batch batch = batchDao.get(batchId);

		if (batch == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		return batch;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response create() throws DaoException, URISyntaxException {
		Batch batch = Batch
				.builder()
				.domain(domain)
				.status(BatchStatus.IDLE)
				.build();
		Batch createdBatch = batchDao.create(batch);

		return Response
				.created(new URI(String.valueOf(createdBatch.getId().getId())))
				.entity(createdBatch.getId())
				.build();
	}

	@DELETE
	@Path("{batchId}")
	public Response discard(@PathParam("batchId") Batch.Id batchId) throws DaoException {
		try {
			batchDao.delete(batchId);
		}
		catch (BatchNotFoundException e) {
			throw new WebApplicationException(e, Status.NOT_FOUND);
		}

		return Response.ok().build();
	}

	@Path("{batchId}/users")
	public Class<UserWriteResource> users() {
		return UserWriteResource.class;
	}
}
