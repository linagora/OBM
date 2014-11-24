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
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Events.RealRunTaskStatusChanged;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Events.TaskStatusChanged;

import com.google.common.eventbus.EventBus;
import com.linagora.scheduling.ScheduledTask;
import com.linagora.scheduling.ScheduledTask.State;

public class ArchiveSchedulerBusTest {

	IMocksControl mocks;
	EventBus bus;
	ArchiveSchedulerBus testee;
	ScheduledTask<ArchiveDomainTask> scheduledTask;

	@Before
	public void setUp() {
		mocks = EasyMock.createControl();
		bus = mocks.createMock(EventBus.class);
		scheduledTask = mocks.createMock(ScheduledTask.class);
		testee = new ArchiveSchedulerBus(bus);
	}

	@Test
	public void scheduledShouldPostAsTaskStatusChanged() {
		ScheduledTask<ArchiveDomainTask> task = mocks.createMock(ScheduledTask.class);
		ArchiveDomainTask archiveDomainTask = mocks.createMock(ArchiveDomainTask.class);
		expect(archiveDomainTask.createStatusChangeEvent(State.WAITING))
			.andReturn(new RealRunTaskStatusChanged(State.WAITING, archiveDomainTask));
		expect(task.task()).andReturn(archiveDomainTask);
		expect(task.state()).andReturn(State.WAITING);
		Capture<TaskStatusChanged> postTaskCapture = new Capture<>();
		
		bus.post(capture(postTaskCapture));
		expectLastCall();
		
		mocks.replay();
		testee.scheduled(task);
		mocks.verify();
		
		assertThat(postTaskCapture.getValue().task()).isSameAs(archiveDomainTask);
		assertThat(postTaskCapture.getValue().state()).isEqualTo(State.WAITING);
	}

	@Test
	public void runningShouldPostAsTaskStatusChanged() {
		ScheduledTask<ArchiveDomainTask> task = mocks.createMock(ScheduledTask.class);
		ArchiveDomainTask archiveDomainTask = mocks.createMock(ArchiveDomainTask.class);
		expect(archiveDomainTask.createStatusChangeEvent(State.RUNNING))
			.andReturn(new RealRunTaskStatusChanged(State.RUNNING, archiveDomainTask));
		expect(task.task()).andReturn(archiveDomainTask);
		expect(task.state()).andReturn(State.RUNNING);
		Capture<TaskStatusChanged> postTaskCapture = new Capture<>();
		
		bus.post(capture(postTaskCapture));
		expectLastCall();
		
		mocks.replay();
		testee.running(task);
		mocks.verify();
		
		assertThat(postTaskCapture.getValue().task()).isSameAs(archiveDomainTask);
		assertThat(postTaskCapture.getValue().state()).isEqualTo(State.RUNNING);
	}

	@Test
	public void terminatedShouldPostAsTaskStatusChanged() {
		ScheduledTask<ArchiveDomainTask> task = mocks.createMock(ScheduledTask.class);
		ArchiveDomainTask archiveDomainTask = mocks.createMock(ArchiveDomainTask.class);
		expect(archiveDomainTask.createStatusChangeEvent(State.TERMINATED))
			.andReturn(new RealRunTaskStatusChanged(State.TERMINATED, archiveDomainTask));
		expect(task.task()).andReturn(archiveDomainTask);
		expect(task.state()).andReturn(State.TERMINATED);
		Capture<TaskStatusChanged> postTaskCapture = new Capture<>();
		
		bus.post(capture(postTaskCapture));
		expectLastCall();
		
		mocks.replay();
		testee.terminated(task);
		mocks.verify();
		
		assertThat(postTaskCapture.getValue().task()).isSameAs(archiveDomainTask);
		assertThat(postTaskCapture.getValue().state()).isEqualTo(State.TERMINATED);
	}

	@Test
	public void failedShouldPostAsTaskStatusChanged() {
		Throwable exception = new IllegalStateException();
		ScheduledTask<ArchiveDomainTask> task = mocks.createMock(ScheduledTask.class);
		ArchiveDomainTask archiveDomainTask = mocks.createMock(ArchiveDomainTask.class);
		expect(archiveDomainTask.createStatusChangeEvent(State.FAILED))
			.andReturn(new RealRunTaskStatusChanged(State.FAILED, archiveDomainTask));
		expect(task.task()).andReturn(archiveDomainTask);
		expect(task.state()).andReturn(State.FAILED);
		Capture<TaskStatusChanged> postTaskCapture = new Capture<>();
		
		bus.post(capture(postTaskCapture));
		expectLastCall();
		
		mocks.replay();
		testee.failed(task, exception);
		mocks.verify();
		
		assertThat(postTaskCapture.getValue().task()).isSameAs(archiveDomainTask);
		assertThat(postTaskCapture.getValue().state()).isEqualTo(State.FAILED);
	}

	@Test
	public void cancelShouldPostAsTaskStatusChanged() {
		ScheduledTask<ArchiveDomainTask> task = mocks.createMock(ScheduledTask.class);
		ArchiveDomainTask archiveDomainTask = mocks.createMock(ArchiveDomainTask.class);
		expect(archiveDomainTask.createStatusChangeEvent(State.CANCELED))
			.andReturn(new RealRunTaskStatusChanged(State.CANCELED, archiveDomainTask));
		expect(task.task()).andReturn(archiveDomainTask);
		expect(task.state()).andReturn(State.CANCELED);
		Capture<TaskStatusChanged> postTaskCapture = new Capture<>();
		
		bus.post(capture(postTaskCapture));
		expectLastCall();
		
		mocks.replay();
		testee.canceled(task);
		mocks.verify();
		
		assertThat(postTaskCapture.getValue().task()).isSameAs(archiveDomainTask);
		assertThat(postTaskCapture.getValue().state()).isEqualTo(State.CANCELED);
	}

	@Test
	public void eventsAreImmutable() {
		ScheduledTask<ArchiveDomainTask> task = mocks.createMock(ScheduledTask.class);
		ArchiveDomainTask archiveDomainTask = mocks.createMock(ArchiveDomainTask.class);
		expect(archiveDomainTask.createStatusChangeEvent(State.WAITING))
			.andReturn(new RealRunTaskStatusChanged(State.WAITING, archiveDomainTask));
		expect(archiveDomainTask.createStatusChangeEvent(State.CANCELED))
			.andReturn(new RealRunTaskStatusChanged(State.CANCELED, archiveDomainTask));
		expect(task.task())
			.andReturn(archiveDomainTask).times(2);
		expect(task.state()).andReturn(State.WAITING);
		expect(task.state()).andReturn(State.CANCELED);
		Capture<TaskStatusChanged> scheduledTaskCapture = new Capture<>();
		Capture<TaskStatusChanged> canceledTaskCapture = new Capture<>();
		
		bus.post(capture(scheduledTaskCapture));
		expectLastCall();
		
		bus.post(capture(canceledTaskCapture));
		expectLastCall();
		
		mocks.replay();
		testee.scheduled(task);
		testee.canceled(task);
		mocks.verify();
		
		assertThat(scheduledTaskCapture.getValue().task()).isSameAs(archiveDomainTask);
		assertThat(scheduledTaskCapture.getValue().state()).isSameAs(State.WAITING);
		assertThat(canceledTaskCapture.getValue().task()).isSameAs(archiveDomainTask);
		assertThat(canceledTaskCapture.getValue().state()).isSameAs(State.CANCELED);
	}
}
