/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */

package org.obm.imap.archive.authentication;

import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.obm.imap.archive.exception.AuthenticationException;
import org.obm.sync.auth.AuthFault;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.user.UserPassword;

@Singleton
public class ImapArchiveAuthorizingRealm extends AuthorizingRealm {
	
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
		SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
		authorizationInfo.addRoles(FluentIterable.from(authorizationService.getRoles(loginParts[0], loginParts[1]))
				.transform(new Function<Authorization, String>() {

					@Override
					public String apply(Authorization authorization) {
						return authorization.get();
					}
				}).toList());
		return authorizationInfo;
	}

	private String[] splitLogin(String loginAtDomain) {
		String[] loginParts = loginAtDomain.split("@");
		if (loginParts.length != 2) {
			throw new AccountException("Usernames must be in the login@domain form for by this realm.");
		}
		return loginParts;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken upToken = (UsernamePasswordToken) token;
		
		String loginAtDomain = upToken.getUsername();
		if (loginAtDomain == null) {
			throw new AuthenticationException("Null usernames are not allowed by this realm.");
		}
		
		try {
			char[] password = upToken.getPassword();
			authenticationService.getTrustedAccessTokenForUser(loginAtDomain, UserPassword.valueOf(String.valueOf(password)));
			return new SimpleAuthenticationInfo(loginAtDomain, password, this.getName());
		} catch (AuthFault e) {
			throw new AuthenticationException(e);
		}
	}
}
