/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.imap.archive.resources;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.UUID;

import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.obm.dao.utils.H2Destination;
import org.obm.dao.utils.H2InMemoryDatabase;
import org.obm.dao.utils.H2InMemoryDatabaseTestRule;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.CommonClientDriverExpectation;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.imap.archive.beans.ArchiveRecurrence.RepeatKind;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.dao.DomainConfigurationJdbcImpl;
import org.obm.imap.archive.dto.DomainConfigurationDto;
import org.obm.server.WebServer;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jayway.restassured.http.ContentType;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class TreatmentsResourceTest {

	private CommonClientDriverExpectation driver = new CommonClientDriverExpectation();
	
	@Rule public TestRule chain = RuleChain
			.outerRule(driver.getClientDriverRule())
			.around(new GuiceRule(this, new TestImapArchiveModules.Simple(driver.getClientDriverRule())))
			.around(new H2InMemoryDatabaseTestRule(new Provider<H2InMemoryDatabase>() {
				@Override
				public H2InMemoryDatabase get() {
					return db;
				}
			}, "sql/initial.sql"));
	
	@Inject H2InMemoryDatabase db;
	@Inject WebServer server;

	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	@Test
	public void calculateNextScheduledDateShouldReturnNoContentWhenConfigurationInactive() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("962b7b35-abf3-4f1b-943d-d6640450812b");
		driver.expectTrustedLogin(domainId);
		driver.expectGetDomain(domainId);
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
		
		given()
			.port(server.getHttpPort())
			.queryParam("login", "admin")
			.queryParam("password", "trust3dToken")
			.queryParam("domain_name", "mydomain.org")
			.contentType(ContentType.JSON)
			.body(domainConfigurationDto).
		expect()
			.statusCode(Status.NO_CONTENT.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/962b7b35-abf3-4f1b-943d-d6640450812b/treatments/next");
	}
	
	@Test
	public void calculateNextScheduledDateShouldReturnNextTreatmentDateWhenConfigurationActive() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("21aeb670-f49e-428a-9d0c-f11f5feaa688");
		driver.expectTrustedLogin(domainId);
		driver.expectGetDomain(domainId);
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
		
		given()
			.port(server.getHttpPort())
			.queryParam("login", "admin")
			.queryParam("password", "trust3dToken")
			.queryParam("domain_name", "mydomain.org")
			.contentType(ContentType.JSON)
			.body(domainConfigurationDto).
		expect()
			.contentType(ContentType.JSON)
			.body("nextTreatmentDate", equalTo("2014-06-19T00:00:00.000Z"))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/21aeb670-f49e-428a-9d0c-f11f5feaa688/treatments/next");
	}
	
	@Test
	public void startArchivingShouldCreate() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("2f096466-5a2a-463e-afad-4196c2952de3");
		driver.expectTrustedLogin(domainId);
		driver.expectGetDomain(domainId);
		
		insertDomainConfiguration();
		
		server.start();
		
		UUID expectedRunId = TestImapArchiveModules.uuid;
		given()
			.port(server.getHttpPort())
			.queryParam("login", "admin")
			.queryParam("password", "trust3dToken")
			.queryParam("domain_name", "mydomain.org").
		expect()
			.contentType(ContentType.JSON)
			.body("runId", equalTo(expectedRunId.toString()))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/2f096466-5a2a-463e-afad-4196c2952de3/treatments");
	}

	private void insertDomainConfiguration() {
		Operation operation =
				Operations.sequenceOf(Operations.deleteAllFrom(DomainConfigurationJdbcImpl.TABLE),
				Operations.sequenceOf(Operations.insertInto(DomainConfigurationJdbcImpl.TABLE)
						.columns(DomainConfigurationJdbcImpl.MAIL_ARCHIVE_DOMAIN_UUID, 
								DomainConfigurationJdbcImpl.MAIL_ARCHIVE_ACTIVATED, 
								DomainConfigurationJdbcImpl.MAIL_ARCHIVE_REPEAT_KIND, 
								DomainConfigurationJdbcImpl.MAIL_ARCHIVE_DAY_OF_WEEK, 
								DomainConfigurationJdbcImpl.MAIL_ARCHIVE_DAY_OF_MONTH, 
								DomainConfigurationJdbcImpl.MAIL_ARCHIVE_DAY_OF_YEAR, 
								DomainConfigurationJdbcImpl.MAIL_ARCHIVE_HOUR, 
								DomainConfigurationJdbcImpl.MAIL_ARCHIVE_MINUTE)
								.values("2f096466-5a2a-463e-afad-4196c2952de3", Boolean.TRUE, RepeatKind.DAILY, 2, 10, 355, 10, 32)
								.build()));
		DbSetup dbSetup = new DbSetup(H2Destination.from(db), operation);
		dbSetup.launch();
	}
	
	@Test
	public void startArchivingTwiceShouldRespondConflict() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("2f096466-5a2a-463e-afad-4196c2952de3");
		driver.expectTrustedLogin(domainId);
		driver.expectTrustedLogin(domainId);
		driver.expectGetDomain(domainId);
		
		insertDomainConfiguration();
		
		server.start();
		
		UUID expectedRunId = TestImapArchiveModules.uuid;
		given()
			.port(server.getHttpPort())
			.queryParam("login", "admin")
			.queryParam("password", "trust3dToken")
			.queryParam("domain_name", "mydomain.org").
		expect()
			.contentType(ContentType.JSON)
			.body("runId", equalTo(expectedRunId.toString()))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/2f096466-5a2a-463e-afad-4196c2952de3/treatments");
		given()
			.port(server.getHttpPort())
			.queryParam("login", "admin")
			.queryParam("password", "trust3dToken")
			.queryParam("domain_name", "mydomain.org").
		expect()
			.statusCode(Status.CONFLICT.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/2f096466-5a2a-463e-afad-4196c2952de3/treatments");
	}
	
	@Test
	public void runningTreatmentShouldReturnChunk() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("2f096466-5a2a-463e-afad-4196c2952de3");
		// POST
		driver.expectTrustedLogin(domainId);
		driver.expectGetDomain(domainId);
		// GET 
		driver.expectTrustedLogin(domainId);
		
		insertDomainConfiguration();
		
		server.start();
		
		UUID runId = TestImapArchiveModules.uuid;
		given()
			.port(server.getHttpPort())
			.queryParam("login", "admin")
			.queryParam("password", "trust3dToken")
			.queryParam("domain_name", "mydomain.org").
		expect()
			.contentType(ContentType.JSON)
			.body("runId", equalTo(runId.toString()))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/2f096466-5a2a-463e-afad-4196c2952de3/treatments");
		
		String date = TestImapArchiveModules.LOCAL_DATE_TIME.toString();
		String expectedString = date + System.lineSeparator() + date;
		
		given()
			.port(server.getHttpPort())
			.queryParam("login", "admin")
			.queryParam("password", "trust3dToken")
			.queryParam("domain_name", "mydomain.org")
			.queryParam("run_id", runId.toString())
			.contentType(ContentType.JSON).
		expect()
			.header("Transfer-encoding", "chunked")
			.body(containsString(expectedString)).
		when()
			.get("/imap-archive/service/v1/domains/2f096466-5a2a-463e-afad-4196c2952de3/treatments/logs");
	}
	
	@Test
	public void runningTreatmentShouldReturnNoContentWhenTreatmentIsOver() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("2f096466-5a2a-463e-afad-4196c2952de3");
		// POST
		driver.expectTrustedLogin(domainId);
		driver.expectGetDomain(domainId);
		// GET 
		driver.expectTrustedLogin(domainId);
		
		insertDomainConfiguration();
		
		server.start();
		
		UUID runId = TestImapArchiveModules.uuid;
		given()
			.port(server.getHttpPort())
			.queryParam("login", "admin")
			.queryParam("password", "trust3dToken")
			.queryParam("domain_name", "mydomain.org").
		expect()
			.contentType(ContentType.JSON)
			.body("runId", equalTo(runId.toString()))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/2f096466-5a2a-463e-afad-4196c2952de3/treatments");
		
		long twoSeconds = 2000;
		Thread.sleep(twoSeconds);
		
		given()
			.port(server.getHttpPort())
			.queryParam("login", "admin")
			.queryParam("password", "trust3dToken")
			.queryParam("domain_name", "mydomain.org")
			.queryParam("run_id", runId.toString())
			.contentType(ContentType.JSON).
		expect()
			.statusCode(Status.NO_CONTENT.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/2f096466-5a2a-463e-afad-4196c2952de3/treatments/logs");
	}
	
	@Test
	public void runningTreatmentShouldReturnNoContentWhenBadRunId() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("2f096466-5a2a-463e-afad-4196c2952de3");
		driver.expectTrustedLogin(domainId);
		driver.expectGetDomain(domainId);
		
		insertDomainConfiguration();
		
		server.start();
		
		given()
			.port(server.getHttpPort())
			.queryParam("login", "admin")
			.queryParam("password", "trust3dToken")
			.queryParam("domain_name", "mydomain.org")
			.queryParam("run_id", TestImapArchiveModules.uuid.toString())
			.contentType(ContentType.JSON).
		expect()
			.statusCode(Status.NO_CONTENT.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/2f096466-5a2a-463e-afad-4196c2952de3/treatments/logs");
	}
}
