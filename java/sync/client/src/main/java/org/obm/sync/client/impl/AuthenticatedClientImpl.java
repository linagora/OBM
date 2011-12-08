package org.obm.sync.client.impl;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.ISyncClient;
import org.obm.sync.client.login.LoginService;


public abstract class AuthenticatedClientImpl extends AbstractClientImpl implements ISyncClient {

	private final LoginService login;
	
	protected AuthenticatedClientImpl(SyncClientException exceptionFactory, LoginService login) {
		super(exceptionFactory);
		this.login = login;
	}

	@Override
	public AccessToken login(String userAtDomain, String pass, String origin) {
		return login.login(userAtDomain, pass, origin);
	}
	
	@Override
	public void logout(AccessToken at) {
		login.logout(at);
	}
	
}
