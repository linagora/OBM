package org.obm.provisioning.resources;

import static org.obm.provisioning.bean.Permissions.profiles_read;
import static org.obm.provisioning.resources.AbstractBatchAwareResource.JSON_WITH_UTF8;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.obm.provisioning.ProfileId;
import org.obm.provisioning.ProvisioningService;
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
	
	@GET
	@Path("/")
	@RequiresPermissions(profiles_read)
	@Produces(JSON_WITH_UTF8)
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
	
	@GET
	@Path("/{profileId}")
	@RequiresPermissions(profiles_read)
	@Produces(JSON_WITH_UTF8)
	public Response getProfileName(@PathParam("profileId")long profileId) {
		try {
			return Response.ok(profileDao.getProfile(domain.getUuid(), ProfileId.builder().id(profileId).build())).build();
		} catch (ProfileNotFoundException e) {
			logger.error("Profile not found", e);
			return Response.status(Status.NOT_FOUND).build();
		} catch (Exception e) {
			logger.error("Cannot get profile name for given id", e);
			return Response.serverError().build();
		}
	}
	
}
