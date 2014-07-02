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

import java.util.UUID;

import org.apache.commons.io.output.DeferredFileOutputStream;
import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.scheduling.ControlledTaskFactory.RemotelyControlledTask;
import org.obm.imap.archive.services.ArchiveService;

import com.linagora.scheduling.DateTimeProvider;
import com.linagora.scheduling.Monitor;
import com.linagora.scheduling.ScheduledTask.State;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class OnlyOnePerDomainSchedulerTest {

	private static final DateTime THE_BEGINNING = DateTime.parse("1970-01-01T00:00");
	IMocksControl mocksControl;
	ArchiveService archiveService;
	
	DateTime now;
	TestDateTimeProvider timeProvider;
	ControlledTaskFactory archiveTaskFactory;
	Monitor<ArchiveDomainTask> monitor;
	FutureTestListener<ArchiveDomainTask> futureListener;
	OnlyOnePerDomainScheduler testee;
	
	int timeout;

	@Before
	public void setUp() {
		timeout = 1500;
		mocksControl = createControl();
		archiveService = mocksControl.createMock(ArchiveService.class);

		now = DateTime.parse("2024-10-1T05:04");
		timeProvider = new TestDateTimeProvider(now);
		archiveTaskFactory = new ControlledTaskFactory(archiveService); 
		futureListener = new FutureTestListener<>();
		Monitor.Builder<ArchiveDomainTask> monitorBuilder = Monitor.<ArchiveDomainTask>builder().addListener(futureListener);
		
		testee = new OnlyOnePerDomainScheduler(archiveTaskFactory, monitorBuilder, timeProvider, MILLISECONDS);
		monitor = testee.getMonitor();
	}
	
	@After
	public void tearDown() throws Exception {
		testee.close();
	}

	public static class TestDateTimeProvider implements DateTimeProvider {
		
		private DateTime current;

		public TestDateTimeProvider(DateTime start) {
			this.current = start;
		}
		
		public void setCurrent(DateTime current) {
			this.current = current;
		}
		
		@Override
		public DateTime now() {
			return current;
		}
	}
	
	@Test
	public void scheduleShouldCallScheduler() throws Exception {
		ObmDomain domain = dummyDomain();
		DateTime when = DateTime.parse("2024-11-1T05:04");
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		expect(archiveService.archive(eq(domain), eq(runId), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId, when, domain.getUuid()));
		
		mocksControl.replay();
		ArchiveDomainTask task = testee.scheduleDomainArchiving(domain, when, runId);
		assertTaskProgression(task);
		assertThat(monitor.all()).isEmpty();
		mocksControl.verify();
	}

	@Test
	public void scheduleShouldCallSchedulerWhenPreviousDomainTaskIsDone() throws Exception {
		ObmDomain domain = dummyDomain();
		DateTime when1 = DateTime.parse("2024-11-1T05:04");
		DateTime when2 = DateTime.parse("2024-11-5T05:04");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");
		
		expect(archiveService.archive(eq(domain), eq(runId1), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId1, when1, domain.getUuid()));
		expect(archiveService.archive(eq(domain), eq(runId2), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId2, when2, domain.getUuid()));
		
		mocksControl.replay();
		ArchiveDomainTask task1 = testee.scheduleDomainArchiving(domain, when1, runId1);
		assertTaskProgression(task1);
		ArchiveDomainTask task2 = testee.scheduleDomainArchiving(domain, when2, runId2);
		assertTaskProgression(task2);
		assertThat(monitor.all()).isEmpty();
		mocksControl.verify();
	}
	
	@Test
	public void scheduleShouldEnqueueWhenTaskForDomainAlreadyScheduled() throws Exception {
		ObmDomain domain = dummyDomain();
		DateTime when = DateTime.parse("2024-11-1T05:04");
		DateTime whenToEnqueue = DateTime.parse("2024-11-2T05:04");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");
		
		expect(archiveService.archive(eq(domain), eq(runId1), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId1, when, domain.getUuid()));
		expect(archiveService.archive(eq(domain), eq(runId2), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId2, whenToEnqueue, domain.getUuid()));
		
		mocksControl.replay();
		ArchiveDomainTask task = testee.scheduleDomainArchiving(domain, when, runId1);
		ArchiveDomainTask taskEnqueued = testee.scheduleDomainArchiving(domain, whenToEnqueue, runId2);
		assertTaskProgression(task);
		assertTaskProgression(taskEnqueued);
		assertThat(monitor.all()).isEmpty();
		mocksControl.verify();
	}
	
	@Test
	public void scheduleShouldEnqueueRespectingOrderWhenTaskForDomainAlreadyScheduled() throws Exception {
		ObmDomain domain = dummyDomain();
		DateTime when = DateTime.parse("2024-11-1T00:00");
		DateTime whenToEnqueueAfter = DateTime.parse("2024-11-9T00:00");
		DateTime whenToEnqueueBefore = DateTime.parse("2024-11-5T00:00");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");
		ArchiveTreatmentRunId runId3 = ArchiveTreatmentRunId.from("b13c4e34-c70a-446d-a764-17575c4ea52f");
		
		expect(archiveService.archive(eq(domain), eq(runId1), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId1, when, domain.getUuid()));
		expect(archiveService.archive(eq(domain), eq(runId2), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId2, whenToEnqueueAfter, domain.getUuid()));
		expect(archiveService.archive(eq(domain), eq(runId3), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId3, whenToEnqueueBefore, domain.getUuid()));
		
		mocksControl.replay();
		ArchiveDomainTask task = testee.scheduleDomainArchiving(domain, when, runId1);
		ArchiveDomainTask taskEnqueuedAfter = testee.scheduleDomainArchiving(domain, whenToEnqueueAfter, runId2);
		ArchiveDomainTask taskEnqueuedBefore = testee.scheduleDomainArchiving(domain, whenToEnqueueBefore, runId3);
		assertTaskProgression(task);
		assertTaskProgression(taskEnqueuedBefore);
		assertTaskProgression(taskEnqueuedAfter);
		assertThat(monitor.all()).isEmpty();
		mocksControl.verify();
	}

	private void assertTaskProgression(ArchiveDomainTask task) throws Exception {
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(monitor.all()).extracting("task", "scheduledTime").containsOnlyOnce(tuple(task, task.getWhen()));
		timeProvider.setCurrent(task.getWhen());
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.RUNNING);
		assertThat(monitor.all()).extracting("task", "scheduledTime").containsOnlyOnce(tuple(task, task.getWhen()));
		((RemotelyControlledTask)task).terminate();
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.TERMINATED);
	}
	
	@Test
	public void scheduleDifferentDomainsShouldCallSchedulerForBoth() throws Exception {
		ObmDomain domain1 = dummyDomain();
		ObmDomain domain2 = dummyDomain();
		DateTime when1 = DateTime.parse("2024-11-1T05:04");
		DateTime when2 = DateTime.parse("2024-11-2T05:04");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");
		
		expect(archiveService.archive(eq(domain1), eq(runId1), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId1, when1, domain1.getUuid()));
		expect(archiveService.archive(eq(domain2), eq(runId2), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId2, when2, domain2.getUuid()));
		
		mocksControl.replay();
		ArchiveDomainTask taskDomain1 = testee.scheduleDomainArchiving(domain1, when1, runId1);
		ArchiveDomainTask taskDomain2 = testee.scheduleDomainArchiving(domain2, when2, runId2);
		
		// BOTH ARE WAITING
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(monitor.all()).extracting("task", "scheduledTime").containsOnlyOnce(
				tuple(taskDomain1, when1),
				tuple(taskDomain2, when2));

		// COMING TIME TO SCHEDULE THEM
		timeProvider.setCurrent(when1);
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.RUNNING);
		timeProvider.setCurrent(when2);
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.RUNNING);
		
		// FIRST TASK TERMINATES		
		((RemotelyControlledTask)taskDomain1).terminate();
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.TERMINATED);
		assertThat(monitor.all()).extracting("task", "scheduledTime").containsOnlyOnce(
				tuple(taskDomain2, when2));
		
		// SECOND TASK TERMINATES		
		((RemotelyControlledTask)taskDomain2).terminate();
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.TERMINATED);
		
		assertThat(monitor.all()).isEmpty();
		mocksControl.verify();
	}
	
	@Test
	public void scheduleDifferentDomainsShouldCallSchedulerForBothThenEnqueueOthers() throws Exception {
		ObmDomain domain1 = dummyDomain();
		ObmDomain domain2 = dummyDomain();
		DateTime when1 = DateTime.parse("2024-11-1T05:04");
		DateTime when2 = DateTime.parse("2024-11-2T05:04");
		DateTime when1ToEnqueue = DateTime.parse("2024-11-3T05:04");
		DateTime when2ToEnqueue = DateTime.parse("2024-11-4T05:04");
		ArchiveTreatmentRunId runId1 = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		ArchiveTreatmentRunId runId2 = ArchiveTreatmentRunId.from("14a311d0-aa84-4aed-ba33-f796a6283e50");
		ArchiveTreatmentRunId runId3 = ArchiveTreatmentRunId.from("b13c4e34-c70a-446d-a764-17575c4ea52f");
		ArchiveTreatmentRunId runId4 = ArchiveTreatmentRunId.from("b1226053-265d-4b0e-a524-e37b1dfcb2e9");
		
		expect(archiveService.archive(eq(domain1), eq(runId1), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId1, when1, domain1.getUuid()));
		expect(archiveService.archive(eq(domain2), eq(runId2), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId2, when2, domain2.getUuid()));
		expect(archiveService.archive(eq(domain1), eq(runId3), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId3, when1ToEnqueue, domain1.getUuid()));
		expect(archiveService.archive(eq(domain2), eq(runId4), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId4, when2ToEnqueue, domain2.getUuid()));
		
		mocksControl.replay();
		ArchiveDomainTask taskDomain1 = testee.scheduleDomainArchiving(domain1, when1, runId1);
		ArchiveDomainTask taskDomain2 = testee.scheduleDomainArchiving(domain2, when2, runId2);
		ArchiveDomainTask taskDomain1Enqueued = testee.scheduleDomainArchiving(domain1, when1ToEnqueue, runId3);
		ArchiveDomainTask taskDomain2Enqueued = testee.scheduleDomainArchiving(domain2, when2ToEnqueue, runId4);
		
		// BOTH ARE WAITING
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.WAITING);
		assertThat(monitor.all()).extracting("task", "scheduledTime").containsOnlyOnce(
				tuple(taskDomain1, when1),
				tuple(taskDomain2, when2));
		
		// COMING TIME TO SCHEDULE THEM
		timeProvider.setCurrent(taskDomain1.getWhen());
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.RUNNING);
		timeProvider.setCurrent(taskDomain2.getWhen());
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.RUNNING);
		
		// DOMAIN1 FIRST TASK TERMINATES		
		((RemotelyControlledTask)taskDomain1).terminate();
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.TERMINATED);
		assertThat(monitor.all()).extracting("task", "scheduledTime").containsOnlyOnce(
				tuple(taskDomain2, when2));
		
		// ENQUEUED TASK OF DOMAIN1 SHOULD BE SCHEDULED THEN RUN
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.WAITING);
		timeProvider.setCurrent(when1ToEnqueue);
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.RUNNING);
		assertThat(monitor.all()).extracting("task", "scheduledTime").containsOnlyOnce(
				tuple(taskDomain1Enqueued, when1ToEnqueue),
				tuple(taskDomain2, when2));
		
		// DOMAIN2 FIRST TASK TERMINATES
		((RemotelyControlledTask)taskDomain2).terminate();
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.TERMINATED);
		assertThat(monitor.all()).extracting("task", "scheduledTime").containsOnlyOnce(
				tuple(taskDomain1Enqueued, when1ToEnqueue));

		// ENQUEUED TASK OF DOMAIN2 SHOULD BE SCHEDULED THEN RUN
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.WAITING);
		timeProvider.setCurrent(when2ToEnqueue);
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.RUNNING);
		assertThat(monitor.all()).extracting("task", "scheduledTime").containsOnlyOnce(
				tuple(taskDomain1Enqueued, when1ToEnqueue),
				tuple(taskDomain2Enqueued, when2ToEnqueue));

		// DOMAIN1 SECOND TASK TERMINATES		
		((RemotelyControlledTask)taskDomain1Enqueued).terminate();
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.TERMINATED);
		assertThat(monitor.all()).extracting("task", "scheduledTime").containsOnlyOnce(
				tuple(taskDomain2Enqueued, when2ToEnqueue));
		
		// DOMAIN2 SECOND TASK TERMINATES		
		((RemotelyControlledTask)taskDomain2Enqueued).terminate();
		assertThat(futureListener.getNextState(timeout, MILLISECONDS)).isEqualTo(State.TERMINATED);
		
		assertThat(monitor.all()).isEmpty();
		mocksControl.verify();
	}
	
	@Test
	public void scheduleNowShouldCallScheduler() throws Exception {
		ObmDomain domain = dummyDomain();
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ff43907a-af02-4509-b66b-a712a4da6146");
		expect(archiveService.archive(eq(domain), eq(runId), anyObject(DeferredFileOutputStream.class)))
			.andReturn(archiveTreatment(runId, now, domain.getUuid()));
		
		mocksControl.replay();
		ArchiveDomainTask task = testee.scheduleNowDomainArchiving(domain, now, runId);
		assertTaskProgression(task);
		assertThat(monitor.all()).isEmpty();
		mocksControl.verify();
	}
	
	private ObmDomain dummyDomain() {
		return ObmDomain.builder()
				.id(4)
				.uuid(ObmDomainUuid.of(UUID.randomUUID()))
				.name("domain.test")
				.build();
	}
	
	private ArchiveTreatment archiveTreatment(ArchiveTreatmentRunId runId, DateTime start, ObmDomainUuid domainId) {
		return ArchiveTreatment.builder()
				.runId(runId)
				.domainId(domainId)
				.archiveStatus(ArchiveStatus.SUCCESS)
				.start(start)
				.end(THE_BEGINNING)
				.lowerBoundary(THE_BEGINNING)
				.higherBoundary(THE_BEGINNING)
				.build();
	}
}
