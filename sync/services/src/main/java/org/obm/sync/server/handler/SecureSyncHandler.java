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
package org.obm.sync.server.handler;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.server.ParametersSource;

import fr.aliacom.obm.common.session.SessionManagement;

public abstract class SecureSyncHandler implements ISyncHandler {

	protected Log logger = LogFactory.getLog(getClass());
	private SessionManagement sessions;

	protected SecureSyncHandler(SessionManagement sessionManagement) {
		sessions = sessionManagement;
	}
	
	protected String p(ParametersSource params, String name) {
		return params.getParameter(name);
	}

	protected AccessToken getToken(ParametersSource params) {
		AccessToken at = new AccessToken(0, 0, "unused");
		at.setSessionId(params.getParameter("sid"));
		return at;
	}
	
	protected AccessToken getCheckedToken(ParametersSource params) throws AuthFault {
		AccessToken token = getToken(params);
		sessions.checkToken(token);
		return token;
	}

}
