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

/**
 * Un annuaire ldap pour l'authentification
 */
public class LDAPDirectory {

	private String uri;

	private String userPattern;

	private String rootDN;

	private String rootPW;

	private String matchField;

	private String baseDN;

	private String obmDomain;

	public LDAPDirectory(String uri, String userPattern, String rootDN,
			String rootPW, String baseDN, String matchField, String obmDomain) {
		this.uri = uri;
		this.userPattern = userPattern;
		this.rootDN = rootDN;
		this.rootPW = rootPW;
		this.baseDN = baseDN;
		this.matchField = matchField;
		this.obmDomain = obmDomain;

	}

	public String getUri() {
		return uri;
	}

	public String getUserPattern() {
		return userPattern;
	}

	public String getRootDN() {
		return rootDN;
	}

	public String getRootPW() {
		return rootPW;
	}

	public String getBaseDN() {
		return baseDN;
	}

	public String getMatchField() {
		return matchField;
	}

	public String getObmDomain() {
		return obmDomain;
	}

}
