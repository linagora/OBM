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
package org.obm.imap.archive.startup;

import static com.jayway.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.ws.rs.core.Response.Status;

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
import org.obm.imap.archive.Expectations;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.imap.archive.TestImapArchiveModules.WithTestingMonitor.TestingOnlyOnePerDomainMonitorFactory;
import org.obm.imap.archive.beans.ArchiveRunningTreatment;
import org.obm.imap.archive.beans.ArchiveScheduledTreatment;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTerminatedTreatment;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.scheduling.AbstractArchiveDomainTask;
import org.obm.imap.archive.scheduling.ArchiveDomainTaskFactory;
import org.obm.server.WebServer;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.linagora.scheduling.ScheduledTask;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class RestoreOnStartUpTest {

	private ClientDriverRule driver = new ClientDriverRule();

	@Rule public TestRule chain = RuleChain
			.outerRule(driver)
			.around(new TemporaryFolder())
			.around(new GuiceRule(this, new TestImapArchiveModules.WithTestingMonitor(driver, new Provider<TemporaryFolder>() {

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
	@Inject ArchiveDomainTaskFactory taskFactory;
	@Inject TestingOnlyOnePerDomainMonitorFactory monitor;
	@Inject ArchiveTreatmentDao archiveTreatmentDao;
	
	@Before
	public void setUp() {
		new Expectations(driver)
			.expectTrustedLogin(ObmDomainUuid.of("ada1cd0a-f6e7-41f4-ac18-b0ce68573776"));

		Operation operation = Operations.deleteAllFrom("mail_archive_run");
		new DbSetup(H2Destination.from(db), operation).launch();
	}
	
	@After
	public void tearDown() throws Exception {
		server.stop();
	}

	@Test
	public void testTasksAreWellRestoredOrMovedAsFailed() throws Exception {
		ArchiveTreatmentRunId expectedScheduledRunId = ArchiveTreatmentRunId.from("45896372-cc9f-4ee9-9efd-8df63e2da8c3");
		ObmDomainUuid expectedScheduledDomain = ObmDomainUuid.of("65ae0168-cb77-43b8-bda3-0aa81f79ab5c");
		DateTime expectedScheduledHigherBoundary = DateTime.parse("2026-11-02T01:04Z");
		DateTime expectedScheduledTime = DateTime.parse("2026-11-02T03:04Z");
		
		ArchiveTreatmentRunId expectedFailedRunId = ArchiveTreatmentRunId.from("c6eb4f70-2304-4bb4-aa38-441935dc6a47");
		ObmDomainUuid expectedFailedDomain = ObmDomainUuid.of("b9de411c-5375-4100-aedf-8e4d827c0a2c");
		DateTime expectedFailedHigherBoundary = DateTime.parse("2026-10-02T01:04Z");
		DateTime expectedFailedScheduleTime = DateTime.parse("2026-11-02T03:04Z");
		DateTime expectedFailedStartTime = DateTime.parse("2026-10-01T01:01Z");
		
		archiveTreatmentDao.insert(ArchiveScheduledTreatment
				.forDomain(expectedScheduledDomain)
				.runId(expectedScheduledRunId)
				.recurrent(true)
				.higherBoundary(expectedScheduledHigherBoundary)
				.scheduledAt(expectedScheduledTime)
				.build());
		
		archiveTreatmentDao.insert(ArchiveRunningTreatment
				.forDomain(expectedFailedDomain)
				.runId(expectedFailedRunId)
				.recurrent(true)
				.higherBoundary(expectedFailedHigherBoundary)
				.scheduledAt(expectedFailedScheduleTime)
				.startedAt(expectedFailedStartTime)
				.build());

		server.start();
		
		given()
			.port(server.getHttpPort())
			.auth().basic("admin@mydomain.org", "trust3dToken").
		expect()
			.statusCode(Status.OK.getStatusCode()).
		when()
			.get("/imap-archive/service/v1/status");
		
		List<ScheduledTask<AbstractArchiveDomainTask>> tasks = monitor.get().all();
		assertThat(tasks).hasSize(1);
		assertThat(tasks.get(0).task()).isEqualTo(taskFactory.createAsRecurrent(
			expectedScheduledDomain,
			expectedScheduledTime,
			expectedScheduledHigherBoundary,
			expectedScheduledRunId));
		
		List<ArchiveTreatment> failedTreatments = archiveTreatmentDao.findByScheduledTime(expectedFailedDomain, 5);
		assertThat(failedTreatments).containsExactly(ArchiveTerminatedTreatment
			.forDomain(expectedFailedDomain)
			.runId(expectedFailedRunId)
			.recurrent(true)
			.scheduledAt(expectedFailedScheduleTime)
			.startedAt(expectedFailedStartTime)
			.higherBoundary(expectedFailedHigherBoundary)
			.terminatedAt(ArchiveTreatment.FAILED_AT_UNKOWN_DATE)
			.status(ArchiveStatus.ERROR)
			.build());
	}
}
