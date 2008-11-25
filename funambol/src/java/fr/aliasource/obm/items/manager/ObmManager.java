package fr.aliasource.obm.items.manager;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.ISyncClient;

import fr.aliasource.funambol.OBMException;

public abstract class ObmManager {

	protected AccessToken token;
	protected boolean syncReceived = false;

	public abstract ISyncClient getSyncClient();

	public void logIn(String user, String pass) throws OBMException {
		token = getSyncClient().login(user, pass);
		if (token == null) {
			throw new OBMException("OBM Login refused for user : " + user);
		}
	}

	public void logout() {
		getSyncClient().logout(token);
	}

}
