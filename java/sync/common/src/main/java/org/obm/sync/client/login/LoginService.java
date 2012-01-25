package org.obm.sync.client.login;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;

public interface LoginService {

	AccessToken login(String loginAtDomain, String password) throws AuthFault;
	AccessToken authenticate(String loginAtDomain, String password) throws AuthFault;
	void logout(AccessToken at);
	
}