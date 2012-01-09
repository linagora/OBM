package org.obm.sync.client.login;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;

public interface LoginService {

	AccessToken login(String loginAtDomain, String password, String origin);
	AccessToken authenticate(String loginAtDomain, String password, String origin) throws AuthFault;
	void logout(AccessToken at);
	
}