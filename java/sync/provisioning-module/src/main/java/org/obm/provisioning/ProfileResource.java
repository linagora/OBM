package org.obm.provisioning;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.obm.provisioning.beans.ProfileEntry;
import org.obm.provisioning.dao.ProfileDao;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
@Path("profiles")
public class ProfileResource {

	private ProfileDao profileDao;

	@Inject
	private ProfileResource(ProfileDao profileDao) {
		this.profileDao = profileDao;
	}
	
	@GET @Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProfileEntries() {
		try {
			Set<ProfileEntry> profiles = profileDao.getProfiles(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6"));
			return Response
					.ok(profiles)
					.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return Response.serverError().build();
	}
	
}
