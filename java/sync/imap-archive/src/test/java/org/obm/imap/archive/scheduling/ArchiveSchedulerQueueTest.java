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

package org.obm.imap.archive.scheduling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveConfiguration;

import com.linagora.scheduling.Monitor;
import com.linagora.scheduling.ScheduledTask;
import com.linagora.scheduling.ScheduledTask.State;
import com.linagora.scheduling.Scheduler;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveSchedulerQueueTest {

	Scheduler<ArchiveDomainTask> scheduler;
	OnlyOnePerDomainMonitorFactory monitorFactory;
	Monitor<ArchiveDomainTask> monitor;
	TestDateTimeProvider timeProvider;
	ObmDomainUuid domain;
	IMocksControl mocks;
	ArchiveSchedulerQueue testee;

	@Before
	public void setUp() {
		mocks = createControl();
		domain = ObmDomainUuid.of("5859ec4d-8dc2-4143-ba53-e86d6862b4d4");
		monitor = Monitor.<ArchiveDomainTask>builder().build();
		DateTime testsStartTime = DateTime.parse("2024-01-1T05:04Z");
		timeProvider = new TestDateTimeProvider(testsStartTime);
		scheduler = Scheduler.<ArchiveDomainTask>builder()
				.resolution(1, TimeUnit.SECONDS)
				.timeProvider(timeProvider)
				.addListener(monitor)
				.start();
		
		monitorFactory = new OnlyOnePerDomainMonitorFactory() {
			
			@Override
			public Monitor<ArchiveDomainTask> create() {
				return monitor;
			}
		};
		
		testee = new ArchiveSchedulerQueue(monitorFactory);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void putShouldStackDomainTasks() {
		ArchiveDomainTask task1 = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration1 = mocks.createMock(ArchiveConfiguration.class);
		expect(task1.getArchiveConfiguration()).andReturn(configuration1);
		ArchiveDomainTask task2 = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration2 = mocks.createMock(ArchiveConfiguration.class);
		expect(task2.getArchiveConfiguration()).andReturn(configuration2);
		TestScheduledTask scheduled1 = new TestScheduledTask(State.WAITING, task1, scheduler, DateTime.parse("2024-11-1T05:04Z"));
		TestScheduledTask scheduled2 = new TestScheduledTask(State.WAITING, task2, scheduler, DateTime.parse("2024-11-1T15:04Z"));
		expect(configuration1.getDomainId()).andReturn(domain);
		expect(configuration2.getDomainId()).andReturn(domain);
		
		mocks.replay();
		testee.put(scheduled1);
		testee.put(scheduled2);
		mocks.verify();
		
		assertThat(testee.domainTasks.get(domain)).containsOnly(scheduled1, scheduled2);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void putShouldSetTaskInExpectedDomain() {
		ObmDomainUuid domain2 = ObmDomainUuid.of("b9b7ea0f-a65e-4d2e-89b1-fb9ef4d2c97d");
		ArchiveDomainTask task1 = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration1 = mocks.createMock(ArchiveConfiguration.class);
		expect(task1.getArchiveConfiguration()).andReturn(configuration1);
		ArchiveDomainTask task2 = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration2 = mocks.createMock(ArchiveConfiguration.class);
		expect(task2.getArchiveConfiguration()).andReturn(configuration2);
		TestScheduledTask scheduled1 = new TestScheduledTask(State.WAITING, task1, scheduler, DateTime.parse("2024-11-1T05:04Z"));
		TestScheduledTask scheduled2 = new TestScheduledTask(State.WAITING, task2, scheduler, DateTime.parse("2024-11-1T06:04Z"));
		expect(configuration1.getDomainId()).andReturn(domain);
		expect(configuration2.getDomainId()).andReturn(domain2);
		
		mocks.replay();
		testee.put(scheduled1);
		testee.put(scheduled2);
		mocks.verify();
		
		assertThat(testee.domainTasks.get(domain)).containsOnly(scheduled1);
		assertThat(testee.domainTasks.get(domain2)).containsOnly(scheduled2);
	}
	
	@Test
	public void removeShouldReturnFalseIfNonExisting() {
		ArchiveDomainTask task = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration = mocks.createMock(ArchiveConfiguration.class);
		expect(task.getArchiveConfiguration()).andReturn(configuration);
		TestScheduledTask scheduled = new TestScheduledTask(State.WAITING, task, scheduler, DateTime.parse("2024-11-1T06:04Z"));
		expect(configuration.getDomainId()).andReturn(domain);
		
		mocks.replay();
		testee.remove(scheduled);
		mocks.verify();
		
		assertThat(testee.domainTasks.get(domain)).isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void removeShouldDeleteOneFromStack() {
		ArchiveDomainTask task1 = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration1 = mocks.createMock(ArchiveConfiguration.class);
		expect(task1.getArchiveConfiguration()).andReturn(configuration1).anyTimes();
		ArchiveDomainTask task2 = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration2 = mocks.createMock(ArchiveConfiguration.class);
		expect(task2.getArchiveConfiguration()).andReturn(configuration2).anyTimes();
		TestScheduledTask scheduled1 = new TestScheduledTask(State.WAITING, task1, scheduler, DateTime.parse("2024-11-1T06:04Z"));
		TestScheduledTask scheduled2 = new TestScheduledTask(State.WAITING, task2, scheduler, DateTime.parse("2024-11-1T06:04Z"));
		expect(configuration1.getDomainId()).andReturn(domain).times(2);
		expect(configuration2.getDomainId()).andReturn(domain);
		
		mocks.replay();
		testee.put(scheduled1);
		testee.put(scheduled2);
		testee.remove(scheduled1);
		mocks.verify();
		
		assertThat(testee.domainTasks.get(domain)).containsOnly(scheduled2);
	}
	
	@Test
	public void pollShouldReturnEmptyWhenNoTask() {
		mocks.replay();
		Collection<ScheduledTask<ArchiveDomainTask>> tasks = testee.poll();
		mocks.verify();
		
		assertThat(tasks).isEmpty();
	}
	
	@Test
	public void pollShouldReturnEmptyWhenTaskButScheduleTimeNotPassed() {
		ArchiveDomainTask task = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration = mocks.createMock(ArchiveConfiguration.class);
		expect(task.getArchiveConfiguration()).andReturn(configuration);
		TestScheduledTask scheduled = new TestScheduledTask(State.WAITING, task, scheduler, DateTime.parse("2024-11-1T06:04Z"));
		expect(configuration.getDomainId()).andReturn(domain);
		
		mocks.replay();
		testee.put(scheduled);
		timeProvider.setCurrent(scheduled.scheduledTime().minusHours(1));
		Collection<ScheduledTask<ArchiveDomainTask>> tasks = testee.poll();
		mocks.verify();
		
		assertThat(tasks).isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void pollShouldReturnDomainTaskWhenNoneRunning() {
		ArchiveDomainTask task = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration = mocks.createMock(ArchiveConfiguration.class);
		expect(task.getArchiveConfiguration()).andReturn(configuration).anyTimes();
		TestScheduledTask scheduled = new TestScheduledTask(State.WAITING, task, scheduler, DateTime.parse("2024-11-1T06:04Z"));
		expect(configuration.getDomainId()).andReturn(domain).times(2);
		
		monitor.scheduled(scheduled);
		
		mocks.replay();
		testee.put(scheduled);
		timeProvider.setCurrent(scheduled.scheduledTime());
		Collection<ScheduledTask<ArchiveDomainTask>> tasks = testee.poll();
		mocks.verify();
		
		assertThat(tasks).containsOnly(scheduled);
	}

	@Test
	public void pollShouldReturnEmptyWhenDomainTaskIsRunning() {
		ArchiveDomainTask task1 = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration1 = mocks.createMock(ArchiveConfiguration.class);
		expect(task1.getArchiveConfiguration()).andReturn(configuration1);
		ArchiveDomainTask task2 = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration2 = mocks.createMock(ArchiveConfiguration.class);
		expect(task2.getArchiveConfiguration()).andReturn(configuration2);
		TestScheduledTask scheduled = new TestScheduledTask(State.WAITING, task1, scheduler, DateTime.parse("2024-11-1T06:04Z"));
		TestScheduledTask running = new TestScheduledTask(State.RUNNING, task2, scheduler, DateTime.parse("2024-11-1T01:04Z"));
		expect(configuration1.getDomainId()).andReturn(domain);
		expect(configuration2.getDomainId()).andReturn(domain);

		monitor.scheduled(running);
		
		mocks.replay();
		testee.put(scheduled);
		timeProvider.setCurrent(scheduled.scheduledTime());
		Collection<ScheduledTask<ArchiveDomainTask>> tasks = testee.poll();
		mocks.verify();
		
		assertThat(tasks).isEmpty();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void pollShouldReturnOnlyOneTaskPerDomain() {
		ObmDomainUuid domain2 = ObmDomainUuid.of("b9b7ea0f-a65e-4d2e-89b1-fb9ef4d2c97d");
		ArchiveDomainTask task1 = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration1 = mocks.createMock(ArchiveConfiguration.class);
		expect(task1.getArchiveConfiguration()).andReturn(configuration1);
		ArchiveDomainTask task2 = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration2 = mocks.createMock(ArchiveConfiguration.class);
		expect(task2.getArchiveConfiguration()).andReturn(configuration2);
		ArchiveDomainTask task3 = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration3 = mocks.createMock(ArchiveConfiguration.class);
		expect(task3.getArchiveConfiguration()).andReturn(configuration3);
		TestScheduledTask scheduled1 = new TestScheduledTask(State.WAITING, task1, scheduler, DateTime.parse("2024-11-1T01:04Z"));
		TestScheduledTask scheduled2 = new TestScheduledTask(State.WAITING, task2, scheduler, DateTime.parse("2024-11-1T03:04Z"));
		TestScheduledTask scheduledTheLater = new TestScheduledTask(State.WAITING, task3, scheduler, DateTime.parse("2024-11-1T06:04Z"));
		expect(configuration1.getDomainId()).andReturn(domain);
		expect(configuration2.getDomainId()).andReturn(domain2);
		expect(configuration3.getDomainId()).andReturn(domain);
		
		mocks.replay();
		testee.put(scheduled1);
		testee.put(scheduled2);
		testee.put(scheduledTheLater);
		timeProvider.setCurrent(scheduledTheLater.scheduledTime());
		Collection<ScheduledTask<ArchiveDomainTask>> tasks = testee.poll();
		mocks.verify();
		
		assertThat(tasks).containsOnly(scheduled1, scheduled2);
	}
	
	@Test
	public void hasAnyTaskShouldFalseWhenNone() {
		mocks.replay();
		boolean hasAnyTask = testee.hasAnyTask();
		mocks.verify();
		
		assertThat(hasAnyTask).isFalse();
	}
	
	@Test
	public void hasAnyTaskShouldReturnTrueWhenOne() {
		ArchiveDomainTask task = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration = mocks.createMock(ArchiveConfiguration.class);
		expect(task.getArchiveConfiguration()).andReturn(configuration);
		TestScheduledTask expectedScheduled = new TestScheduledTask(State.WAITING, task, scheduler, DateTime.parse("2024-11-1T01:04Z"));
		expect(configuration.getDomainId()).andReturn(domain);
		
		mocks.replay();
		testee.put(expectedScheduled);
		
		boolean hasAnyTask = testee.hasAnyTask();
		mocks.verify();
		
		assertThat(hasAnyTask).isTrue();
	}
	
	@Test
	public void hasAnyTaskShouldReturnTrueWhenMulti() {
		ObmDomainUuid domain2 = ObmDomainUuid.of("b9b7ea0f-a65e-4d2e-89b1-fb9ef4d2c97d");
		
		ArchiveDomainTask task1 = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration1 = mocks.createMock(ArchiveConfiguration.class);
		expect(task1.getArchiveConfiguration()).andReturn(configuration1);
		TestScheduledTask scheduled1 = new TestScheduledTask(State.WAITING, task1, scheduler, DateTime.parse("2024-11-1T01:04Z"));
		expect(configuration1.getDomainId()).andReturn(domain);
		
		ArchiveDomainTask task2 = mocks.createMock(ArchiveDomainTask.class);
		ArchiveConfiguration configuration2 = mocks.createMock(ArchiveConfiguration.class);
		expect(task2.getArchiveConfiguration()).andReturn(configuration2);
		TestScheduledTask expectedScheduled = new TestScheduledTask(State.WAITING, task2, scheduler, DateTime.parse("2024-11-1T03:04Z"));
		expect(configuration2.getDomainId()).andReturn(domain2);
		
		mocks.replay();
		testee.put(scheduled1);
		testee.put(expectedScheduled);
		
		boolean hasAnyTask = testee.hasAnyTask();
		mocks.verify();
		
		assertThat(hasAnyTask).isTrue();
	}
}
