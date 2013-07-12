package org.obm.provisioning.authorization;

import java.util.Collection;

import fr.aliacom.obm.common.domain.ObmDomain;

public interface AuthorizationService {
	
	public Collection<String> getPermissions(String login, ObmDomain domain) throws AuthorizationException;
}
