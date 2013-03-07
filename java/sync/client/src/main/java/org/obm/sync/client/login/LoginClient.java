/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.sync.client.login;

import org.obm.annotations.technicallogging.KindToBeLogged;
import org.obm.annotations.technicallogging.ResourceType;
import org.obm.annotations.technicallogging.TechnicalLogging;
import org.obm.configuration.module.LoggerModule;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.client.exception.SIDNotFoundException;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.client.impl.SyncClientException;
import org.obm.sync.locators.Locator;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import fr.aliacom.obm.common.domain.ObmDomain;

public class LoginClient extends AbstractClientImpl implements LoginService {

	private final Locator locator;
	private final String origin;

	@Inject
	protected LoginClient(@Named("origin")String origin,
			SyncClientException syncClientException, Locator locator, @Named(LoggerModule.OBM_SYNC)Logger obmSyncLogger) {
		super(syncClientException, obmSyncLogger);
		this.origin = origin;
		this.locator = locator;
	}
	
	@Override
	@TechnicalLogging(kindToBeLogged=KindToBeLogged.RESOURCE, onStartOfMethod=true, resourceType=ResourceType.HTTP_CLIENT)
	public AccessToken login(String loginAtDomain, String password) throws AuthFault {
		Multimap<String, String> params = ArrayListMultimap.create();
		params.put("login", loginAtDomain);
		params.put("password", password);
		params.put("origin", origin);

		AccessToken token = newAccessToken(loginAtDomain, origin);
		
		Document doc = execute(token, "/login/doLogin", params);
		exceptionFactory.checkLoginExpection(doc);
		
		return fillToken(token, doc);
	}

	private AccessToken fillToken(AccessToken token, Document doc) {
		Element root = doc.getDocumentElement();
		String email = DOMUtils.getElementText(root, "email");
		String displayname = DOMUtils.getElementText(root, "displayname");
		String sid = DOMUtils.getElementText(root, "sid");
		Element v = DOMUtils.getUniqueElement(root, "version");
		Element domain = DOMUtils.getUniqueElement(root, "domain");
		token.setDomain(getDomain(domain));
		token.setSessionId(sid);
		token.setVersion(getVersion(v));
		token.setUserEmail(email);
		token.setUserDisplayName(displayname);
		return token;
	}

	private MavenVersion getVersion(Element v) {
		MavenVersion version = new MavenVersion();
		if (v != null) {
			version.setMajor(v.getAttribute("major"));
			version.setMinor(v.getAttribute("minor"));
			version.setRelease(v.getAttribute("release"));
		}
		return version;
	}

	@Override
	public AccessToken authenticate(String loginAtDomain, String password) throws AuthFault {
		AccessToken token = login(loginAtDomain, password);
		try {
			if (token == null || token.getSessionId() == null) {
				throw new AuthFault(loginAtDomain + " can't log on obm-sync. The username or password isn't valid");
			}
		} finally {
			logout(token);
		}
		return token;
	}

	@Override
	@TechnicalLogging(kindToBeLogged=KindToBeLogged.RESOURCE, onEndOfMethod=true, resourceType=ResourceType.HTTP_CLIENT)
	public void logout(AccessToken at) {
		try {
			Multimap<String, String> params = ArrayListMultimap.create();
			setToken(params, at);
			executeVoid(at, "/login/doLogout", params);
		} catch (SIDNotFoundException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	private ObmDomain getDomain(Element domain) {
		return ObmDomain
				.builder()
				.uuid(domain.getAttribute("uuid"))
				.name(DOMUtils.getElementText(domain))
				.build();
	}
	
	private AccessToken newAccessToken(String loginAtDomain, String origin) {
		AccessToken token = new AccessToken(0, origin);
		ObmDomain obmDomain = ObmDomain
                				.builder()
                				.name(loginAtDomain.split("@", 2)[1])
                				.build();

		token.setUserLogin(loginAtDomain.split("@", 2)[0]);
		token.setDomain(obmDomain);
		
		return token;
	}

	@Override
	protected Locator getLocator() {
		return locator;
	}
	
}
