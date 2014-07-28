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
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.output.DeferredFileOutputStream;
import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Client;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Events;
import org.obm.imap.archive.scheduling.ControlledTaskFactory.RemotelyControlledTask;
import org.obm.imap.archive.services.ArchiveService;
import org.obm.imap.archive.services.LogFileService;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Queues;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.linagora.scheduling.Monitor;
import com.linagora.scheduling.ScheduledTask;
import com.linagora.scheduling.ScheduledTask.State;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveSchedulerTest {

	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

	int timeout;

	IMocksControl mocks;
	ArchiveService archiveService;
	LogFileService logFileService;
	
	TestDateTimeProvider timeProvider;
	ControlledTaskFactory archiveTaskFactory;
	Monitor<ArchiveDomainTask> monitor;
	FutureTestListener<ArchiveDomainTask> futureListener;
	BusClient busClient;
	ArchiveSchedulerBus bus;
	ArchiveSchedulerQueue queue;
	ArchiveScheduler testee;



	@Before
	public void setUp() {
		timeout = 1500;
		mocks = createControl();
		archiveService = mocks.createMock(ArchiveService.class);
		logFileService = mocks.createMock(LogFileService.class);

		DateTime testsStartTime = DateTime.parse("2024-01-1T05:04Z");
		timeProvider = new TestDateTimeProvider(testsStartTime);
		archiveTaskFactory = new ControlledTaskFactory(archiveService, logFileService); 
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

		queue = new ArchiveSchedulerQueue(monitorFactory);
		busClient = Guice.createInjector(new BusModule()).getInstance(BusClient.class); 
		bus = new ArchiveSchedulerBus(ImmutableSet.<Client>of(busClient));
		testee = new ArchiveScheduler(queue, bus, archiveTaskFactory, timeProvider, MILLISECONDS);
	}
	
	@After
	public void tearDown() throws Exception {
		testee.close();
	}

	@Test
	public void scheduleShouldCallScheduler() throws Exception {
		ObmDomainUuid domain = ObmDomainUuid.of("70cd05cd-72f9-449a-b83b-740d136cd8d4");
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		DateTime when = DateTime.parse("2024-11-1T05:04Z");
		
		expectGetRunLogFile(runId);
		archiveService.archive(eq(domain), eq(runId), anyObject(DeferredFileOutputStream.class));
		expectLastCall();
		
		mocks.replay();
		ArchiveDomainTask task = testee.schedule(domain, when, runId).task();
		assertTaskProgression(task);
		assertThat(monitor.all()).isEmpty();
		mocks.verify();
	}

	@Test
	public void scheduleShouldCallSchedulerWhenPreviousDomainTaskIsDone() throws Exception {
		ObmDomainUuid domain = ObmDomainUuid.of("70cd05cd-72f9-449a-b83b-740d136cd8d4");
		DateTime when1 = DateTime.parse("2024-11-1T05:04Z");
		DateTime when2 = DateTime.parse("2024-11-5T05:04Z");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");

		archiveService.archive(eq(domain), eq(runId1), anyObject(DeferredFileOutputStream.class));
		expectLastCall();
		archiveService.archive(eq(domain), eq(runId2), anyObject(DeferredFileOutputStream.class));
		expectLastCall();

		expectGetRunLogFile(runId1, runId2);

		mocks.replay();
		ArchiveDomainTask task1 = testee.schedule(domain, when1, runId1).task();
		assertTaskProgression(task1);
		ArchiveDomainTask task2 = testee.schedule(domain, when2, runId2).task();
		assertTaskProgression(task2);
		assertThat(monitor.all()).isEmpty();
		mocks.verify();
	}
	
	@Test
	public void scheduleShouldEnqueueWhenTaskForDomainAlreadyScheduled() throws Exception {
		ObmDomainUuid domain = ObmDomainUuid.of("70cd05cd-72f9-449a-b83b-740d136cd8d4");
		DateTime when = DateTime.parse("2024-11-1T05:04Z");
		DateTime whenToEnqueue = DateTime.parse("2024-11-2T05:04Z");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");

		archiveService.archive(eq(domain), eq(runId1), anyObject(DeferredFileOutputStream.class));
		expectLastCall();
		archiveService.archive(eq(domain), eq(runId2), anyObject(DeferredFileOutputStream.class));
		expectLastCall();

		expectGetRunLogFile(runId1, runId2);

		mocks.replay();
		ArchiveDomainTask task = testee.schedule(domain, when, runId1).task();
		ArchiveDomainTask taskEnqueued = testee.schedule(domain, whenToEnqueue, runId2).task();
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
		ObmDomainUuid domain = ObmDomainUuid.of("70cd05cd-72f9-449a-b83b-740d136cd8d4");
		DateTime when = DateTime.parse("2024-11-1T00:00");
		DateTime earlierWhenEnqueuedAfter = DateTime.parse("2024-11-5T00:00");
		DateTime laterWhenEnqueuedBefore = DateTime.parse("2024-11-9T00:00");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");
		ArchiveTreatmentRunId runId3 = ArchiveTreatmentRunId.from("b13c4e34-c70a-446d-a764-17575c4ea52f");

		archiveService.archive(eq(domain), eq(runId1), anyObject(DeferredFileOutputStream.class));
		expectLastCall();
		archiveService.archive(eq(domain), eq(runId2), anyObject(DeferredFileOutputStream.class));
		expectLastCall();
		archiveService.archive(eq(domain), eq(runId3), anyObject(DeferredFileOutputStream.class));
		expectLastCall();

		expectGetRunLogFile(runId1, runId2, runId3);

		mocks.replay();
		ArchiveDomainTask task = testee.schedule(domain, when, runId1).task();
		assertTaskIsScheduled(task);
		assertTaskIsRunning(task);
		ArchiveDomainTask laterTaskEnqueuedBefore = testee.schedule(domain, laterWhenEnqueuedBefore, runId2).task();
		ArchiveDomainTask earlierTaskEnqueuedAfter = testee.schedule(domain, earlierWhenEnqueuedAfter, runId3).task();
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
		ObmDomainUuid domain1 = ObmDomainUuid.of("70cd05cd-72f9-449a-b83b-740d136cd8d4");
		ObmDomainUuid domain2 = ObmDomainUuid.of("b360ac79-a928-4655-a173-59d2c4666cad");
		DateTime when1 = DateTime.parse("2024-11-1T05:04Z");
		DateTime when2 = DateTime.parse("2024-11-2T05:04Z");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");

		archiveService.archive(eq(domain1), eq(runId1), anyObject(DeferredFileOutputStream.class));
		expectLastCall();
		archiveService.archive(eq(domain2), eq(runId2), anyObject(DeferredFileOutputStream.class));
		expectLastCall();

		expectGetRunLogFile(runId1, runId2);

		mocks.replay();
		ArchiveDomainTask taskDomain1 = testee.schedule(domain1, when1, runId1).task();
		ArchiveDomainTask taskDomain2 = testee.schedule(domain2, when2, runId2).task();
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
		ObmDomainUuid domain1 = ObmDomainUuid.of("70cd05cd-72f9-449a-b83b-740d136cd8d4");
		ObmDomainUuid domain2 = ObmDomainUuid.of("b360ac79-a928-4655-a173-59d2c4666cad");
		DateTime when1 = DateTime.parse("2024-11-1T05:04Z");
		DateTime when2 = DateTime.parse("2024-11-2T05:04Z");
		DateTime when1ToEnqueue = DateTime.parse("2024-11-3T05:04Z");
		DateTime when2ToEnqueue = DateTime.parse("2024-11-4T05:04Z");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");
		ArchiveTreatmentRunId runId3 = ArchiveTreatmentRunId.from("b13c4e34-c70a-446d-a764-17575c4ea52f");
		ArchiveTreatmentRunId runId4 = ArchiveTreatmentRunId.from("b1226053-265d-4b0e-a524-e37b1dfcb2e9");

		archiveService.archive(eq(domain1), eq(runId1), anyObject(DeferredFileOutputStream.class));
		expectLastCall();
		archiveService.archive(eq(domain2), eq(runId2), anyObject(DeferredFileOutputStream.class));
		expectLastCall();
		archiveService.archive(eq(domain1), eq(runId3), anyObject(DeferredFileOutputStream.class));
		expectLastCall();
		archiveService.archive(eq(domain2), eq(runId4), anyObject(DeferredFileOutputStream.class));
		expectLastCall();
		
		expectGetRunLogFile(runId1, runId2, runId3, runId4);

		mocks.replay();
		ArchiveDomainTask taskDomain1 = testee.schedule(domain1, when1, runId1).task();
		ArchiveDomainTask taskDomain2 = testee.schedule(domain2, when2, runId2).task();
		ArchiveDomainTask taskDomain1Enqueued = testee.schedule(domain1, when1ToEnqueue, runId3).task();
		ArchiveDomainTask taskDomain2Enqueued = testee.schedule(domain2, when2ToEnqueue, runId4).task();

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
		ObmDomainUuid domain = ObmDomainUuid.of("70cd05cd-72f9-449a-b83b-740d136cd8d4");
		DateTime when1 = DateTime.parse("2024-11-1T00:00");
		DateTime when2 = DateTime.parse("2024-11-9T00:00");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");

		archiveService.archive(eq(domain), eq(runId1), anyObject(DeferredFileOutputStream.class));
		expectLastCall().anyTimes();

		expectGetRunLogFile(runId1, runId2);
		
		mocks.replay();
		ScheduledTask<ArchiveDomainTask> taskRunning = testee.schedule(domain, when1, runId1);
		ScheduledTask<ArchiveDomainTask> taskScheduled = testee.schedule(domain, when2, runId2);
		assertTaskIsScheduled(taskRunning.task());
		assertTaskIsScheduled(taskScheduled.task());
		assertTaskIsRunning(taskRunning.task());
		testee.clearDomain(domain);
		mocks.verify();
		
		assertThat(taskScheduled.state()).isEqualTo(State.CANCELED);
		assertThat(taskRunning.state()).isEqualTo(State.RUNNING);
	}

	@Test
	public void clearDomainShouldNotRemoveOtherDomainTasks() throws Exception {
		ObmDomainUuid domain1 = ObmDomainUuid.of("70cd05cd-72f9-449a-b83b-740d136cd8d4");
		ObmDomainUuid domain2 = ObmDomainUuid.of("b9b7ea0f-a65e-4d2e-89b1-fb9ef4d2c97d");
		DateTime when1 = DateTime.parse("2024-11-1T00:00");
		DateTime when2 = DateTime.parse("2024-11-9T00:00");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");

		expectGetRunLogFile(runId1, runId2);
		
		mocks.replay();
		ScheduledTask<ArchiveDomainTask> taskScheduled1 = testee.schedule(domain1, when1, runId1);
		ScheduledTask<ArchiveDomainTask> taskScheduled2 = testee.schedule(domain2, when2, runId2);
		assertTaskIsScheduled(taskScheduled1.task());
		assertTaskIsScheduled(taskScheduled2.task());
		testee.clearDomain(domain1);
		mocks.verify();
		
		assertThat(taskScheduled1.state()).isEqualTo(State.CANCELED);
		assertThat(taskScheduled2.state()).isEqualTo(State.WAITING);
	}

	private void expectGetRunLogFile(ArchiveTreatmentRunId...runIds) throws Exception {
		for (ArchiveTreatmentRunId runId : runIds) {
			expect(logFileService.getFile(runId)).andReturn(temporaryFolder.newFile());
		}
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
				Iterables.filter(monitor.all(), onlyTaskWithStatusPredicate(State.WAITING)))
				.extracting("task", "scheduledTime")
				.contains(tuple(task, task.getWhen()));
	}
	
	void assertTaskIsRunning(ArchiveDomainTask task) throws Exception {
		timeProvider.setCurrent(task.getWhen());
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.RUNNING);
		assertThat(busClient.getState(timeout, MILLISECONDS)).isEqualTo(State.RUNNING);
		assertThat(
				Iterables.filter(monitor.all(), onlyTaskWithStatusPredicate(State.RUNNING)))
				.extracting("task", "scheduledTime")
				.contains(tuple(task, task.getWhen()));
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
			states.put(event.getTask().state());
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
