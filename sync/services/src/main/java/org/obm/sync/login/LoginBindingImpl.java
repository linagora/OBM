/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.login;

import org.obm.sync.auth.AccessToken;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.ObmSyncVersionNotFoundException;
import fr.aliacom.obm.common.session.SessionManagement;

@Singleton
public class LoginBindingImpl {

	private final SessionManagement sessionManagement;

	@Inject
	private LoginBindingImpl(SessionManagement sessionManagement) {
		this.sessionManagement = sessionManagement;
	}
	
	public AccessToken logUserIn(String user, String password, String origin,
			String clientIP, String remoteIP, String lemonLogin,
			String lemonDomain) throws ObmSyncVersionNotFoundException {
		
		return sessionManagement.login(user, password, origin, clientIP, remoteIP, lemonLogin, lemonDomain);
	}

	public void logout(String sessionId) {
		sessionManagement.logout(sessionId);
	}
}
