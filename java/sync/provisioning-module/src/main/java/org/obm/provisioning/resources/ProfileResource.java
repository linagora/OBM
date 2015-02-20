package org.obm.provisioning.resources;

import static org.obm.provisioning.resources.AbstractBatchAwareResource.JSON_WITH_UTF8;

import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import org.obm.annotations.transactional.Transactional;
import org.obm.provisioning.ProfileId;
import org.obm.provisioning.ProfileName;
import org.obm.provisioning.beans.ProfileEntry;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.ProfileNotFoundException;

import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;

public class ProfileResource {

	@Inject
	private ProfileDao profileDao;

	@Context
	private ObmDomain domain;

	@GET
	@Path("/")
	@Produces(JSON_WITH_UTF8)
	@Transactional(readOnly = true)
	public Set<ProfileEntry> getProfileEntries() throws DaoException {
		return profileDao.getProfileEntries(domain.getUuid());
	}

	@GET
	@Path("/{profileId}")
	@Produces(JSON_WITH_UTF8)
	@Transactional(readOnly = true)
	public ProfileName getProfileName(@PathParam("profileId") ProfileId profileId) throws DaoException {
		try {
			return profileDao.getProfileName(domain.getUuid(), profileId);
		}
		catch (ProfileNotFoundException e) {
			throw new WebApplicationException(e, Status.NOT_FOUND);
		}
	}

}
