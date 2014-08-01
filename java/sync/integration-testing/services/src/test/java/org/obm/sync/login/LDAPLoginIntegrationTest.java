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
package org.obm.sync.login;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URL;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.push.arquillian.ManagedTomcatGuiceArquillianRunner;
import org.obm.push.arquillian.extension.deployment.DeployForEachTests;
import org.obm.sync.ObmSyncArchiveUtils;
import org.obm.sync.ObmSyncIntegrationTest;
import org.obm.sync.ServicesClientAndEmbeddedLDAPModule;
import org.obm.sync.ServicesClientModule.ArquillianLocatorService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.MavenVersion;
import org.obm.sync.client.login.LoginClient;
import org.opends.messages.Message;
import org.opends.server.backends.MemoryBackend;
import org.opends.server.core.AddOperation;
import org.opends.server.core.DirectoryServer;
import org.opends.server.protocols.internal.InternalClientConnection;
import org.opends.server.types.DN;
import org.opends.server.types.Entry;
import org.opends.server.types.LDIFImportConfig;
import org.opends.server.types.ResultCode;
import org.opends.server.util.LDIFReader;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

@RunWith(ManagedTomcatGuiceArquillianRunner.class)
@GuiceModule(ServicesClientAndEmbeddedLDAPModule.class)
public class LDAPLoginIntegrationTest extends ObmSyncIntegrationTest {

	@Inject ArquillianLocatorService locatorService;
	@Inject LoginClient loginClient;
	@Inject DirectoryServer directoryServer;
	
	@Test
	@RunAsClient
	public void testDoLoginSuccess(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login("user1@domain.org", "user1");
		
		assertThat(token).isNotNull();
		assertThatTokenIsWellFormed(token);
	}

	@Test
	@RunAsClient
	public void testDoLoginIsCaseInsensitive(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login("UseR1@domain.org", "user1");

		assertThat(token).isNotNull();
		assertThatTokenIsWellFormed(token);
	}

	@Test
	@RunAsClient
	public void testDoLoginWithoutDomainSuccess(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login("user1", "user1");

		assertThat(token).isNotNull();
		assertThatTokenIsWellFormed(token);
	}

	@Test
	@RunAsClient
	public void testDoLoginWithoutDomainIsCaseInsensitive(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login("UseR1", "user1");

		assertThat(token).isNotNull();
		assertThatTokenIsWellFormed(token);
	}

	@Test(expected=AuthFault.class)
	@RunAsClient
	public void testDoLoginFailsWithWrongLogin(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		try {
			loginClient.login("user@domain.org", "user1");
		} catch(AuthFault e) {
			assertThat(e.getMessage()).contains("javax.naming.AuthenticationException: User with login 'user' not found in LDAP directory.");
			throw e;
		}
	}

	@Test(expected=AuthFault.class)
	@RunAsClient
	public void testDoLoginFailsWithWrongPassword(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		try {
			loginClient.login("user1", "user");
		} catch(AuthFault e) {
			assertThat(e.getMessage()).contains("javax.naming.AuthenticationException: [LDAP: error code 49 - Invalid Credentials]");
			throw e;
		}
	}

	@Test
	@RunAsClient
	public void testDoLoginSuccessForUser2onDomain1(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login("user2@domain.org", "user2");

		assertThat(token).isNotNull();
	}

	@Test(expected=AuthFault.class)
	@RunAsClient
	public void testDoLoginFailsForUser2WithoutDomain(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		try {
			loginClient.login("user2", "user2");
		} catch(AuthFault e) {
			assertThat(e.getMessage()).contains("The login user2 is in several domains (at least domain.org and  domain2.org).");
			throw e;
		}
	}

	@Test
	@RunAsClient
	public void testDoLoginSuccessForUser2onDomain2(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		locatorService.configure(baseUrl);

		AccessToken token = loginClient.login("user2@domain2.org", "user2");

		assertThat(token).isNotNull();
	}

	@Test(expected = RuntimeException.class)
	@RunAsClient
	public void testDoLoginDoesNotThrowAuthFaultOnConnectionError(@ArquillianResource @OperateOnDeployment(ARCHIVE) URL baseUrl) throws Exception {
		tearDown();

		locatorService.configure(baseUrl);
		loginClient.login("user1@domain.org", "user1");
	}

	private void assertThatTokenIsWellFormed(AccessToken token) {
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
		assertThat(token.getVersion()).isEqualTo(new MavenVersion("2", "9", "4"));
		assertThat(token.isRootAccount()).isFalse();
	}

	@Before
	public void setup() throws Exception {
		directoryServer.startServer();

		initializeBackend();
		loadLdifBootstrap("bootstrap.ldif");
		loadLdifBootstrap("test-domains.ldif");
		loadLdifBootstrap("test-users.ldif");
	}

	private void initializeBackend() throws Exception {
		MemoryBackend memoryBackend = new MemoryBackend();

		memoryBackend.setBackendID("test");
		memoryBackend.setBaseDNs(new DN[] {DN.decode("dc=local")});
		memoryBackend.initializeBackend();

		DirectoryServer.registerBackend(memoryBackend);
	}

	private void loadLdifBootstrap(String file) throws Exception {
		Entry entry;
		LDIFReader reader = new LDIFReader(new LDIFImportConfig(new AutoCloseInputStream(ClassLoader.getSystemClassLoader().getResourceAsStream(file))));

		while ((entry = reader.readEntry()) != null) {
			addEntry(entry);
		}
	}

	private void addEntry(Entry entry) {
		InternalClientConnection conn = InternalClientConnection.getRootConnection();
		AddOperation addOperation = conn.processAdd(entry.getDN(), entry.getObjectClasses(), entry.getUserAttributes(),
				entry.getOperationalAttributes());
		
		Preconditions.checkState(addOperation.getResultCode() == ResultCode.SUCCESS, "Unable to add bootstrap entry " + entry.getDN());
	}

	@After
	public void tearDown() {
		try {
			DirectoryServer.shutDown(LDAPLoginIntegrationTest.class.toString(), Message.EMPTY);
		} catch (Exception e) {
			// Ahem...
		}
	}

	@DeployForEachTests
	@Deployment(managed=false, name=ARCHIVE)
	public static WebArchive createDeployment() {
		return ObmSyncArchiveUtils.createDeploymentWithLDAPAuth();
	}

}
