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

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Events.TaskStatusChanged;

import com.google.common.eventbus.EventBus;
import com.linagora.scheduling.ScheduledTask;

public class ArchiveSchedulerBusTest {

	IMocksControl mocks;
	EventBus bus;
	ArchiveSchedulerBus testee;

	@Before
	public void setUp() {
		mocks = EasyMock.createControl();
		bus = mocks.createMock(EventBus.class);
		testee = new ArchiveSchedulerBus(bus);
	}

	@Test
	public void scheduledShouldPostAsTaskStatusChanged() {
		ScheduledTask<ArchiveDomainTask> task = mocks.createMock(ScheduledTask.class);
		Capture<TaskStatusChanged> postTaskCapture = new Capture<>();
		
		bus.post(capture(postTaskCapture));
		expectLastCall();
		
		mocks.replay();
		testee.scheduled(task);
		mocks.verify();
		
		assertThat(postTaskCapture.getValue().getTask()).isSameAs(task);
	}

	@Test
	public void runningShouldPostAsTaskStatusChanged() {
		ScheduledTask<ArchiveDomainTask> task = mocks.createMock(ScheduledTask.class);
		Capture<TaskStatusChanged> postTaskCapture = new Capture<>();
		
		bus.post(capture(postTaskCapture));
		expectLastCall();
		
		mocks.replay();
		testee.running(task);
		mocks.verify();
		
		assertThat(postTaskCapture.getValue().getTask()).isSameAs(task);
	}

	@Test
	public void terminatedShouldPostAsTaskStatusChanged() {
		ScheduledTask<ArchiveDomainTask> task = mocks.createMock(ScheduledTask.class);
		Capture<TaskStatusChanged> postTaskCapture = new Capture<>();
		
		bus.post(capture(postTaskCapture));
		expectLastCall();
		
		mocks.replay();
		testee.terminated(task);
		mocks.verify();
		
		assertThat(postTaskCapture.getValue().getTask()).isSameAs(task);
	}

	@Test
	public void failedShouldPostAsTaskStatusChanged() {
		Throwable exception = new IllegalStateException();
		ScheduledTask<ArchiveDomainTask> task = mocks.createMock(ScheduledTask.class);
		Capture<TaskStatusChanged> postTaskCapture = new Capture<>();
		
		bus.post(capture(postTaskCapture));
		expectLastCall();
		
		mocks.replay();
		testee.failed(task, exception);
		mocks.verify();
		
		assertThat(postTaskCapture.getValue().getTask()).isSameAs(task);
	}

	@Test
	public void cancelShouldPostAsTaskStatusChanged() {
		ScheduledTask<ArchiveDomainTask> task = mocks.createMock(ScheduledTask.class);
		Capture<TaskStatusChanged> postTaskCapture = new Capture<>();
		
		bus.post(capture(postTaskCapture));
		expectLastCall();
		
		mocks.replay();
		testee.canceled(task);
		mocks.verify();
		
		assertThat(postTaskCapture.getValue().getTask()).isSameAs(task);
	}
}
