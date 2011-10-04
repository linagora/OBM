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
package fr.aliacom.obm.ldap;

import com.google.inject.Inject;

import fr.aliacom.obm.services.constant.ConstantService;

/**
 * Contient la liste des ldaps sur lesquels on peut essayer de s'authentifier
 */
class LDAPAuthConfig {

	private LDAPDirectory dir;

	@Inject
	private LDAPAuthConfig(ConstantService cs) {
		String uri = cs.getStringValue("auth-ldap-server");
		String baseDN = cs.getStringValue("auth-ldap-basedn").replace("\"", "");
		String userFilter = cs.getStringValue("auth-ldap-filter").replace("\"",
				"");
		String bindDn = cs.getStringValue("auth-ldap-binddn");
		if (bindDn != null) {
			bindDn = bindDn.replace("\"", "");
		}
		String bindPw = cs.getStringValue("auth-ldap-bindpw");
		if (bindPw != null) {
			bindPw = bindPw.replace("\"", "");
		}
		dir = new LDAPDirectory(uri, userFilter, bindDn, bindPw, baseDN, null,
				null);
	}

	public LDAPDirectory getDirectory() {
		return dir;
	}

}
