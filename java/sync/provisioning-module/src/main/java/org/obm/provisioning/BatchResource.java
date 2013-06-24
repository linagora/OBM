package org.obm.provisioning;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.inject.Singleton;

@Singleton
@Path("batches")
public class BatchResource {

	@GET @Path("/{batchId}")
	@Produces("application/json")
	public Response status(@PathParam("batchId")String batchId) {
		return Response.status(Status.NOT_FOUND).build();
	}
	
}
