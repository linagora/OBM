package org.obm.sync.client.login;

import org.obm.sync.auth.AccessToken;

public interface LoginService {

	AccessToken login(String loginAtDomain, String password, String origin);
	void logout(AccessToken at);
	
}