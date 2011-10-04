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
package org.obm.sync.server.auth;

import org.obm.sync.server.auth.impl.DatabaseAuthentificationService;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import fr.aliacom.obm.ldap.LDAPAuthService;
import fr.aliacom.obm.services.constant.ConstantService;

/**
 * Fetches the ldap or database authentification service.
 */
@Singleton
public class AuthentificationServiceFactory {

	private final ConstantService constantService;
	private final LDAPAuthService ldapAuthService;
	private final DatabaseAuthentificationService databaseAuthentificationService;

	@Inject
	private AuthentificationServiceFactory(ConstantService constantService, 
			DatabaseAuthentificationService databaseAuthentificationService,
			Provider<LDAPAuthService> ldapAuthServiceProvider) {
		this.constantService = constantService;
		this.databaseAuthentificationService = databaseAuthentificationService;
		if (isLDAPBasedLogin()) {
			this.ldapAuthService = ldapAuthServiceProvider.get();
		} else {
			this.ldapAuthService = null;
		}
	}
	
	private boolean isLDAPBasedLogin() {
		return constantService.getStringValue("auth-ldap-server") != null;
	}

	public IAuthentificationService get() {
		if (isLDAPBasedLogin()) {
			return ldapAuthService;
		}
		return databaseAuthentificationService;
	}
}
