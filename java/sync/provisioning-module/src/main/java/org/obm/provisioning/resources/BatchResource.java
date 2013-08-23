package org.obm.provisioning.resources;

import static org.obm.provisioning.bean.Permissions.batches_create;
import static org.obm.provisioning.bean.Permissions.batches_delete;
import static org.obm.provisioning.bean.Permissions.batches_read;
import static org.obm.provisioning.bean.Permissions.batches_update;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.authz.AuthorizationException;
import org.obm.annotations.transactional.Transactional;
import org.obm.provisioning.authorization.ResourceAuthorizationHelper;
import org.obm.provisioning.beans.Batch;
import org.obm.provisioning.beans.BatchStatus;
import org.obm.provisioning.dao.BatchDao;
import org.obm.provisioning.dao.exceptions.BatchNotFoundException;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.processing.BatchProcessor;
import org.obm.provisioning.processing.BatchTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;

public class BatchResource {

	@Context
	private ObmDomain domain;

	@Inject
	private BatchDao batchDao;
	@Inject
	private BatchProcessor batchProcessor;
	@Inject
	private BatchTracker batchTracker;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@GET
	@Path("{batchId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional(readOnly = true)
	public Batch get(@PathParam("batchId") Batch.Id batchId) throws DaoException, AuthorizationException {
		ResourceAuthorizationHelper.assertAuthorized(domain, batches_read);
		Batch batch = batchTracker.getTrackedBatch(batchId);

		if (batch != null) {
			return batch;
		}

		batch = batchDao.get(batchId);

		if (batch == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}

		return batch;
	}

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response create() throws DaoException, URISyntaxException, AuthorizationException {
		ResourceAuthorizationHelper.assertAuthorized(domain, batches_create);
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
	@Transactional
	public Response discard(@PathParam("batchId") Batch.Id batchId) throws DaoException, AuthorizationException {
		ResourceAuthorizationHelper.assertAuthorized(domain, batches_delete);
		try {
			batchDao.delete(batchId);
		}
		catch (BatchNotFoundException e) {
			throw new WebApplicationException(e, Status.NOT_FOUND);
		}

		return Response.ok().build();
	}

	@PUT
	@Path("{batchId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Transactional
	public Response commit(@PathParam("batchId") Batch.Id batchId) throws DaoException {
		ResourceAuthorizationHelper.assertAuthorized(domain, batches_update);
		Batch batch = batchTracker.getTrackedBatch(batchId);

		if (batch == null) {
			batch = batchDao.get(batchId);

			if (batch == null) {
				throw new WebApplicationException(Status.NOT_FOUND);
			}

			if (BatchStatus.IDLE.equals(batch.getStatus())) {
				try {
					batchProcessor.process(batch);
				}
				catch (Exception e) {
					throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
				}
			} else {
				logger.info("Not commiting batch {} in status {}.", batch.getId(), batch.getStatus());
			}
		}

		return Response.ok().build();
	}

	@Path("{batchId}/users")
	public Class<UserWriteResource> users(@Context Batch batch) {
		assertBatchIsIdle(batch);

		return UserWriteResource.class;
	}
	
	@Path("{batchId}/groups")
	public Class<GroupWriteResource> groups(@Context Batch batch) {
		assertBatchIsIdle(batch);

		return GroupWriteResource.class;
	}

	private void assertBatchIsIdle(Batch batch) {
		if (!BatchStatus.IDLE.equals(batch.getStatus())) {
			throw new WebApplicationException(Status.CONFLICT);
		}
	}

}
