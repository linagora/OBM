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
package org.obm.sync.server.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.sync.ServerCapability;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.login.LoginBindingImpl;
import org.obm.sync.login.TrustedLoginBindingImpl;

import com.google.common.collect.ImmutableMap;

import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;

public class SimpleLoginHandlerTest {

	private LoginHandler handler;

	private IMocksControl control;
	private ObmSyncConfigurationService configurationService;

	@Before
	public void setup() {
		LoginBindingImpl loginBinding = null;
		TrustedLoginBindingImpl trustedLoginBinding = null;
		SettingsService settingsService = null;
		UserService userService = null;

		control = createControl();
		configurationService = control.createMock(ObmSyncConfigurationService.class);

		handler = new LoginHandler(
				loginBinding,
				trustedLoginBinding,
				settingsService,
				userService, configurationService);
	}

	@After
	public void tearDown() {
		control.verify();
	}

	@Test
	public void testFillTokenWithServerCapabilitiesShouldIncludeConfidentialEventsWithTrueWhenConfIsTrue() {
		AccessToken token = new AccessToken(0, "origin");

		expect(configurationService.isConfidentialEventsEnabled()).andReturn(true);
		control.replay();

		handler.fillTokenWithServerCapabilities(token);

		assertThat(token.getServerCapabilities()).isEqualTo(ImmutableMap
				.builder()
				.put(ServerCapability.ADDRESS_BOOK_HANDLER_SUPPORTS_STORECONTACT, "true")
				.put(ServerCapability.CALENDAR_HANDLER_SUPPORTS_NOTALLOWEDEXCEPTION, "true")
				.put(ServerCapability.CALENDAR_HANDLER_SUPPORTS_PAGINATION, "true")
				.put(ServerCapability.CALENDAR_HANDLER_SUPPORTS_STOREEVENT, "true")
				.put(ServerCapability.CONFIDENTIAL_EVENTS, "true")
				.put(ServerCapability.SERVER_SIDE_ANONYMIZATION, "true")
				.put(ServerCapability.HASH_IN_EVENT_SERIALIZATION, "true")
				.put(ServerCapability.LOGIN_HANDLER_SUPPORTS_AUTHFAULT, "true")
				.build());
	}

	@Test
	public void testFillTokenWithServerCapabilitiesShouldIncludeConfidentialEventsWithFalseWhenConfIsFalse() {
		AccessToken token = new AccessToken(0, "origin");

		expect(configurationService.isConfidentialEventsEnabled()).andReturn(false);
		control.replay();

		handler.fillTokenWithServerCapabilities(token);

		assertThat(token.getServerCapabilities()).isEqualTo(ImmutableMap
				.builder()
				.put(ServerCapability.ADDRESS_BOOK_HANDLER_SUPPORTS_STORECONTACT, "true")
				.put(ServerCapability.CALENDAR_HANDLER_SUPPORTS_NOTALLOWEDEXCEPTION, "true")
				.put(ServerCapability.CALENDAR_HANDLER_SUPPORTS_PAGINATION, "true")
				.put(ServerCapability.CALENDAR_HANDLER_SUPPORTS_STOREEVENT, "true")
				.put(ServerCapability.CONFIDENTIAL_EVENTS, "false")
				.put(ServerCapability.SERVER_SIDE_ANONYMIZATION, "true")
				.put(ServerCapability.HASH_IN_EVENT_SERIALIZATION, "true")
				.put(ServerCapability.LOGIN_HANDLER_SUPPORTS_AUTHFAULT, "true")
				.build());
	}

}
