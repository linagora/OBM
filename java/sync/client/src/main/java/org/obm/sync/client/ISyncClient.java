package org.obm.sync.client;

import org.obm.sync.auth.AccessToken;

public interface ISyncClient {

	AccessToken login(String userAtDomain, String pass, String origin);

	void logout(AccessToken at);

}
