package org.obm.provisioning;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fr.aliacom.obm.common.domain.ObmDomain;

public class BatchResource {

	@Context
	private ObmDomain domain;

	@GET
	@Path("/{batchId}")
	@Produces("application/json")
	public Response status(@PathParam("batchId") String batchId) {
		return Response.status(Status.NOT_FOUND).build();
	}

}
