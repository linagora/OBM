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
package org.obm.imap.archive.scheduling;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expectLastCall;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obm.imap.archive.beans.ArchiveConfiguration;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.logging.LoggerAppenders;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Client;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Events;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Events.RealRunTaskStatusChanged;
import org.obm.imap.archive.services.ImapArchiveProcessing;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Queues;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.linagora.scheduling.Monitor;
import com.linagora.scheduling.ScheduledTask;
import com.linagora.scheduling.ScheduledTask.State;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveSchedulerTest {

	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

	int timeout;

	IMocksControl mocks;
	ImapArchiveProcessing imapArchiveProcessing;
	Logger logger;
	LoggerAppenders loggerAppenders;
	DateTime higherBoundary;
	
	TestDateTimeProvider timeProvider;
	Monitor<ArchiveDomainTask> monitor;
	FutureTestListener<ArchiveDomainTask> futureListener;
	ObmDomain domain;
	BusClient busClient;
	ArchiveSchedulerBus bus;
	ArchiveSchedulerQueue queue;
	ArchiveScheduler testee;

	@Before
	public void setUp() throws IOException {
		timeout = 1500;
		higherBoundary = DateTime.parse("2024-11-1T05:04Z");
		
		mocks = createControl();
		imapArchiveProcessing = mocks.createMock(ImapArchiveProcessing.class);
		logger = (Logger) LoggerFactory.getLogger(temporaryFolder.newFile().getAbsolutePath());
		loggerAppenders = mocks.createMock(LoggerAppenders.class);
		loggerAppenders.startAppenders();
		expectLastCall().anyTimes();
		loggerAppenders.stopAppenders();
		expectLastCall().anyTimes();
		
		DateTime testsStartTime = DateTime.parse("2024-01-1T05:04Z");
		timeProvider = new TestDateTimeProvider(testsStartTime);
		futureListener = new FutureTestListener<>();
		
		OnlyOnePerDomainMonitorFactory monitorFactory = new OnlyOnePerDomainMonitorFactory() {

				@Override
				public Monitor<ArchiveDomainTask> create() {
					monitor = Monitor.<ArchiveDomainTask>builder()
							.addListener(futureListener)
							.build();
					return monitor;
				}
			
		};

		domain = ObmDomain.builder().uuid(ObmDomainUuid.of("70cd05cd-72f9-449a-b83b-740d136cd8d4")).name("mydomain.org").build();
		
		queue = new ArchiveSchedulerQueue(monitorFactory);
		busClient = Guice.createInjector(new BusModule()).getInstance(BusClient.class); 
		bus = new ArchiveSchedulerBus();
		bus.register(busClient);
		testee = new ArchiveScheduler(queue, bus, new ArchiveSchedulerLoggerListener(), timeProvider, MILLISECONDS);
	}
	
	private RemotelyControlledTask createTask(DomainConfiguration domainConfiguration,
			DateTime when, DateTime higherBoundary, ArchiveTreatmentRunId runId) {
		return new RemotelyControlledTask(imapArchiveProcessing, logger, loggerAppenders, domainConfiguration, when, higherBoundary, runId);
	}
	
	@After
	public void tearDown() throws Exception {
		testee.close();
	}

	@Test
	public void scheduleShouldCallScheduler() throws Exception {
		DomainConfiguration configuration = DomainConfiguration.builder()
				.domain(domain)
				.enabled(true)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build();
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		DateTime when = DateTime.parse("2024-11-1T05:04Z");
		RemotelyControlledTask task = createTask(configuration, when, higherBoundary, runId);
		imapArchiveProcessing.archive(task.getArchiveConfiguration());
		expectLastCall();
		
		mocks.replay();
		
		testee.schedule(task);
		assertTaskProgression(task);
		assertThat(monitor.all()).isEmpty();
		mocks.verify();
	}

	@Test
	public void scheduleShouldCallSchedulerWhenPreviousDomainTaskIsDone() throws Exception {
		DomainConfiguration configuration = DomainConfiguration.builder()
				.domain(domain)
				.enabled(true)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build();
		DateTime when1 = DateTime.parse("2024-11-1T05:04Z");
		DateTime when2 = DateTime.parse("2024-11-5T05:04Z");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");
		RemotelyControlledTask task1 = createTask(configuration, when1, higherBoundary, runId1);
		RemotelyControlledTask task2 = createTask(configuration, when2, higherBoundary, runId2);

		imapArchiveProcessing.archive(task1.getArchiveConfiguration());
		expectLastCall();
		imapArchiveProcessing.archive(task2.getArchiveConfiguration());
		expectLastCall();

		mocks.replay();
		testee.schedule(task1);
		assertTaskProgression(task1);
		testee.schedule(task2);
		assertTaskProgression(task2);
		assertThat(monitor.all()).isEmpty();
		mocks.verify();
	}
	
	@Test
	public void scheduleShouldEnqueueWhenTaskForDomainAlreadyScheduled() throws Exception {
		DomainConfiguration configuration = DomainConfiguration.builder()
				.domain(domain)
				.enabled(true)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build();
		DateTime when = DateTime.parse("2024-11-1T05:04Z");
		DateTime whenToEnqueue = DateTime.parse("2024-11-2T05:04Z");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");
		RemotelyControlledTask task =  createTask(configuration, when, higherBoundary, runId1);
		RemotelyControlledTask taskEnqueued = createTask(configuration, whenToEnqueue, higherBoundary, runId2);

		imapArchiveProcessing.archive(task.getArchiveConfiguration());
		expectLastCall();
		imapArchiveProcessing.archive(taskEnqueued.getArchiveConfiguration());
		expectLastCall();

		mocks.replay();
		testee.schedule(task);
		testee.schedule(taskEnqueued);
		assertTaskIsScheduled(task);
		assertTaskIsScheduled(taskEnqueued);
		assertTaskIsRunning(task);
		assertTaskIsTerminated(task);
		assertTaskIsRunning(taskEnqueued);
		assertTaskIsTerminated(taskEnqueued);
		mocks.verify();
		
		assertThat(monitor.all()).isEmpty();
	}
	
	@Test
	public void scheduleShouldEnqueueRespectingOrderWhenTaskForDomainAlreadyRunning() throws Exception {
		DomainConfiguration configuration = DomainConfiguration.builder()
				.domain(domain)
				.enabled(true)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build();
		DateTime when = DateTime.parse("2024-11-1T00:00");
		DateTime earlierWhenEnqueuedAfter = DateTime.parse("2024-11-5T00:00");
		DateTime laterWhenEnqueuedBefore = DateTime.parse("2024-11-9T00:00");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");
		ArchiveTreatmentRunId runId3 = ArchiveTreatmentRunId.from("b13c4e34-c70a-446d-a764-17575c4ea52f");
		RemotelyControlledTask task = createTask(configuration, when, higherBoundary, runId1);
		RemotelyControlledTask laterTaskEnqueuedBefore = createTask(configuration, laterWhenEnqueuedBefore, higherBoundary, runId2);
		RemotelyControlledTask earlierTaskEnqueuedAfter = createTask(configuration, earlierWhenEnqueuedAfter, higherBoundary, runId3);

		imapArchiveProcessing.archive(task.getArchiveConfiguration());
		expectLastCall();
		imapArchiveProcessing.archive(laterTaskEnqueuedBefore.getArchiveConfiguration());
		expectLastCall();
		imapArchiveProcessing.archive(earlierTaskEnqueuedAfter.getArchiveConfiguration());
		expectLastCall();

		mocks.replay();
		testee.schedule(task);
		assertTaskIsScheduled(task);
		assertTaskIsRunning(task);
		
		testee.schedule(laterTaskEnqueuedBefore);
		testee.schedule(earlierTaskEnqueuedAfter);
		assertTaskIsScheduled(laterTaskEnqueuedBefore);
		assertTaskIsScheduled(earlierTaskEnqueuedAfter);
		assertTaskIsTerminated(task);
		assertTaskIsRunning(earlierTaskEnqueuedAfter);
		assertTaskIsTerminated(earlierTaskEnqueuedAfter);
		assertTaskIsRunning(laterTaskEnqueuedBefore);
		assertTaskIsTerminated(laterTaskEnqueuedBefore);
		assertThat(monitor.all()).isEmpty();
		mocks.verify();
	}
	
	@Test
	public void scheduleDifferentDomainsShouldCallSchedulerForBoth() throws Exception {
		ObmDomain domain1 = ObmDomain.builder().uuid(ObmDomainUuid.of("70cd05cd-72f9-449a-b83b-740d136cd8d4")).name("mydomain.org").build();
		ObmDomain domain2 = ObmDomain.builder().uuid(ObmDomainUuid.of("b9b7ea0f-a65e-4d2e-89b1-fb9ef4d2c97d")).name("mydomain2.org").build();
		DomainConfiguration configuration1 = DomainConfiguration.builder()
				.domain(domain1)
				.enabled(true)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build();
		DomainConfiguration configuration2 = DomainConfiguration.builder()
				.domain(domain2)
				.enabled(true)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build();
		DateTime when1 = DateTime.parse("2024-11-1T05:04Z");
		DateTime when2 = DateTime.parse("2024-11-2T05:04Z");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");

		RemotelyControlledTask taskDomain1 = createTask(configuration1, when1, higherBoundary, runId1);
		RemotelyControlledTask taskDomain2 = createTask(configuration2, when2, higherBoundary, runId2);

		imapArchiveProcessing.archive(taskDomain1.getArchiveConfiguration());
		expectLastCall();
		imapArchiveProcessing.archive(taskDomain2.getArchiveConfiguration());
		expectLastCall();

		mocks.replay();
		testee.schedule(taskDomain1);
		testee.schedule(taskDomain2);
		assertTaskIsScheduled(taskDomain1);
		assertTaskIsScheduled(taskDomain2);
		assertTaskIsRunning(taskDomain1);
		assertTaskIsRunning(taskDomain2);
		assertTaskIsTerminated(taskDomain1);
		assertTaskIsTerminated(taskDomain2);
		
		assertThat(monitor.all()).isEmpty();
		mocks.verify();
	}
	
	@Test
	public void scheduleDifferentDomainsShouldCallSchedulerForBothThenEnqueueOthers() throws Exception {
		ObmDomain domain1 = ObmDomain.builder().uuid(ObmDomainUuid.of("70cd05cd-72f9-449a-b83b-740d136cd8d4")).name("mydomain.org").build();
		ObmDomain domain2 = ObmDomain.builder().uuid(ObmDomainUuid.of("b9b7ea0f-a65e-4d2e-89b1-fb9ef4d2c97d")).name("mydomain2.org").build();
		DomainConfiguration configuration1 = DomainConfiguration.builder()
				.domain(domain1)
				.enabled(true)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build();
		DomainConfiguration configuration2 = DomainConfiguration.builder()
				.domain(domain2)
				.enabled(true)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build();
		DateTime when1 = DateTime.parse("2024-11-1T05:04Z");
		DateTime when2 = DateTime.parse("2024-11-2T05:04Z");
		DateTime when1ToEnqueue = DateTime.parse("2024-11-3T05:04Z");
		DateTime when2ToEnqueue = DateTime.parse("2024-11-4T05:04Z");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");
		ArchiveTreatmentRunId runId3 = ArchiveTreatmentRunId.from("b13c4e34-c70a-446d-a764-17575c4ea52f");
		ArchiveTreatmentRunId runId4 = ArchiveTreatmentRunId.from("b1226053-265d-4b0e-a524-e37b1dfcb2e9");
		RemotelyControlledTask taskDomain1 = createTask(configuration1, when1, higherBoundary, runId1);
		RemotelyControlledTask taskDomain2 = createTask(configuration2, when2, higherBoundary, runId2);
		RemotelyControlledTask taskDomain1Enqueued = createTask(configuration1, when1ToEnqueue, higherBoundary, runId3);
		RemotelyControlledTask taskDomain2Enqueued = createTask(configuration2, when2ToEnqueue, higherBoundary, runId4);

		imapArchiveProcessing.archive(taskDomain1.getArchiveConfiguration());
		expectLastCall();
		imapArchiveProcessing.archive(taskDomain2.getArchiveConfiguration());
		expectLastCall();
		imapArchiveProcessing.archive(taskDomain1Enqueued.getArchiveConfiguration());
		expectLastCall();
		imapArchiveProcessing.archive(taskDomain2Enqueued.getArchiveConfiguration());
		expectLastCall();
		
		mocks.replay();
		testee.schedule(taskDomain1);
		testee.schedule(taskDomain2);
		testee.schedule(taskDomain1Enqueued);
		testee.schedule(taskDomain2Enqueued);

		assertTaskIsScheduled(taskDomain1);
		assertTaskIsScheduled(taskDomain2);
		assertTaskIsScheduled(taskDomain1Enqueued);
		assertTaskIsScheduled(taskDomain2Enqueued);
		assertTaskIsRunning(taskDomain1);
		assertTaskIsRunning(taskDomain2);
		assertTaskIsTerminated(taskDomain1);
		assertTaskIsRunning(taskDomain1Enqueued);
		assertTaskIsTerminated(taskDomain2);
		assertTaskIsRunning(taskDomain2Enqueued);
		assertTaskIsTerminated(taskDomain1Enqueued);
		assertTaskIsTerminated(taskDomain2Enqueued);
		
		assertThat(monitor.all()).isEmpty();
		mocks.verify();
	}
	
	@Test
	public void clearDomainShouldNotFailWhenNoPut() {
		ObmDomainUuid domain = ObmDomainUuid.of("70cd05cd-72f9-449a-b83b-740d136cd8d4");
		
		mocks.replay();
		testee.clearDomain(domain);
		mocks.verify();
	}

	@Test
	public void clearDomainShouldRemoveScheduledButNotRunningTasks() throws Exception {
		DomainConfiguration configuration = DomainConfiguration.builder()
				.domain(domain)
				.enabled(true)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build();

		DateTime when1 = DateTime.parse("2024-11-1T00:00");
		DateTime when2 = DateTime.parse("2024-11-9T00:00");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");
		RemotelyControlledTask runningTask = createTask(configuration, when1, higherBoundary, runId1);
		RemotelyControlledTask scheduledTask = createTask(configuration, when2, higherBoundary, runId2);

		imapArchiveProcessing.archive(runningTask.getArchiveConfiguration());
		expectLastCall().anyTimes();

		mocks.replay();
		ScheduledTask<ArchiveDomainTask> running = testee.schedule(runningTask);
		ScheduledTask<ArchiveDomainTask> scheduled = testee.schedule(scheduledTask);
		
		assertTaskIsScheduled(runningTask);
		assertTaskIsScheduled(scheduledTask);
		assertTaskIsRunning(runningTask);
		testee.clearDomain(domain.getUuid());
		mocks.verify();
		
		assertThat(scheduled.state()).isEqualTo(State.CANCELED);
		assertThat(running.state()).isEqualTo(State.RUNNING);
	}

	@Test
	public void clearDomainShouldNotRemoveOtherDomainTasks() throws Exception {
		ObmDomain domain1 = ObmDomain.builder().uuid(ObmDomainUuid.of("70cd05cd-72f9-449a-b83b-740d136cd8d4")).name("mydomain.org").build();
		ObmDomain domain2 = ObmDomain.builder().uuid(ObmDomainUuid.of("b9b7ea0f-a65e-4d2e-89b1-fb9ef4d2c97d")).name("mydomain2.org").build();
		DomainConfiguration configuration1 = DomainConfiguration.builder()
				.domain(domain1)
				.enabled(true)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build();
		DomainConfiguration configuration2 = DomainConfiguration.builder()
				.domain(domain2)
				.enabled(true)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build();
		DateTime when1 = DateTime.parse("2024-11-1T00:00");
		DateTime when2 = DateTime.parse("2024-11-9T00:00");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");

		mocks.replay();
		RemotelyControlledTask scheduledTask1 = createTask(configuration1, when1, higherBoundary, runId1);
		ScheduledTask<ArchiveDomainTask> scheduled1 = testee.schedule(scheduledTask1);
		RemotelyControlledTask scheduledTask2 = createTask(configuration2, when2, higherBoundary, runId2);
		ScheduledTask<ArchiveDomainTask> scheduled2 = testee.schedule(scheduledTask2);
		assertTaskIsScheduled(scheduledTask1);
		assertTaskIsScheduled(scheduledTask2);
		testee.clearDomain(domain1.getUuid());
		mocks.verify();
		
		assertThat(scheduled1.state()).isEqualTo(State.CANCELED);
		assertThat(scheduled2.state()).isEqualTo(State.WAITING);
	}

	ArchiveDomainTask archiveDomainTask(DomainConfiguration configuration, ArchiveTreatmentRunId runId, DateTime when) {
		return new ArchiveDomainTask(imapArchiveProcessing, new RealRunTaskStatusChanged.Factory(),
				new ArchiveConfiguration(configuration, when, 
				higherBoundary, runId, logger, loggerAppenders, false));
	}

	void assertTaskProgression(ArchiveDomainTask task) throws Exception {
		assertTaskIsScheduled(task);
		assertTaskIsRunning(task);
		assertTaskIsTerminated(task);
	}

	void assertTaskIsScheduled(ArchiveDomainTask task) throws Exception {
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(busClient.getState(timeout, MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(
				FluentIterable.from(monitor.all()).filter(onlyTaskWithStatusPredicate(State.WAITING)))
				.extracting("task", "scheduledTime")
				.contains(tuple(task, task.getArchiveConfiguration().getWhen()));
	}
	
	void assertTaskIsRunning(ArchiveDomainTask task) throws Exception {
		timeProvider.setCurrent(task.getArchiveConfiguration().getWhen());
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.RUNNING);
		assertThat(busClient.getState(timeout, MILLISECONDS)).isEqualTo(State.RUNNING);
		assertThat(
				Iterables.filter(monitor.all(), onlyTaskWithStatusPredicate(State.RUNNING)))
				.extracting("task", "scheduledTime")
				.contains(tuple(task, task.getArchiveConfiguration().getWhen()));
	}

	void assertTaskIsTerminated(ArchiveDomainTask task) throws Exception {
		((RemotelyControlledTask)task).terminate();
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.TERMINATED);
		assertThat(busClient.getState(timeout, MILLISECONDS)).isEqualTo(State.TERMINATED);
		assertThat(monitor.all()).extracting("task").doesNotContain(task);
	}

	Predicate<ScheduledTask<ArchiveDomainTask>> onlyTaskWithStatusPredicate(final State state) {
		return new Predicate<ScheduledTask<ArchiveDomainTask>>() {

			@Override
			public boolean apply(ScheduledTask<ArchiveDomainTask> input) {
				return input.state() == state;
			}
		};
	}
	
	static class BusModule extends AbstractModule {

		@Override
		protected void configure() {
			bind(BusClient.class).toInstance(new BusClient());
		}
		
	}
	
	static class BusClient implements Client {
		
		final ArrayBlockingQueue<State> states;

		public BusClient() {
			states = Queues.newArrayBlockingQueue(10);
		}
		
		@Subscribe 
		public void onTaskStateChanged(Events.TaskStatusChanged event) throws Exception {
			states.put(event.state());
		}

		State getState(int timeout, TimeUnit unit) throws Exception {
			State state = states.poll(timeout, unit);
			if (state == null) {
				throw new TimeoutException();
			}
			return state;
		}
	}
}
