/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.sync.client.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.DomainConfiguration;
import org.obm.sync.Parameter;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.client.AbstractClientTest;
import org.obm.sync.client.impl.SyncClientAssert;
import org.w3c.dom.Document;

import com.google.common.collect.Multimap;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.UserPassword;


public class LoginClientTest extends AbstractClientTest {

	private LoginClient client;

	@Before
	public void setUpLoginClient() {
		client = new LoginClient("client-testing", new DomainConfiguration() {

			@Override
			public String getObmSyncServicesUrl(String obmSyncHost) {
				return null;
			}

			@Override
			public String getObmSyncBaseUrl(String obmSyncHost) {
				return null;
			}

			@Override
			public String getGlobalDomain() {
				return null;
			}

		}, new SyncClientAssert(), null, logger, null) {

			@Override
			protected Document execute(AccessToken token, String action, Multimap<String, Parameter> parameters) {
				return responder.execute(token, action, parameters);
			}

		};
	}

	@Test(expected = AuthFault.class)
	public void testLoginAuthFault() throws Exception {
		Document document = mockErrorDocument(AuthFault.class, null);

		expect(responder.execute(isA(AccessToken.class), eq("/login/doLogin"), isA(Multimap.class))).andReturn(document).once();
		control.replay();

		client.login("user@domain.com", UserPassword.valueOf("secret"));
	}

	@Test(expected = AuthFault.class)
	public void testLoginAuthFaultAsDefaultWhenTypeIsNotDefined() throws Exception {
		Document document = mockErrorDocument(null, "any error !");

		expect(responder.execute(isA(AccessToken.class), eq("/login/doLogin"), isA(Multimap.class))).andReturn(document).once();
		control.replay();

		client.login("user@domain.com", UserPassword.valueOf("secret"));
	}

	@Test(expected = RuntimeException.class)
	public void testLoginRuntimeException() throws Exception {
		Document document = mockErrorDocument(RuntimeException.class, null);

		expect(responder.execute(isA(AccessToken.class), eq("/login/doLogin"), isA(Multimap.class))).andReturn(document).once();
		control.replay();

		client.login("user@domain.com", UserPassword.valueOf("secret"));
	}

	@Test
	public void testLogin() throws Exception {
		ObmDomain domain = ObmDomain
				.builder()
				.uuid(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6"))
				.name("domain.com")
				.build();
		MavenVersion version = new MavenVersion("1", "0", "0");
		Document document = mockAccessTokenDocument("user@domain.com", "user", "sid", version, domain);

		expect(responder.execute(isA(AccessToken.class), eq("/login/doLogin"), isA(Multimap.class))).andReturn(document).once();
		control.replay();

		AccessToken token = client.login("user@domain.com", UserPassword.valueOf("secret"));
		AccessToken expectedToken = new AccessToken(0, "client-testing");

		expectedToken.setUserLogin("user");
		expectedToken.setUserDisplayName("user");
		expectedToken.setVersion(version);
		expectedToken.setSessionId("sid");
		expectedToken.setUserEmail("user@domain.com");
		expectedToken.setDomain(domain);

		assertThat(token).isEqualToComparingFieldByField(expectedToken);
	}

}
