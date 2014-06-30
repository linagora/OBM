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
import static org.hamcrest.Matchers.equalTo;

import java.util.UUID;

import javax.ws.rs.core.Response.Status;

import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.imap.archive.beans.ArchiveRecurrence.RepeatKind;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.dto.DomainConfigurationDto;
import org.obm.server.WebServer;

import com.google.inject.Inject;
import com.jayway.restassured.http.ContentType;

public class TreatmentsResourceTest {

	@Rule public TestRule chain = RuleChain
			.outerRule(new GuiceRule(this, new TestImapArchiveModules.Simple()));

	@Inject WebServer server;
	
	@Before
	public void setUp() throws Exception {
		server.start();
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}
	
	@Test
	public void calculateNextScheduledDateShouldReturnNoContentWhenConfigurationInactive() {
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = UUID.fromString("962b7b35-abf3-4f1b-943d-d6640450812b");
		domainConfigurationDto.enabled = false;
		domainConfigurationDto.repeatKind = RepeatKind.DAILY.toString();
		domainConfigurationDto.dayOfWeek = DayOfWeek.MONDAY.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = DayOfMonth.of(1).getDayIndex();
		domainConfigurationDto.dayOfYear = DayOfYear.of(1).getDayOfYear();
		domainConfigurationDto.hour = 0;
		domainConfigurationDto.minute = 0;
		
		given()
			.port(server.getHttpPort())
			.queryParam("login", "cyrus")
			.queryParam("password", "cyrus")
			.queryParam("domain_name", "mydomain.org")
			.contentType(ContentType.JSON)
			.body(domainConfigurationDto).
		expect()
			.statusCode(Status.NO_CONTENT.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/962b7b35-abf3-4f1b-943d-d6640450812b/treatments/next");
	}
	
	@Ignore
	@Test
	public void calculateNextScheduledDateShouldReturnNextTreatmentDateWhenConfigurationActive() {
		DateTime now = TestImapArchiveModules.LOCAL_DATE_TIME
				.plusDays(1);
		DomainConfigurationDto domainConfigurationDto = new DomainConfigurationDto();
		domainConfigurationDto.domainId = UUID.fromString("21aeb670-f49e-428a-9d0c-f11f5feaa688");
		domainConfigurationDto.enabled = true;
		domainConfigurationDto.repeatKind = RepeatKind.DAILY.toString();
		domainConfigurationDto.dayOfWeek = DayOfWeek.MONDAY.getSpecificationValue();
		domainConfigurationDto.dayOfMonth = DayOfMonth.of(1).getDayIndex();
		domainConfigurationDto.dayOfYear = DayOfYear.of(1).getDayOfYear();
		domainConfigurationDto.hour = 0;
		domainConfigurationDto.minute = 0;
		
		given()
			.port(server.getHttpPort())
			.queryParam("login", "cyrus")
			.queryParam("password", "cyrus")
			.queryParam("domain_name", "mydomain.org")
			.contentType(ContentType.JSON)
			.body(domainConfigurationDto).
		expect()
			.contentType(ContentType.JSON)
			.body("nextTreatmentDate", equalTo(now.getMillis()))
			.statusCode(Status.OK.getStatusCode()).
		when()
			.post("/imap-archive/service/v1/domains/21aeb670-f49e-428a-9d0c-f11f5feaa688/treatments/next");
	}
}
