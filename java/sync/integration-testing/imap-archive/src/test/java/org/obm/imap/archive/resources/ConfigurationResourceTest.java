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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

import java.util.UUID;

import javax.ws.rs.core.Response.Status;

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
import org.obm.imap.archive.DatabaseOperations;
import org.obm.imap.archive.Expectations;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.dao.DomainConfigurationJdbcImpl;
import org.obm.imap.archive.dto.DomainConfigurationDto;
import org.obm.server.WebServer;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.jayway.restassured.http.ContentType;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ConfigurationResourceTest {

	private ClientDriverRule driver = new ClientDriverRule();
	
	@Rule public TestRule chain = RuleChain
			.outerRule(driver)
			.around(new TemporaryFolder())
			.around(new GuiceRule(this, new TestImapArchiveModules.Simple(driver, new Provider<TemporaryFolder>() {

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
			}, "sql/initial.sql"));

	@Inject TemporaryFolder temporaryFolder;
	@Inject H2InMemoryDatabase db;
	@Inject WebServer server;
	Expectations expectations;

	private ObmDomainUuid domainId;

	
	@Before
	public void setUp() {
		domainId = ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		expectations = new Expectations(driver)
			.expectTrustedLogin(domainId)
			.expectGetDomain(domainId);
	}

	private void initDb(Operation... operationToAppend) {
		Operation operation =
				Operations.sequenceOf(DatabaseOperations.cleanDB(),
				Operations.sequenceOf(operationToAppend));
		
		DbSetup dbSetup = new DbSetup(H2Destination.from(db), operation);
		dbSetup.launch();
	}
	
	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	@Test
	public void getDomainConfigurationShouldReturnADefaultConfiguration() throws Exception {
		initDb();
		server.start();
		
		given()
			.port(server.getHttpPort())
			.auth().basic("admin@mydomain.org", "trust3dToken").
		expect()
			.contentType(ContentType.JSON)
			.body("domainId", equalTo("a6af9131-60b6-4e3a-a9f3-df5b43a89309"),
				"enabled", equalTo(false))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration");
	}

	@Test
	public void getDomainConfigurationShouldReturnStoredConfiguration() throws Exception {
		String domainId = "a6af9131-60b6-4e3a-a9f3-df5b43a89309";
		initDb(Operations.insertInto(DomainConfigurationJdbcImpl.TABLE.NAME)
					.columns(DomainConfigurationJdbcImpl.TABLE.FIELDS.DOMAIN_UUID, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.ACTIVATED, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.REPEAT_KIND, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_WEEK, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_MONTH, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_YEAR, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.HOUR, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.MINUTE,
						DomainConfigurationJdbcImpl.TABLE.FIELDS.EXCLUDED_FOLDER)
					.values(domainId, Boolean.TRUE, RepeatKind.DAILY, 2, 10, 355, 10, 32, "excluded")
					.build(),
				Operations.insertInto(DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.NAME)
					.columns(DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.FIELDS.DOMAIN_UUID, DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.FIELDS.USER_UUID)
					.values(domainId, "08607f19-05a4-42a2-9b02-6f11f3ceff3b")
					.build(),
				Operations.insertInto(DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.NAME)
					.columns(DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.FIELDS.DOMAIN_UUID, DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.FIELDS.USER_UUID)
					.values(domainId, "8e30e673-1c47-4ca8-85e8-4609d4228c10")
					.build(),
				Operations.insertInto(DomainConfigurationJdbcImpl.MAILING.TABLE.NAME)
					.columns(DomainConfigurationJdbcImpl.MAILING.TABLE.FIELDS.DOMAIN_UUID, DomainConfigurationJdbcImpl.MAILING.TABLE.FIELDS.EMAIL)
					.values(domainId, "user@mydomain.org")
					.build(),
				Operations.insertInto(DomainConfigurationJdbcImpl.MAILING.TABLE.NAME)
					.columns(DomainConfigurationJdbcImpl.MAILING.TABLE.FIELDS.DOMAIN_UUID, DomainConfigurationJdbcImpl.MAILING.TABLE.FIELDS.EMAIL)
					.values(domainId, "user2@mydomain.org")
					.build());
		server.start();
		
		given()
			.port(server.getHttpPort())
			.auth().basic("admin@mydomain.org", "trust3dToken").
		expect()
			.contentType(ContentType.JSON)
			.body("domainId", equalTo(domainId),
				"enabled", equalTo(true),
				"excludedUserIds", contains("08607f19-05a4-42a2-9b02-6f11f3ceff3b", "8e30e673-1c47-4ca8-85e8-4609d4228c10"),
				"mailingEmails", containsInAnyOrder("user@mydomain.org", "user2@mydomain.org"))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration");
	}

	@Test
	public void domainShouldBeEvaluatedEachTime() throws Exception {
		initDb(Operations.insertInto(DomainConfigurationJdbcImpl.TABLE.NAME)
				.columns(DomainConfigurationJdbcImpl.TABLE.FIELDS.DOMAIN_UUID, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.ACTIVATED, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.REPEAT_KIND, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_WEEK, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_MONTH, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_YEAR, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.HOUR, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.MINUTE,
						DomainConfigurationJdbcImpl.TABLE.FIELDS.EXCLUDED_FOLDER)
						.values("a6af9131-60b6-4e3a-a9f3-df5b43a89309", Boolean.TRUE, RepeatKind.DAILY, 2, 10, 355, 10, 32, "excluded")
						.build());
		
		ObmDomainUuid otherDomain = ObmDomainUuid.of("31ae9172-ca35-4045-8ea3-c3125dab771e");
		expectations
			.expectTrustedLogin(otherDomain)
			.expectGetDomain(otherDomain);
		
		server.start();
		
		given()
			.port(server.getHttpPort())
			.auth().basic("admin@mydomain.org", "trust3dToken").
		expect()
			.contentType(ContentType.JSON)
			.body("domainId", equalTo("a6af9131-60b6-4e3a-a9f3-df5b43a89309"),
				"enabled", equalTo(true))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration");
		
		given()
			.port(server.getHttpPort())
			.auth().basic("admin@mydomain.org", "trust3dToken").
		expect()
			.contentType(ContentType.JSON)
			.body("domainId", equalTo("31ae9172-ca35-4045-8ea3-c3125dab771e"),
				"enabled", equalTo(false))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/31ae9172-ca35-4045-8ea3-c3125dab771e/configuration");
	}

	@Test
	public void updateDomainConfigurationShouldThrowExceptionWhenBadInputs() throws Exception {
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = UUID.fromString("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		server.start();
		
		given()
			.port(server.getHttpPort())
			.auth().basic("admin@mydomain.org", "trust3dToken")
			.contentType(ContentType.JSON)
			.body(domainConfigurationDto).
		expect()
			.statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode()).
		when()
			.put("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration");
	}

	@Test
	public void updateDomainConfigurationShouldCreateWhenNoData() throws Exception {
		ObmDomainUuid newDomainUuid = ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		expectations
			.expectTrustedLogin(newDomainUuid)
			.expectGetDomain(newDomainUuid);
		server.start();
		
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = UUID.fromString("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		domainConfigurationDto.enabled = true;
		domainConfigurationDto.repeatKind = RepeatKind.WEEKLY.toString();
		domainConfigurationDto.dayOfWeek = DayOfWeek.TUESDAY.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = DayOfMonth.of(10).getDayIndex();
		domainConfigurationDto.dayOfYear = DayOfYear.of(100).getDayOfYear();
		domainConfigurationDto.hour = 11;
		domainConfigurationDto.minute = 32;
		domainConfigurationDto.excludedUserIds = ImmutableList.of("08607f19-05a4-42a2-9b02-6f11f3ceff3b", "8e30e673-1c47-4ca8-85e8-4609d4228c10");
		domainConfigurationDto.mailingEmails = ImmutableList.of("user@mydomain.org", "user2@mydomain.org");
		
		given()
			.auth().basic("admin@mydomain.org", "trust3dToken")
			.port(server.getHttpPort())
			.contentType(ContentType.JSON)
			.body(domainConfigurationDto).
		expect()
			.header("Location", endsWith("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration"))
			.statusCode(Status.CREATED.getStatusCode()).
		when()
			.put("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration");
		
		given()
			.port(server.getHttpPort())
			.auth().basic("admin@mydomain.org", "trust3dToken").
		expect()
			.contentType(ContentType.JSON)
			.body("domainId", equalTo("a6af9131-60b6-4e3a-a9f3-df5b43a89309"),
				"enabled", equalTo(true),
				"dayOfWeek", equalTo(DayOfWeek.TUESDAY.getSpecificationValue()),
				"excludedUserIds", contains("08607f19-05a4-42a2-9b02-6f11f3ceff3b", "8e30e673-1c47-4ca8-85e8-4609d4228c10"),
				"mailingEmails", containsInAnyOrder("user@mydomain.org", "user2@mydomain.org"))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration");
	}

	@Test
	public void updateDomainConfigurationShouldReturnNoContentWhenUpdating() throws Exception {
		initDb(Operations.insertInto(DomainConfigurationJdbcImpl.TABLE.NAME)
					.columns(DomainConfigurationJdbcImpl.TABLE.FIELDS.DOMAIN_UUID, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.ACTIVATED, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.REPEAT_KIND, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_WEEK, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_MONTH, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.DAY_OF_YEAR, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.HOUR, 
						DomainConfigurationJdbcImpl.TABLE.FIELDS.MINUTE,
						DomainConfigurationJdbcImpl.TABLE.FIELDS.EXCLUDED_FOLDER)
					.values("a6af9131-60b6-4e3a-a9f3-df5b43a89309", Boolean.TRUE, RepeatKind.DAILY, 2, 10, 355, 10, 32, "excluded")
					.build(),
				Operations.insertInto(DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.NAME)
					.columns(DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.FIELDS.DOMAIN_UUID, DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.FIELDS.USER_UUID)
					.values(domainId, "08607f19-05a4-42a2-9b02-6f11f3ceff3b")
					.build(),
				Operations.insertInto(DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.NAME)
					.columns(DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.FIELDS.DOMAIN_UUID, DomainConfigurationJdbcImpl.EXCLUDED_USERS.TABLE.FIELDS.USER_UUID)
					.values(domainId, "8e30e673-1c47-4ca8-85e8-4609d4228c10")
					.build(),
				Operations.insertInto(DomainConfigurationJdbcImpl.MAILING.TABLE.NAME)
					.columns(DomainConfigurationJdbcImpl.MAILING.TABLE.FIELDS.DOMAIN_UUID, DomainConfigurationJdbcImpl.MAILING.TABLE.FIELDS.EMAIL)
					.values(domainId, "user@mydomain.org")
					.build(),
				Operations.insertInto(DomainConfigurationJdbcImpl.MAILING.TABLE.NAME)
					.columns(DomainConfigurationJdbcImpl.MAILING.TABLE.FIELDS.DOMAIN_UUID, DomainConfigurationJdbcImpl.MAILING.TABLE.FIELDS.EMAIL)
					.values(domainId, "user2@mydomain.org")
					.build());
		ObmDomainUuid newDomainUuid = ObmDomainUuid.of("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		expectations
			.expectTrustedLogin(newDomainUuid)
			.expectGetDomain(newDomainUuid);
		
		server.start();
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = UUID.fromString("a6af9131-60b6-4e3a-a9f3-df5b43a89309");
		domainConfigurationDto.enabled = true;
		domainConfigurationDto.repeatKind = RepeatKind.WEEKLY.toString();
		domainConfigurationDto.dayOfWeek = DayOfWeek.WEDNESDAY.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = DayOfMonth.of(10).getDayIndex();
		domainConfigurationDto.dayOfYear = DayOfYear.of(100).getDayOfYear();
		domainConfigurationDto.hour = 11;
		domainConfigurationDto.minute = 32;
		domainConfigurationDto.excludedFolder = "anotherExcluded";
		domainConfigurationDto.excludedUserIds = ImmutableList.of("08607f19-05a4-42a2-9b02-6f11f3ceff3b", "2d7a5942-46ab-4fad-9bd2-608bde249671");
		domainConfigurationDto.mailingEmails = ImmutableList.of("user@mydomain.org", "user3@mydomain.org");
		
		given()
			.port(server.getHttpPort())
			.auth().basic("admin@mydomain.org", "trust3dToken")
			.contentType(ContentType.JSON)
			.body(domainConfigurationDto).
		expect()
			.statusCode(Status.NO_CONTENT.getStatusCode()).
		when()
			.put("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration");
		
		given()
			.port(server.getHttpPort())
			.auth().basic("admin@mydomain.org", "trust3dToken").
		expect()
			.contentType(ContentType.JSON)
			.body("domainId", equalTo("a6af9131-60b6-4e3a-a9f3-df5b43a89309"),
				"enabled", equalTo(true),
				"dayOfWeek", equalTo(DayOfWeek.WEDNESDAY.getSpecificationValue()),
				"excludedFolder", equalTo("anotherExcluded"),
				"excludedUserIds", contains("08607f19-05a4-42a2-9b02-6f11f3ceff3b", "2d7a5942-46ab-4fad-9bd2-608bde249671"),
				"mailingEmails", containsInAnyOrder("user@mydomain.org", "user3@mydomain.org"))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/domains/a6af9131-60b6-4e3a-a9f3-df5b43a89309/configuration");
	}
}
