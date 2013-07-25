package org.obm.provisioning.resources;

import static org.obm.provisioning.bean.Permissions.groups_read;
import static org.obm.provisioning.resources.AbstractBatchAwareResource.JSON_WITH_UTF8;

import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response.Status;

import org.obm.provisioning.Group;
import org.obm.provisioning.GroupExtId;
import org.obm.provisioning.ProvisioningService;
import org.obm.provisioning.authorization.ResourceAuthorizationHelper;
import org.obm.provisioning.dao.GroupDao;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.GroupNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;

public class GroupResource {

	Logger logger = LoggerFactory.getLogger(ProvisioningService.class);
	
	@Inject
	private GroupDao groupDao;

	@Context
	private ObmDomain domain;

	@GET
	@Path("/{groupExtId}")
	@Produces(JSON_WITH_UTF8)
	public Group get(@PathParam("groupExtId") GroupExtId groupExtId, @QueryParam("expandDepth") @DefaultValue("0") int expandDepth) throws DaoException {
		ResourceAuthorizationHelper.assertAuthorized(domain, groups_read);
		try {
			return groupDao.getRecursive(domain, groupExtId, true, expandDepth);
		} catch (GroupNotFoundException e) {
			logger.error(String.format("Group %s not found", groupExtId), e);

			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@GET
	@Path("/{groupExtId}/users")
	@Produces(JSON_WITH_UTF8)
	public Set<ObmUser> getUserMembers(@PathParam("groupExtId") GroupExtId groupExtId) throws DaoException {
		ResourceAuthorizationHelper.assertAuthorized(domain, groups_read);
		try {
			return groupDao.getRecursive(domain, groupExtId, true, 0).getUsers();
		} catch (GroupNotFoundException e) {
			logger.error(String.format("Group %s not found", groupExtId), e);

			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}

	@GET
	@Path("/{groupExtId}/subgroups")
	@Produces(JSON_WITH_UTF8)
	public Set<Group> getUserMembersOfSubgroups(@PathParam("groupExtId") GroupExtId groupExtId) throws DaoException {
		ResourceAuthorizationHelper.assertAuthorized(domain, groups_read);
		try {
			return groupDao.getRecursive(domain, groupExtId, true, 1).getSubgroups();
		} catch (GroupNotFoundException e) {
			logger.error(String.format("Group %s not found", groupExtId), e);

			throw new WebApplicationException(Status.NOT_FOUND);
		}
	}
}
