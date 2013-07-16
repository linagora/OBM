package org.obm.provisioning.authorization;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.apache.shiro.SecurityUtils;

import fr.aliacom.obm.common.domain.ObmDomain;

public class ResourceAuthorizationHelper {

	public static void assertAuthorized(ObmDomain domain, String permission) {
		String domainContextualPermission = String.format("%s:%s", domain.getUuid().get(), permission);
		if (!SecurityUtils.getSubject().isPermitted(domainContextualPermission)) {
			throw new WebApplicationException(Status.UNAUTHORIZED);
		}
	}
}
