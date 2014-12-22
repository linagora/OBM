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

package org.obm.imap.archive.reschedule;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.TimeUnit;

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
import org.obm.guice.GuiceRule;
import org.obm.imap.archive.CyrusCompatGreenmailRule;
import org.obm.imap.archive.DBData;
import org.obm.imap.archive.FutureSchedulerBusClient;
import org.obm.imap.archive.TestImapArchiveModules;
import org.obm.imap.archive.TestImapArchiveModules.TimeBasedModule.TestDateProvider;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.ConfigurationState;
import org.obm.imap.archive.beans.DayOfMonth;
import org.obm.imap.archive.beans.DayOfWeek;
import org.obm.imap.archive.beans.DayOfYear;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.scheduling.ArchiveDomainTaskFactory;
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

import fr.aliacom.obm.common.domain.ObmDomain;

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
	@Inject ArchiveDomainTaskFactory taskFactory;
	@Inject DomainConfigurationDao domainConfigDao;
	@Inject ArchiveScheduler scheduler;
	@Inject TestDateProvider timeProvider;
	@Inject FutureSchedulerBusClient futureBusClient;

	int timeout = 2000;
	
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
		
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
			.domain(ObmDomain.builder().uuid(DBData.domainId).name("mydomain.org").build())
			.state(ConfigurationState.ENABLE)
			.schedulingConfiguration(SchedulingConfiguration.builder()
				.recurrence(ArchiveRecurrence.builder()
					.repeat(RepeatKind.DAILY)
					.dayOfWeek(DayOfWeek.FRIDAY)
					.dayOfMonth(DayOfMonth.last())
					.dayOfYear(DayOfYear.of(5))
					.build())
				.time(LocalTime.parse("13:37"))
				.build())
			.archiveMainFolder("arChive")
			.build();
		domainConfigDao.create(domainConfiguration);
		
		server.start();

		// SCHEDULE AND RUN A RECURRENT TASK
		scheduler.schedule(taskFactory.createAsRecurrent(domainConfiguration, when, higherBoundary, runId));
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
	}

	private State nextTaskState() throws Exception {
		return futureBusClient.next(timeout, TimeUnit.SECONDS).state();
	}
}
