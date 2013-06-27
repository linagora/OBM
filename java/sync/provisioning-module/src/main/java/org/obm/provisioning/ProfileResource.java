package org.obm.provisioning;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.obm.provisioning.beans.ProfileId;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.exceptions.ProfileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;

public class ProfileResource {

	private final Logger logger = LoggerFactory.getLogger(ProvisioningService.class);

	@Inject
	private ProfileDao profileDao;

	@Context
	private ObmDomain domain;
	
	@GET @Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProfileEntries() {
		try {
			return Response
					.ok(profileDao.getProfiles(domain.getUuid()))
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
