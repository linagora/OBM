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
package org.obm.provisioning.contact;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.obm.provisioning.ProvisioningIntegrationTestUtils.commitBatch;
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
import org.obm.SolrModuleUtils.DummyCommonsHttpSolrServer;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.domain.dao.AddressBookDao;
import org.obm.guice.GuiceRule;
import org.obm.provisioning.TestingProvisioningModule;
import org.obm.provisioning.beans.Request;
import org.obm.server.WebServer;
import org.obm.sync.book.AddressBook;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jayway.restassured.http.ContentType;
import com.linagora.obm.sync.JMSServer;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserLogin;

public class ContactIntegrationTest {

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
	@Inject private DummyCommonsHttpSolrServer solrServer;
	@Inject private AddressBookDao addressBookDao;
	
	private URL baseURL;
	private ObmUser obmUser;
	
	@Before
	public void init() throws Exception {
		server.start();
		baseURL = new URL("http", "localhost", server.getHttpPort(), "/");
		
		ObmDomain domain = ObmDomain.builder().id(2).name("test.tlse.lng").build();
		obmUser = ObmUser.builder().uid(2).login(UserLogin.valueOf("user1")).domain(domain).build();
		AddressBook defaultAddressBook = addressBookDao.create(AddressBook.builder()
			.origin("papi")
			.name("contacts")
			.defaultBook(true)
			.syncable(true)
			.build(), obmUser);
		addressBookDao.enableAddressBookSynchronization(defaultAddressBook.getUid(), obmUser);
	}

	@After
	public void tearDown() throws Exception {
		jmsServer.stop();
		server.stop();
	}
	
	@Test
	public void testImportVCFWhenTheUserIsUnknown() throws Exception {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = startBatch(baseURL, obmDomainUuid);
		
		String vcf = Resources.toString(Resources.getResource("vcf/sample.vcf"), Charsets.UTF_8);
		String expectedVCF = vcf.replaceAll("\n", "\\\\n");
		
		importVCF(vcf, "unexisting_user@test.tlse.lng");
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
						+ "\"entityType\":\"CONTACT\","
						+ "\"entity\":\"" + expectedVCF + "\","
						+ "\"operation\":\"POST\","
						+ "\"error\":\"org.obm.provisioning.exception.ProcessingException: "
							+ "org.obm.provisioning.dao.exceptions.UserNotFoundException: "
							+ "The user with login unexisting_user@test.tlse.lng with domain id ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6 was not found\"}"
					+ "]"
				+ "}")).
		when()
			.get("");
		
		assertThat(solrServer.addCount).isEqualTo(0);
		assertThat(solrServer.commitCount).isEqualTo(0);
	}
	
	@Test
	public void testImportVCF() throws Exception {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String batchId = startBatch(baseURL, obmDomainUuid);
		
		String vcf = Resources.toString(Resources.getResource("vcf/sample.vcf"), Charsets.UTF_8);
		String expectedVCF = vcf.replaceAll("\n", "\\\\n");
		
		importVCF(vcf);

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
						+ "\"entityType\":\"CONTACT\","
						+ "\"entity\":\"" + expectedVCF + "\","
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
						+ "\"entityType\":\"CONTACT\","
						+ "\"entity\":\"" + expectedVCF + "\","
						+ "\"operation\":\"POST\","
						+ "\"error\":null}"
					+ "]"
				+ "}")).
		when()
			.get("");

		ResultSet results = db.execute("select count(1) from contact");
		results.next();
		assertThat(results.getInt(1)).isEqualTo(2);
		// expect 3 indexed documents, 2 contacts and 1 event for the birthday
		assertThat(solrServer.addCount).isEqualTo(3);
		assertThat(solrServer.commitCount).isEqualTo(3);
	}
	
	@Test
	public void testImportVCFShouldBeRefusedWhenTrackingIsUsedButVCFContainsTwoVcards() throws Exception {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String vcf = Resources.toString(Resources.getResource("vcf/sample.vcf"), Charsets.UTF_8);
		
		String batchId = startBatch(baseURL, obmDomainUuid);
		importVCFAsTracked(vcf, "ref1", "2016-09-06T07:51:20Z");
		commitBatch();
		waitForBatchSuccess(batchId, 1, 0);
		
		ResultSet results = db.execute("select count(1) from contact");
		results.next();
		assertThat(results.getInt(1)).isEqualTo(0);
		assertThat(solrServer.addCount).isEqualTo(0);
		assertThat(solrServer.commitCount).isEqualTo(0);
	}
	
	@Test
	public void testImportVCFShouldBeDoneWhenTrackingIsUnknown() throws Exception {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String vcf = Resources.toString(Resources.getResource("vcf/only-one-vcard.vcf"), Charsets.UTF_8);
		
		String batchId = startBatch(baseURL, obmDomainUuid);
		importVCFAsTracked(vcf, "ref1", "2016-09-06T07:51:20Z");
		commitBatch();
		waitForBatchSuccess(batchId);

		ResultSet results = db.execute("select count(1) from contact");
		results.next();
		assertThat(results.getInt(1)).isEqualTo(1);
		assertThat(solrServer.addCount).isEqualTo(1);
		assertThat(solrServer.commitCount).isEqualTo(1);
	}
	
	@Test
	public void testImportVCFShouldBeDoneOnlyOnceWhenTheSameTrackingIsUsedTwice() throws Exception {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String vcf = Resources.toString(Resources.getResource("vcf/only-one-vcard.vcf"), Charsets.UTF_8);
		String vcf2 = vcf.replace("Shrimp Man", "New title");
		
		String batchId1 = startBatch(baseURL, obmDomainUuid);
		importVCFAsTracked(vcf, "ref1", "2016-09-06T07:51:20Z");
		commitBatch();
		waitForBatchSuccess(batchId1);
		String batchId2 = startBatch(baseURL, obmDomainUuid);
		importVCFAsTracked(vcf2, "ref1", "2016-09-06T07:51:20Z");
		commitBatch();
		waitForBatchSuccess(batchId2);

		ResultSet results = db.execute("select count(1), contact_title from contact");
		results.next();
		assertThat(results.getInt(1)).isEqualTo(1);
		assertThat(results.getString(2)).isEqualTo("Shrimp Man");
		assertThat(solrServer.addCount).isEqualTo(1);
		assertThat(solrServer.commitCount).isEqualTo(1);
	}
	
	@Test
	public void testImportVCFShouldUpdateWhenTheSameTrackingIsUsedWithDateAfter() throws Exception {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String vcf = Resources.toString(Resources.getResource("vcf/only-one-vcard.vcf"), Charsets.UTF_8);
		String vcf2 = vcf.replace("Shrimp Man", "New title");
		
		String batchId1 = startBatch(baseURL, obmDomainUuid);
		importVCFAsTracked(vcf, "ref1", "2016-09-06T07:51:20Z");
		commitBatch();
		waitForBatchSuccess(batchId1);
		String batchId2 = startBatch(baseURL, obmDomainUuid);
		importVCFAsTracked(vcf2, "ref1", "2016-09-10T07:51:20Z");
		commitBatch();
		waitForBatchSuccess(batchId2);

		ResultSet results = db.execute("select count(1), contact_title from contact");
		results.next();
		assertThat(results.getInt(1)).isEqualTo(1);
		assertThat(results.getString(2)).isEqualTo("New title");
		assertThat(solrServer.addCount).isEqualTo(2);
		assertThat(solrServer.commitCount).isEqualTo(2);
	}
	
	@Test
	public void testImportVCFShouldUpdateWhenTheSameTrackingIsUsedWithDateBefore() throws Exception {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		String vcf = Resources.toString(Resources.getResource("vcf/only-one-vcard.vcf"), Charsets.UTF_8);
		String vcf2 = vcf.replace("Shrimp Man", "New title");

		String batchId1 = startBatch(baseURL, obmDomainUuid);
		importVCFAsTracked(vcf, "ref1", "2016-09-06T07:51:20Z");
		commitBatch();
		waitForBatchSuccess(batchId1);
		String batchId2 = startBatch(baseURL, obmDomainUuid);
		importVCFAsTracked(vcf2, "ref1", "2016-09-02T07:51:20Z");
		commitBatch();
		waitForBatchSuccess(batchId2);

		ResultSet results = db.execute("select count(1), contact_title from contact");
		results.next();
		assertThat(results.getInt(1)).isEqualTo(1);
		assertThat(results.getString(2)).isEqualTo("Shrimp Man");
		assertThat(solrServer.addCount).isEqualTo(1);
		assertThat(solrServer.commitCount).isEqualTo(1);
	}
	
	@Test
	public void testImportVCFWhenUserIsNotAdmin() {
		ObmDomainUuid obmDomainUuid = ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		
		startBatch(baseURL, obmDomainUuid);
		given()
			.auth().basic("user1@test.tlse.lng", "user1")
			.body("THE VCF").contentType(ContentType.TEXT).
		expect()
			.statusCode(Status.UNAUTHORIZED.getStatusCode()).
		when()
			.post("contacts/user1@test.tlse.lng");
	}

	private void importVCF(String vcf) {
		importVCF(vcf, obmUser.getLoginAtDomain());
	}
	
	private void importVCF(String vcf, String userEmail) {
		given()
			.auth().basic("admin0@global.virt", "admin0")
			.body(vcf).contentType(ContentType.TEXT).
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.post("contacts/" + userEmail);
	}
	
	private void importVCFAsTracked(String vcf, String ref, String date) {
		given()
			.auth().basic("admin0@global.virt", "admin0")
			.body(vcf).contentType(ContentType.TEXT)
			.queryParam(Request.TRACKING_REF, ref)
			.queryParam(Request.TRACKING_DATE, date).
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.post("contacts/" + obmUser.getLoginAtDomain());
	}

}
