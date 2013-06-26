package org.obm.provisioning;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obm.provisioning.beans.ProfileId;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.exceptions.ProfileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Path("profiles")
public class ProfileResource {

	Logger logger = LoggerFactory.getLogger(ProvisioningService.class);
	
	@Inject
	private ProfileDao profileDao;

//	@PathParam("domainUuid")
	private String domainUuid = "ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6";
	
	@GET @Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProfileEntries() {
		try {
			return Response
					.ok(profileDao.getProfiles(ObmDomainUuid.of(domainUuid)))
					.build();
		} catch (Exception e) {
			logger.error("Cannot get profiles for given domain", e);
			return Response.serverError().build();
		}
	}
	
	@GET @Path("/{profileId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProfileName(@PathParam("profileId")long profileId) {
		try {
			return Response.ok(profileDao.getProfile(ProfileId.builder().id(profileId).build())).build();
		} catch (ProfileNotFoundException e) {
			logger.error("Profile not found", e);
			return Response.status(Status.NOT_FOUND).build();
		} catch (Exception e) {
			logger.error("Cannot get profile name for given id", e);
			return Response.serverError().build();
		}
	}
	
}
