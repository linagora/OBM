package org.obm.provisioning.authorization;

import java.util.Collection;

public interface AuthorizationService {
	
	public Collection<String> getPermissions(String login, String domainName) throws AuthorizationException;
}
