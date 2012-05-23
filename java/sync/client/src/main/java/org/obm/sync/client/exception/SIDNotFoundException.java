package org.obm.sync.client.exception;

import org.obm.sync.auth.AccessToken;

public class SIDNotFoundException extends Exception {

	public SIDNotFoundException(AccessToken token) {
		super(token.getUserWithDomain() + " sid not found.");
	}

}
