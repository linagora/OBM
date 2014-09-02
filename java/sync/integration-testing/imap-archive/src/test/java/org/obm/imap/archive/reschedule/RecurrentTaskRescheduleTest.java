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
package org.obm.imap.archive.reschedule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.expect;

import java.util.concurrent.TimeUnit;

import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
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
import org.obm.domain.dao.UserSystemDao;
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.Expectations;
import org.obm.imap.archive.FutureSchedulerBusClient;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.imap.archive.TestImapArchiveModules.TimeBasedModule.TestDateProvider;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.scheduling.ArchiveDomainTask;
import org.obm.imap.archive.scheduling.ArchiveScheduler;
import org.obm.server.WebServer;

import com.github.restdriver.clientdriver.ClientDriverRule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.icegreen.greenmail.util.GreenMail;
import com.linagora.scheduling.ScheduledTask.State;
import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.operation.Operation;

import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.system.ObmSystemUser;
import fr.aliacom.obm.common.user.UserPassword;

public class RecurrentTaskRescheduleTest {

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
			}, "sql/initial.sql"));

	@Inject TemporaryFolder temporaryFolder;
	@Inject H2InMemoryDatabase db;
	@Inject WebServer server;
	@Inject GreenMail imapServer;
	@Inject ArchiveDomainTask.Factory taskFactory;
	@Inject DomainConfigurationDao domainConfigDao;
	@Inject ArchiveScheduler scheduler;
	@Inject TestDateProvider timeProvider;
	@Inject FutureSchedulerBusClient futureBusClient;

	@Inject IMocksControl control;
	@Inject UserSystemDao userSystemDao;

	int timeout = 2000;
	ObmDomainUuid domainUuid = ObmDomainUuid.of("b9de411c-5375-4100-aedf-8e4d827c0a2c");
	
	@Before
	public void setUp() {
		Operation operation = Operations.deleteAllFrom("mail_archive_run");
		new DbSetup(H2Destination.from(db), operation).launch();
	}
	
	@After
	public void tearDown() throws Exception {
		server.stop();
		imapServer.stop();
	}

	@Test
	public void recurrentTaskShouldBeRescheduledWhenTerminated() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("b3de5c2b-daaa-48ae-9b01-ba390dea47a9");
		DateTime when = DateTime.parse("2026-11-02T13:37Z");
		DateTime higherBoundary = DateTime.parse("2026-11-02T23:59Z");
		
		new Expectations(driver)
			.expectGetDomain(domainUuid)
			.expectGetDomain(domainUuid);
		imapServer.setUser("cyrus", "cyrus");
		expect(userSystemDao.getByLogin("cyrus"))
			.andReturn(ObmSystemUser.builder().login("cyrus").password(UserPassword.valueOf("cyrus")).id(12).build())
			.anyTimes();
		
		domainConfigDao.create(DomainConfiguration.builder()
			.domainId(domainUuid)
			.enabled(true)
			.schedulingConfiguration(SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.DAILY)
					.dayOfWeek(DayOfWeek.FRIDAY)
					.dayOfMonth(DayOfMonth.last())
					.dayOfYear(DayOfYear.of(5))
					.build())
				.time(LocalTime.parse("13:37"))
				.build())
			.build());
		
		control.replay();
		imapServer.start();
		server.start();

		// SCHEDULE AND RUN A RECURRENT TASK
		scheduler.schedule(taskFactory.createAsRecurrent(domainUuid, when, higherBoundary, runId));
		assertThat(nextTaskState()).isEqualTo(State.WAITING);
		timeProvider.setCurrent(when);
		assertThat(nextTaskState()).isEqualTo(State.RUNNING);
		assertThat(nextTaskState()).isEqualTo(State.TERMINATED);
		
		// AUTOMATIC RE-SCHEDULED TASK RUN
		assertThat(nextTaskState()).isEqualTo(State.WAITING);
		timeProvider.setCurrent(when.plusDays(1));
		assertThat(nextTaskState()).isEqualTo(State.RUNNING);
		assertThat(nextTaskState()).isEqualTo(State.TERMINATED);
		
		// AUTOMATIC RE-SCHEDULED TASK
		assertThat(nextTaskState()).isEqualTo(State.WAITING);
		control.verify();
	}

	private State nextTaskState() throws Exception {
		return futureBusClient.next(timeout, TimeUnit.SECONDS).getTask().state();
	}
}
