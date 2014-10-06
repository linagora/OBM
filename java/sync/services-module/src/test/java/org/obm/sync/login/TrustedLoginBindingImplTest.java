/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

package org.obm.sync.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.expect;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.domain.dao.TrustTokenDao;
import org.obm.push.utils.DateUtils;
import org.obm.sync.auth.AccessToken;

import fr.aliacom.obm.common.session.SessionManagement;
import fr.aliacom.obm.common.trust.TrustToken;
import fr.aliacom.obm.common.user.UserPassword;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;


public class TrustedLoginBindingImplTest {

	private IMocksControl control;
	private SessionManagement sessionManagement;
	private TrustTokenDao trustTokenDao;
	private ObmSyncConfigurationService configurationService;
	private TrustedLoginBindingImpl trustedLoginBindingImpl;
	
	@Before
	public void setup() {
		control = EasyMock.createControl();
		
		sessionManagement = control.createMock(SessionManagement.class);
		trustTokenDao = control.createMock(TrustTokenDao.class);
		configurationService = control.createMock(ObmSyncConfigurationService.class);
		
		trustedLoginBindingImpl = new TrustedLoginBindingImpl(sessionManagement, trustTokenDao, configurationService);
	}
	
	@Test
	public void shouldLogUserInWhenUserWithDomain() throws Exception {
		String user = "user";
		String userAtDomain = user + "@mydomain.org";
		String token = "8ca8abfb-6f6f-47e4-a43c-d48513431911";
		AccessToken expectedAccessToken = new AccessToken(1, "origin");
		
		expect(trustTokenDao.getTrustToken(user))
			.andReturn(new TrustToken(token, DateUtils.getCurrentDate()));
		expect(configurationService.trustTokenTimeoutInSeconds())
			.andReturn(60);
		expect(sessionManagement.trustedLogin(userAtDomain, null, null, null, null, null))
			.andReturn(expectedAccessToken);
		
		control.replay();
		assertThat(trustedLoginBindingImpl.logUserIn(userAtDomain, UserPassword.valueOf(token), null, null, null, null, null, false)).isEqualTo(expectedAccessToken);
		control.verify();
	}
	
	@Test
	public void shouldLogUserInWhenUserWithoutDomain() throws Exception {
		String user = "user";
		String token = "8ca8abfb-6f6f-47e4-a43c-d48513431911";
		AccessToken expectedAccessToken = new AccessToken(1, "origin");
		
		expect(trustTokenDao.getTrustToken(user))
			.andReturn(new TrustToken(token, DateUtils.getCurrentDate()));
		expect(configurationService.trustTokenTimeoutInSeconds())
			.andReturn(60);
		expect(sessionManagement.trustedLogin(user, null, null, null, null, null))
			.andReturn(expectedAccessToken);
		
		control.replay();
		assertThat(trustedLoginBindingImpl.logUserIn(user, UserPassword.valueOf(token), null, null, null, null, null, false)).isEqualTo(expectedAccessToken);
		control.verify();
	}
}
