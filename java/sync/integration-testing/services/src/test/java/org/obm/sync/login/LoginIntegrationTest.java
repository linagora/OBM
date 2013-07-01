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
package org.obm.sync.login;

import static org.fest.assertions.api.Assertions.assertThat;

import java.net.URL;

import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.push.arquillian.ManagedTomcatSlowGuiceArquillianRunner;
import org.obm.sync.ObmSyncIntegrationTest;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.MavenVersion;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Slow
@RunWith(ManagedTomcatSlowGuiceArquillianRunner.class)
public class LoginIntegrationTest extends ObmSyncIntegrationTest {

	@Test
	@RunAsClient
	public void testDoLoginSuccess(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "user1@domain.org";
		AccessToken token = loginClient.login(calendar, "user1");
		
		assertThat(token).isNotNull();
		assertThatTokenIsWellFormed(token);
	}
	
	@Test
	@RunAsClient
	public void testDoLoginIsCaseInsensitiveWithDBAuth(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "UseR1@domain.org";
		AccessToken token = loginClient.login(calendar, "user1");
		
		assertThat(token).isNotNull();
		assertThatTokenIsWellFormed(token);
	}
	
	@Test
	@RunAsClient
	public void testDoLoginWithoutDomainSuccess(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "user1";
		AccessToken token = loginClient.login(calendar, "user1");
		
		assertThat(token).isNotNull();
		assertThatTokenIsWellFormed(token);
	}
	
	@Test
	@RunAsClient
	public void testDoLoginWithoutDomainIsCaseInsensitiveWithDBAuth(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "UseR1";
		AccessToken token = loginClient.login(calendar, "user1");
		
		assertThat(token).isNotNull();
		assertThatTokenIsWellFormed(token);
	}
	
	@Test(expected=AuthFault.class)
	@RunAsClient
	public void testDoLoginFailsWithWrongLogin(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "user@domain.org";
		try {
			loginClient.login(calendar, "user1");
		} catch(AuthFault e) {
			assertThat(e.getMessage()).contains("Login failed for user 'user@domain.org'");
			throw e;
		}
	}
	
	@Test(expected=AuthFault.class)
	@RunAsClient
	public void testDoLoginFailsWithWrongPassword(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "user1";
		try {
			loginClient.login(calendar, "user");
		} catch(AuthFault e) {
			assertThat(e.getMessage()).contains("Login failed for user 'user1'");
			throw e;
		}
	}
	
	@Test
	@RunAsClient
	public void testDoLoginSuccessForUser2onDomain1(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "user2@domain.org";
		AccessToken token = loginClient.login(calendar, "user2");
		
		assertThat(token).isNotNull();
	}
	
	@Test(expected=AuthFault.class)
	@RunAsClient
	public void testDoLoginFailsForUser2WithoutDomain(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "user2";
		try {
			loginClient.login(calendar, "user2");
		} catch(AuthFault e) {
			assertThat(e.getMessage()).contains("Login failed for user 'user2'");
			throw e;
		}
	}
	
	@Test
	@RunAsClient
	public void testDoLoginSuccessForUser2onDomain2(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		configureTest(baseUrl);
		String calendar = "user2@domain2.org";
		AccessToken token = loginClient.login(calendar, "user2");
		
		assertThat(token).isNotNull();
	}
	
	private void assertThatTokenIsWellFormed(AccessToken token) {
		assertThat(token.getCalendarRights()).isNull();
		assertThat(token.getConversationUid()).isEqualTo(0);
		assertThat(token.getDomain()).isEqualTo(
				ObmDomain.builder()
				         .name("domain.org")
				         .uuid(ObmDomainUuid.of("b55911e6-6848-4f16-abd4-52d94b6901a6"))
				         .build());
		assertThat(token.getIsoCodeToNameCache()).isEmpty();
		assertThat(token.getObmId()).isEqualTo(0);
		assertThat(token.getOrigin()).isEqualTo("integration-testing");
		assertThat(token.getServerCapabilities()).isEmpty();
		assertThat(token.getServiceProperties()).isEmpty();
		assertThat(token.getSessionId()).isNotNull().isNotEmpty();
		assertThat(token.getUserDisplayName()).isEqualTo("Firstname Lastname");
		assertThat(token.getUserEmail()).isEqualTo("user1@domain.org");
		assertThat(token.getUserLogin()).isEqualToIgnoringCase("user1");
		assertThat(token.getUserSettings()).isEqualTo(null);
		assertThat(token.getUserWithDomain()).isEqualToIgnoringCase("user1@domain.org");
		assertThat(token.getVersion()).isEqualTo(new MavenVersion("2", "5", "0"));
		assertThat(token.isRootAccount()).isFalse();
	}
}
