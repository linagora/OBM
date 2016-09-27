/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2016 Linagora
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
package org.obm.provisioning.addressbook;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.guava.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.obm.provisioning.ProvisioningIntegrationTestUtils.commitBatch;
import static org.obm.provisioning.ProvisioningIntegrationTestUtils.createAddressBook;
import static org.obm.provisioning.ProvisioningIntegrationTestUtils.startBatch;
import static org.obm.provisioning.ProvisioningIntegrationTestUtils.waitForBatchSuccess;

import java.net.URL;
import java.sql.ResultSet;

import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.domain.dao.AddressBookDao;
import org.obm.domain.dao.UserDao;
import org.obm.guice.GuiceRule;
import org.obm.provisioning.TestingProvisioningModule;
import org.obm.server.WebServer;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.AddressBook;
import org.obm.sync.book.AddressBook.Id;
import org.obm.sync.book.AddressBookReference;
import org.obm.sync.host.ObmHost;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.linagora.obm.sync.JMSServer;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserEmails;
import fr.aliacom.obm.common.user.UserExtId;
import fr.aliacom.obm.common.user.UserLogin;

public class AddressBookIntegrationTest {

	@Rule public TestRule chain = RuleChain
			.outerRule(new GuiceRule(this, new TestingProvisioningModule()))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "dbInitialScriptEvent.sql"));

	@Inject private H2InMemoryDatabase db;
	@Inject private WebServer server;
	@Inject private JMSServer jmsServer;
	
	@Inject private UserDao userDao;
	@Inject private AddressBookDao addressBookDao;
	
	private URL baseURL;
	private AccessToken token;
	private ObmUser obmUser;
	
	@Before
	public void init() throws Exception {
		server.start();
		baseURL = new URL("http", "localhost", server.getHttpPort(), "/");
		
		ObmDomain domain = ObmDomain.builder().name("test.tlse.lng").id(2).build();
		UserLogin login = UserLogin.valueOf("user-with-books");
		ObmHost mailHost = ObmHost.builder().id(1).build();
		UserEmails emails = UserEmails.builder().domain(domain).addAddress(login.getStringValue()).server(mailHost).build();
		obmUser = userDao.create(ObmUser.builder().login(login).emails(emails).domain(domain).extId(UserExtId.valueOf("extId123")).build());
		token = new AccessToken(obmUser.getUid(), "papi");
		token.setDomain(domain);
	}

	@After
	public void tearDown() throws Exception {
		jmsServer.stop();
		server.stop();
	}
	
	@Test
	public void testPostWhenTheUserIsUnknown() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = startBatch(baseURL, obmDomainUuid);
		
		String json = "{}";
		createAddressBook(json, "unexisting_user@test.tlse.lng");
		commitBatch();
		waitForBatchSuccess(batchId, 1, 0);

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("{"
					+ "\"id\":" + batchId + ","
					+ "\"status\":\"SUCCESS\","
					+ "\"operationCount\":1,"
					+ "\"operationDone\":0,"
					+ "\"operations\":["
						+ "{\"status\":\"ERROR\","
						+ "\"entityType\":\"ADDRESS_BOOK\","
						+ "\"entity\":" + json + ","
						+ "\"operation\":\"POST\","
						+ "\"error\":\"org.obm.provisioning.exception.ProcessingException: "
							+ "org.obm.provisioning.dao.exceptions.UserNotFoundException: "
							+ "The user with login unexisting_user@test.tlse.lng with domain id ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6 was not found\"}"
					+ "]"
				+ "}")).
		when()
			.get("");
	}
	
	@Test
	public void testPostShouldCreateReferenceWhenPrimaryWithReference() throws Exception {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");

		String json = "{"
				+ "\"name\":\"the primary address book name can't be choosen\","
				+ "\"role\":\"primary\","
				+ "\"reference\": {"
					+ "\"value\":\"1234\","
					+ "\"origin\":\"exchange\""
				+ "}"
			+ "}";
		
		String batchId = startBatch(baseURL, obmDomainUuid);
		createAddressBook(json, obmUser.getLoginAtDomain());
		commitBatch();
		waitForBatchSuccess(batchId);

		Optional<Id> idByReference = addressBookDao.findByReference(new AddressBookReference("1234", "exchange"));
		assertThat(idByReference).isPresent();
		assertThatAddressBookIsSynced(idByReference.get());
		assertThat(addressBookDao.get(idByReference.get())).isEqualTo(AddressBook.builder()
				.uid(idByReference.get())
				.name("contacts")
				.origin("provisioning")
				.defaultBook(true)
				.readOnly(false)
				.syncable(true)
				.build());
	}
	
	@Test
	public void testPostShouldCreateReferenceWhenCollectedWithReference() throws Exception {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");

		String json = "{"
				+ "\"name\":\"the collected address book name can't be choosen\","
				+ "\"role\":\"collected\","
				+ "\"reference\": {"
					+ "\"value\":\"1234\","
					+ "\"origin\":\"exchange\""
				+ "}"
			+ "}";
		
		String batchId = startBatch(baseURL, obmDomainUuid);
		createAddressBook(json, obmUser.getLoginAtDomain());
		commitBatch();
		waitForBatchSuccess(batchId);

		Optional<Id> idByReference = addressBookDao.findByReference(new AddressBookReference("1234", "exchange"));
		assertThat(idByReference).isPresent();
		assertThat(addressBookDao.get(idByReference.get())).isEqualTo(AddressBook.builder()
				.uid(idByReference.get())
				.name("collected_contacts")
				.origin("provisioning")
				.defaultBook(true)
				.readOnly(false)
				.syncable(true)
				.build());
	}
	
	@Test
	public void testPostShouldCreateWhenCustomWithReference() throws Exception {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = startBatch(baseURL, obmDomainUuid);

		String json = "{"
				+ "\"name\":\"custom book\","
				+ "\"role\":\"custom\","
				+ "\"reference\": {"
					+ "\"value\":\"1234\","
					+ "\"origin\":\"exchange\""
				+ "}"
			+ "}";
		
		createAddressBook(json, obmUser.getLoginAtDomain());

		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("{"
					+ "\"id\":" + batchId + ","
					+ "\"status\":\"IDLE\","
					+ "\"operationCount\":1,"
					+ "\"operationDone\":0,"
					+ "\"operations\":["
						+ "{\"status\":\"IDLE\","
						+ "\"entityType\":\"ADDRESS_BOOK\","
						+ "\"entity\":" + json + ","
						+ "\"operation\":\"POST\","
						+ "\"error\":null}"
					+ "]"
				+ "}")).
		when()
			.get("");
		
		commitBatch();
		waitForBatchSuccess(batchId);
		
		given()
			.auth().basic("admin0@global.virt", "admin0").
		expect()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("{"
					+ "\"id\":" + batchId + ","
					+ "\"status\":\"SUCCESS\","
					+ "\"operationCount\":1,"
					+ "\"operationDone\":1,"
					+ "\"operations\":["
						+ "{\"status\":\"SUCCESS\","
						+ "\"entityType\":\"ADDRESS_BOOK\","
						+ "\"entity\":" + json + ","
						+ "\"operation\":\"POST\","
						+ "\"error\":null}"
					+ "]"
				+ "}")).
		when()
			.get("");

		Optional<Id> idByReference = addressBookDao.findByReference(new AddressBookReference("1234", "exchange"));
		assertThat(idByReference).isPresent();
		assertThatAddressBookIsSynced(idByReference.get());
		assertThat(addressBookDao.get(idByReference.get())).isEqualTo(AddressBook.builder()
				.uid(idByReference.get())
				.name("custom book")
				.origin("provisioning")
				.defaultBook(false)
				.readOnly(false)
				.syncable(true)
				.build());
	}
	
	@Test
	public void testPostShouldRenameWhenCustomWithKnownReference() throws Exception {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");

		String creationJson = "{"
				+ "\"name\":\"creation name\","
				+ "\"role\":\"custom\","
				+ "\"reference\": {"
					+ "\"value\":\"1234\","
					+ "\"origin\":\"exchange\""
				+ "}"
			+ "}";

		String updatingJson = "{"
				+ "\"name\":\"updating name\","
				+ "\"role\":\"custom\","
				+ "\"reference\": {"
					+ "\"value\":\"1234\","
					+ "\"origin\":\"exchange\""
				+ "}"
			+ "}";
		
		String batchId1 = startBatch(baseURL, obmDomainUuid);
		createAddressBook(creationJson, obmUser.getLoginAtDomain());
		commitBatch();
		waitForBatchSuccess(batchId1);

		String batchId2 = startBatch(baseURL, obmDomainUuid);
		createAddressBook(updatingJson, obmUser.getLoginAtDomain());
		commitBatch();
		waitForBatchSuccess(batchId2);

		Optional<Id> idByReference = addressBookDao.findByReference(new AddressBookReference("1234", "exchange"));
		assertThat(idByReference).isPresent();
		assertThatAddressBookIsSynced(idByReference.get());
		assertThat(addressBookDao.get(idByReference.get())).isEqualTo(AddressBook.builder()
				.uid(idByReference.get())
				.name("updating name")
				.origin("provisioning")
				.defaultBook(false)
				.readOnly(false)
				.syncable(true)
				.build());
	}

	private void assertThatAddressBookIsSynced(Id addressBookId) throws Exception {
		ResultSet results = db.execute("select count(1) from syncedaddressbook "
				+ "where addressbook_id=" + addressBookId.getId() + " and user_id=" + obmUser.getUid());
		results.next();
		assertThat(results.getInt(1)).isEqualTo(1);
	}

}
