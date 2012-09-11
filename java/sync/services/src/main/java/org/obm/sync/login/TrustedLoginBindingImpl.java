package org.obm.sync.login;

import org.obm.annotations.transactional.Transactional;
import org.obm.sync.auth.AccessToken;

import com.google.inject.Inject;

import fr.aliacom.obm.common.ObmSyncVersionNotFoundException;
import fr.aliacom.obm.common.session.SessionManagement;

public class TrustedLoginBindingImpl extends LoginBindingImpl {

	@Inject
	public TrustedLoginBindingImpl(SessionManagement sessionManagement) {
		super(sessionManagement);
	}

	@Override
	@Transactional(readOnly=true)
	public AccessToken logUserIn(String user, String password, String origin,
			String clientIP, String remoteIP, String lemonLogin,
			String lemonDomain, boolean isPasswordHashed) throws ObmSyncVersionNotFoundException {

		return sessionManagement.trustedLogin(user, origin, clientIP, remoteIP, lemonLogin, lemonDomain);
	}

}
