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
package org.obm.push.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.User;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.UnexpectedObmSyncServerException;
import org.obm.push.resource.AccessTokenResource;
import org.obm.push.resource.HttpClientResource;
import org.obm.push.resource.ResourceCloseOrder;
import org.obm.push.resource.ResourcesUtils;
import org.obm.push.service.AuthenticationService;
import org.obm.push.technicallog.bean.KindToBeLogged;
import org.obm.push.technicallog.bean.ResourceType;
import org.obm.push.technicallog.bean.TechnicalLogging;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.client.user.UserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AuthenticationServiceImpl implements AuthenticationService {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final LoginClient.Factory loginClientFactory;
	private final User.Factory userFactory;
	private final UserClient.Factory userClientFactory;
	private final AccessTokenResource.Factory accessTokenResourceFactory;

	@Inject
	@VisibleForTesting AuthenticationServiceImpl(LoginClient.Factory loginClientFactory, 
			User.Factory userFactory,
			UserClient.Factory userClientFactory,
			AccessTokenResource.Factory accessTokenResourceFactory) {
		
		this.loginClientFactory = loginClientFactory;
		this.userFactory = userFactory;
		this.userClientFactory = userClientFactory;
		this.accessTokenResourceFactory = accessTokenResourceFactory;
	}
	
	@Override
	public String getUserEmail(UserDataRequest udr) {
		try {
			return userClientFactory.create(ResourcesUtils.getHttpClient(udr))
					.getUserEmail(ResourcesUtils.getAccessToken(udr));
		} catch (ServerFault e) {
			throw new UnexpectedObmSyncServerException(e);
		}
	}

	@Override
	public Credentials authenticateValidRequest(HttpServletRequest request, String userId, String password) throws Exception {
		HttpClientResource httpClientResource = setHttpClientRequestAttribute(request);
		
		AccessTokenResource token = setAccessTokenRequestAttribute(request, 
				httpClientResource.getHttpClient(), userId, password);
		return getCredentials(token, userId, password);
	}

	private Credentials getCredentials(AccessTokenResource token, String userId, String password) throws Exception {
		User user = createUser(userId, token);
		if (user != null) {
			logger.debug("Login success {} ! ", user.getLoginAtDomain());
			return new Credentials(user, password);
		} else {
			throw new Exception("Login {"+ userId + "} failed, bad login or/and password.");
		}
	}

	private AccessTokenResource setAccessTokenRequestAttribute(HttpServletRequest request, HttpClient httpClient, String userId, String password) throws Exception {
		AccessTokenResource token = login(httpClient, getLoginAtDomain(userId), password);
		request.setAttribute(ResourceCloseOrder.ACCESS_TOKEN.name(), token);
		return token;
	}
	
	private HttpClientResource setHttpClientRequestAttribute(HttpServletRequest request) {
		HttpClientResource httpClientResource = new HttpClientResource(HttpClientBuilder.create().build());
		request.setAttribute(ResourceCloseOrder.HTTP_CLIENT.name(), httpClientResource);
		return httpClientResource;
	}

	@TechnicalLogging(kindToBeLogged=KindToBeLogged.RESOURCE, onStartOfMethod=true, resourceType=ResourceType.HTTP_CLIENT)
	private AccessTokenResource login(HttpClient httpClient, String userId, String password) throws Exception {
		AccessToken accessToken = loginClientFactory.create(httpClient)
				.authenticate(userFactory.getLoginAtDomain(userId), password);
		return accessTokenResourceFactory.create(httpClient, accessToken);
	}
	
	private User createUser(String userId, AccessTokenResource token) {
		return userFactory.createUser(userId, token.getUserEmail(), token.getUserDisplayName());
	}

	protected String getLoginAtDomain(String userId) {
		return userFactory.getLoginAtDomain(userId);
	}
}
