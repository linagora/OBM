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

package org.obm.imap.archive.resources;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.obm.imap.archive.DBData.admin;
import static org.obm.imap.archive.DBData.domain;
import static org.obm.imap.archive.DBData.domainId;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.mail.Flags;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.core.Response.Status;

import org.hamcrest.Matchers;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.obm.dao.utils.H2Destination;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.CyrusCompatGreenmailRule;
import org.obm.imap.archive.DatabaseOperations;
import org.obm.imap.archive.Expectations;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.imap.archive.beans.ArchiveTreatmentKind;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.ConfigurationState;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.dto.DomainConfigurationDto;
import org.obm.server.WebServer;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.icegreen.greenmail.imap.AuthorizationException;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.store.MailFolder;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.jayway.awaitility.Duration;
import com.jayway.restassured.config.RedirectConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.http.ContentType;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;

public class TreatmentsResourceTest {

	private ClientDriverRule driver = new ClientDriverRule();

	@Rule public TestRule chain = RuleChain
			.outerRule(driver)
			.around(new TemporaryFolder())
			.around(new GuiceRule(this, new TestImapArchiveModules.WithGreenmail(driver, new Provider<TemporaryFolder>() {

				@Override
				public TemporaryFolder get() {
					return temporaryFolder;
				}
				
			})))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "sql/initial.sql"))
			.around(new CyrusCompatGreenmailRule(new Provider<GreenMail>() {

				@Override
				public GreenMail get() {
					return imapServer;
				}
				
			}));
	
	@Inject TemporaryFolder temporaryFolder;
	@Inject H2InMemoryDatabase db;
	@Inject WebServer server;
	@Inject GreenMail imapServer;
	Expectations expectations;

	@Before
	public void setUp() {
		expectations = new Expectations(driver);
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
		imapServer.stop();
	}

	private void play(Operation operation) {
		DbSetup dbSetup = new DbSetup(H2Destination.from(db), operation);
		dbSetup.launch();
	}
	
	@Test
	public void calculateNextScheduledDateShouldReturnNoContentWhenConfigurationInactive() throws Exception {
		expectations
			.expectTrustedLogin(domain);
		
		server.start();
		
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = domainId.getUUID();
		domainConfigurationDto.enabled = false;
		domainConfigurationDto.repeatKind = RepeatKind.DAILY.toString();
		domainConfigurationDto.dayOfWeek = DayOfWeek.MONDAY.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = DayOfMonth.of(1).getDayIndex();
		domainConfigurationDto.dayOfYear = DayOfYear.of(1).getDayOfYear();
		domainConfigurationDto.hour = 0;
		domainConfigurationDto.minute = 0;
		domainConfigurationDto.archiveMainFolder = "arChive";
		domainConfigurationDto.scopeUserIdToLoginMap = ImmutableMap.of();
		domainConfigurationDto.scopeSharedMailboxIdToNameMap = ImmutableMap.of();
		domainConfigurationDto.mailingEmails = ImmutableList.of();
		
		given()
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.contentType(ContentType.JSON)
			.body(domainConfigurationDto).
		expect()
			.statusCode(Status.NO_CONTENT.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments/next");
	}
	
	@Test
	public void calculateNextScheduledDateShouldReturnNextTreatmentDateWhenConfigurationActive() throws Exception {
		expectations
			.expectTrustedLogin(domain);
		
		server.start();
		
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = domainId.getUUID();
		domainConfigurationDto.enabled = true;
		domainConfigurationDto.repeatKind = RepeatKind.DAILY.toString();
		domainConfigurationDto.dayOfWeek = DayOfWeek.MONDAY.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = DayOfMonth.of(1).getDayIndex();
		domainConfigurationDto.dayOfYear = DayOfYear.of(1).getDayOfYear();
		domainConfigurationDto.hour = 0;
		domainConfigurationDto.minute = 0;
		domainConfigurationDto.archiveMainFolder = "arChive";
		domainConfigurationDto.scopeUserIdToLoginMap = ImmutableMap.of();
		domainConfigurationDto.scopeSharedMailboxIdToNameMap = ImmutableMap.of();
		domainConfigurationDto.mailingEmails = ImmutableList.of();
		
		given()
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.contentType(ContentType.JSON)
			.body(domainConfigurationDto).
		expect()
			.contentType(ContentType.JSON)
			.body("nextTreatmentDate", equalTo("2014-06-19T00:00:00.000Z"))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments/next");
	}
	
	@Test
	public void startArchivingShouldReturnNotFoundWhenNoConfiguration() throws Exception {
		expectations
			.expectTrustedLogin(domain);
		
		server.start();
		
		given()
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.queryParam("archive_treatment_kind", ArchiveTreatmentKind.REAL_RUN).
		expect()
			.statusCode(Status.NOT_FOUND.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");
	}
	
	@Test
	public void startArchivingShouldReturnConflictWhenConfigurationIsDisable() throws Exception {
		expectations
			.expectTrustedLogin(domain);
		
		play(Operations.sequenceOf(DatabaseOperations.cleanDB(),
				DatabaseOperations.insertDomainConfiguration(domainId, ConfigurationState.DISABLE)));
		
		server.start();
		
		given()
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.queryParam("archive_treatment_kind", ArchiveTreatmentKind.REAL_RUN).
		expect()
			.statusCode(Status.CONFLICT.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");
	}
	
	@Test
	public void startArchivingShouldCreate() throws Exception {
		expectations
			.expectTrustedLogin(domain);
		
		play(Operations.sequenceOf(DatabaseOperations.cleanDB(),
				DatabaseOperations.insertDomainConfiguration(domainId, ConfigurationState.ENABLE)));
		
		server.start();
		
		UUID expectedRunId = TestImapArchiveModules.uuid;
		given()
			.config(RestAssuredConfig.config().redirect(RedirectConfig.redirectConfig().followRedirects(false)))
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.queryParam("archive_treatment_kind", ArchiveTreatmentKind.REAL_RUN).
		expect()
			.header("Location", containsString("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments/" + expectedRunId.toString()))
			.statusCode(Status.SEE_OTHER.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");
	}
	
	@Test
	public void startArchivingShouldRedirect() throws Exception {
		expectations
			.expectTrustedLogin(domain)
			.expectTrustedLogin(domain);
		
		play(Operations.sequenceOf(DatabaseOperations.cleanDB(),
				DatabaseOperations.insertDomainConfiguration(domainId, ConfigurationState.ENABLE)));
		
		server.start();
		
		given()
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.queryParam("archive_treatment_kind", ArchiveTreatmentKind.REAL_RUN).
		expect()
			.contentType(ContentType.JSON)
			.body("runId", equalTo(TestImapArchiveModules.uuid.toString()))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");
	}
	
	@Test
	public void startArchivingShouldProcessARealRunWhenMissingArchiveTreatmentKind() throws Exception {
		expectations
			.expectTrustedLoginAnyTime(domain);
		
		play(Operations.sequenceOf(DatabaseOperations.cleanDB(),
				DatabaseOperations.insertDomainConfiguration(domainId, ConfigurationState.ENABLE)));
		
		server.start();
		
		UUID expectedRunId = TestImapArchiveModules.uuid;
		given()
			.config(RestAssuredConfig.config().redirect(RedirectConfig.redirectConfig().followRedirects(false)))
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue()).
		expect()
			.header("Location", containsString("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments/" + expectedRunId.toString()))
			.statusCode(Status.SEE_OTHER.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");
		
		await()
			.atMost(1, TimeUnit.MINUTES)
			.pollDelay(Duration.ONE_SECOND)
			.pollInterval(Duration.ONE_SECOND)
			.until(endOfTheTreatment(expectedRunId));

		given()
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.contentType(ContentType.JSON).
		expect()
			.body(containsString("Starting IMAP Archive in REAL_RUN for domain mydomain")).
		when()
			.get("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments/" + expectedRunId.toString() + "/logs");
	}
	
	@Test
	public void startArchivingShouldProcessADryRunWhenArchiveTreatmentKindIsDry() throws Exception {
		expectations
			.expectTrustedLogin(domain)
			.expectTrustedLogin(domain);
		
		play(Operations.sequenceOf(DatabaseOperations.cleanDB(),
				DatabaseOperations.insertDomainConfiguration(domainId, ConfigurationState.ENABLE)));
		
		server.start();
		
		UUID expectedRunId = TestImapArchiveModules.uuid;
		given()
			.config(RestAssuredConfig.config().redirect(RedirectConfig.redirectConfig().followRedirects(false)))
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.queryParam("archive_treatment_kind", ArchiveTreatmentKind.DRY_RUN).
		expect()
			.header("Location", containsString("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments/" + expectedRunId.toString()))
			.statusCode(Status.SEE_OTHER.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");
		given()
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.contentType(ContentType.JSON).
		expect()
			.body(containsString("Starting IMAP Archive in DRY_RUN for domain mydomain")).
		when()
			.get("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments/" + expectedRunId.toString() + "/logs");
	}
	
	@Test
	public void startArchivingTwiceShouldStackSchedules() throws Exception {
		expectations
			.expectTrustedLoginAnyTime(domain);
		
		play(Operations.sequenceOf(DatabaseOperations.cleanDB(),
				DatabaseOperations.insertDomainConfiguration(domainId, ConfigurationState.ENABLE)));
		
		server.start();
		
		UUID expectedRunId = TestImapArchiveModules.uuid;
		UUID expectedRunId2 = TestImapArchiveModules.uuid2;
		given()
			.config(RestAssuredConfig.config().redirect(RedirectConfig.redirectConfig().followRedirects(false)))
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.queryParam("archive_treatment_kind", ArchiveTreatmentKind.REAL_RUN).
		expect()
			.header("Location", containsString("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments/" + expectedRunId.toString()))
			.statusCode(Status.SEE_OTHER.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");

		await()
			.atMost(1, TimeUnit.MINUTES)
			.pollDelay(Duration.ONE_SECOND)
			.pollInterval(Duration.ONE_SECOND)
			.until(endOfTheTreatment(expectedRunId));

		given()
			.config(RestAssuredConfig.config().redirect(RedirectConfig.redirectConfig().followRedirects(false)))
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.queryParam("archive_treatment_kind", ArchiveTreatmentKind.REAL_RUN).
		expect()
			.header("Location", containsString("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments/" + expectedRunId2.toString()))
			.statusCode(Status.SEE_OTHER.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");
	}
	
	@Test
	public void getShouldReturnNotImplemented() throws Exception {
		expectations
		.expectTrustedLogin(domain);
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("7624b49f-4eb8-4b79-a396-c814ee5039bd");
		play(Operations.sequenceOf(DatabaseOperations.insertDomainConfiguration(domainId, ConfigurationState.ENABLE),
				DatabaseOperations.insertArchiveTreatment(runId, domainId)));
		
		server.start();
		
		given()
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.contentType(ContentType.JSON).
		expect()
			.statusCode(Status.NOT_IMPLEMENTED.getStatusCode()).
		when()
		.get("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");
	}
	
	@Test
	public void getShouldReturnEmptyListWhenNoTreatments() throws Exception {
		expectations
			.expectTrustedLogin(domain);
		
		play(Operations.sequenceOf(DatabaseOperations.cleanDB(),
				DatabaseOperations.insertDomainConfiguration(domainId, ConfigurationState.ENABLE)));
		server.start();
		
		given()
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.contentType(ContentType.JSON)
			.queryParam("filter_terminated", true).
		expect()
			.contentType(ContentType.JSON)
			.body("archiveTreatmentDtos", Matchers.emptyIterable())
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");
	}
	
	@Test
	public void getShouldReturnTheOnlyOne() throws Exception {
		expectations
			.expectTrustedLogin(domain);
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("7624b49f-4eb8-4b79-a396-c814ee5039bd");
		play(Operations.sequenceOf(DatabaseOperations.cleanDB(),
				DatabaseOperations.insertDomainConfiguration(domainId, ConfigurationState.ENABLE),
				DatabaseOperations.insertArchiveTreatment(runId, domainId)));
		
		server.start();
		
		given()
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.contentType(ContentType.JSON)
			.queryParam("filter_terminated", true).
		expect()
			.contentType(ContentType.JSON)
			.body("archiveTreatmentDtos.runId", hasItem(runId.serialize()))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");
	}
	
	@Test
	public void getShouldReturnMultiple() throws Exception {
		expectations
			.expectTrustedLogin(domain);
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("7624b49f-4eb8-4b79-a396-c814ee5039bd");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("049bdc76-f991-4e40-ad96-1aeb3d9d3bae");
		ArchiveTreatmentRunId runId3 = ArchiveTreatmentRunId.from("a8dc4c16-bc23-4f9f-9eb0-a0f18ff3f3b2");
		play(Operations.sequenceOf(DatabaseOperations.cleanDB(),
				DatabaseOperations.insertDomainConfiguration(domainId, ConfigurationState.ENABLE),
				DatabaseOperations.insertArchiveTreatment(runId, domainId),
				DatabaseOperations.insertArchiveTreatment(runId2, domainId),
				DatabaseOperations.insertArchiveTreatment(runId3, domainId)));
		
		server.start();
		
		given()
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.contentType(ContentType.JSON)
			.queryParam("filter_terminated", true).
		expect()
			.contentType(ContentType.JSON)
			.body("archiveTreatmentDtos.runId", hasItems(runId.serialize(), runId2.serialize(), runId3.serialize()))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");
	}
	
	@Test
	public void resetShouldThrowWhenNotInTestingMode() throws Exception {
		expectations
			.expectTrustedLogin(domain)
			.expectTrustedLogin(domain);
		
		play(Operations.sequenceOf(DatabaseOperations.cleanDB(),
				DatabaseOperations.insertDomainConfiguration(domainId, ConfigurationState.ENABLE)));
		
		server.start();
		
		UUID expectedRunId = TestImapArchiveModules.uuid;
		given()
			.config(RestAssuredConfig.config().redirect(RedirectConfig.redirectConfig().followRedirects(false)))
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.queryParam("all", true).
		expect()
			.header("Location", containsString("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments/" + expectedRunId.toString()))
			.statusCode(Status.SEE_OTHER.getStatusCode()).
		when()
			.delete("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");
		
		given()
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.contentType(ContentType.JSON).
		expect()
			.body(containsString("Starting IMAP Archive reset for domain mydomain")).
		when()
			.get("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments/" + expectedRunId.toString() + "/logs");
	}

	@Test
	public void startArchivingShouldNotSelectMailboxWhenFlagNoSelectIsSet() throws Exception {
		expectations
			.expectTrustedLoginAnyTime(domain);
		
		play(Operations.sequenceOf(DatabaseOperations.cleanDB(),
				DatabaseOperations.insertDomainConfiguration(domainId, ConfigurationState.ENABLE)));
		
		GreenMailUser usera = imapServer.setAdminUser("usera", "usera");
		imapServer.getManagers().getImapHostManager().deleteMailbox(usera, "INBOX");
		MailFolder mailbox = imapServer.getManagers().getImapHostManager().createMailbox(usera, "user/usera@mydomain.org");
		createNoSelectFolder(usera);
		MimeMessage mimeMessage = new MimeMessage((Session) null);
		mimeMessage.setSubject("subject");
		mimeMessage.setFrom(new InternetAddress("user/usera@mydomain.org"));
		mimeMessage.setText("msg");
		mailbox.appendMessage(mimeMessage, new Flags(), DateTime.parse("2014-05-19T00:00:00.000Z").toDate());

		server.start();
		
		UUID expectedRunId = TestImapArchiveModules.uuid;
		given()
			.config(RestAssuredConfig.config().redirect(RedirectConfig.redirectConfig().followRedirects(false)))
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue()).
		expect()
			.header("Location", containsString("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments/" + expectedRunId.toString()))
			.statusCode(Status.SEE_OTHER.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments");

		await().atMost(1, TimeUnit.MINUTES)
			.pollDelay(Duration.ONE_SECOND)
			.pollInterval(Duration.ONE_SECOND)
			.until(endOfTheTreatment(expectedRunId));

		given()
			.config(RestAssuredConfig.config().redirect(RedirectConfig.redirectConfig().followRedirects(false)))
			.port(server.getHttpPort())
			.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
			.queryParam("filter_terminated", true).
		when()
			.get("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments").
		then()
			.statusCode(Status.OK.getStatusCode())
			.body(containsString("\"archiveStatus\":\"SUCCESS\""));
	}

	private void createNoSelectFolder(GreenMailUser usera) throws AuthorizationException, FolderException {
		imapServer.getManagers().getImapHostManager().createMailbox(usera, "user/usera.folder@mydomain.org");
	}

	private Callable<Boolean> endOfTheTreatment(final UUID expectedRunId) {
		return new Callable<Boolean>() {

			@Override
			public Boolean call() throws Exception {
				try {
					given()
						.config(RestAssuredConfig.config().redirect(RedirectConfig.redirectConfig().followRedirects(false)))
						.port(server.getHttpPort())
						.auth().basic(admin.getLogin() + "@" + domain.getName(), admin.getPassword().getStringValue())
						.queryParam("filter_terminated", true).
					when()
						.get("/imap-archive/service/v1/domains/" + domainId.get() + "/treatments").
					then()
						.statusCode(Status.OK.getStatusCode())
						.body(containsString(expectedRunId.toString()));
					return true;
				} catch (AssertionError e) {
					return false;
				}
			}
		};
	}
}
