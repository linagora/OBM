/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version, provided you comply
 * with the Additional Terms applicable for OBM connector by Linagora
 * pursuant to Section 7 of the GNU Affero General Public License,
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain
 * the “Message sent thanks to OBM, Free Communication by Linagora”
 * signature notice appended to any and all outbound messages
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks
 * and commercial brands. Other Additional Terms apply,
 * see <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and its applicable Additional Terms for OBM along with this program. If not,
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to
 * OBM connectors.
 *
 * ***** END LICENSE BLOCK ***** */
package org.obm.provisioning.authentication;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obm.provisioning.authorization.AuthorizationException;
import org.obm.provisioning.authorization.AuthorizationService;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.user.UserPassword;

@Singleton
public class ObmJDBCAuthorizingRealm extends AuthorizingRealm {
	
	@Inject
	private AuthenticationService authenticationService;
	
	@Inject
	private AuthorizationService authorizationService;
	
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principal) {
		String loginAtDomain = (String) principal.getPrimaryPrincipal();
		
		if (loginAtDomain == null) {
			throw new AccountException("Null usernames are not allowed by this realm.");
		}
		
		String[] loginParts = splitLogin(loginAtDomain);
		
		try {
			SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
			authorizationInfo.addStringPermissions(authorizationService.getPermissions(loginParts[0], loginParts[1]));
			return authorizationInfo;
		} catch (AuthorizationException e) {
			throw new org.apache.shiro.authz.AuthorizationException(e);
		}
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
		
		String loginAtDomain = upToken.getUsername();
		if (loginAtDomain == null) {
			throw new AccountException("Null usernames are not allowed by this realm.");
		}
		
		String[] loginParts = splitLogin(loginAtDomain);
		UserPassword password = authenticationService.getPasswordForUser(loginParts[0], loginParts[1]);
		
		return new SimpleAuthenticationInfo(loginAtDomain, password.getStringValue(), this.getName());

	}

	private String[] splitLogin(String loginAtDomain) {
		String[] loginParts = loginAtDomain.split("@");
		if (loginParts.length != 2) {
			throw new AccountException("Usernames must be login@domain form for by this realm.");
		}
		return loginParts;
	}
}
