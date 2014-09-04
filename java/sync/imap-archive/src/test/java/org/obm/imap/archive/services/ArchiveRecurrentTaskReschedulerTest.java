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
package org.obm.imap.archive.services;

import static org.easymock.EasyMock.expect;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.scheduling.ArchiveDomainTask;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Events.TaskStatusChanged;
import org.obm.imap.archive.scheduling.ArchiveSchedulingService;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.linagora.scheduling.ScheduledTask.State;
import com.linagora.scheduling.Scheduler;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveRecurrentTaskReschedulerTest {

	Logger logger;
	ArchiveTreatmentRunId runId;
	ObmDomainUuid domain;
	DateTime scheduledTime;
	
	IMocksControl mocks;
	ArchiveSchedulingService schedulingService;
	Scheduler<ArchiveDomainTask> scheduler;
	ArchiveDomainTask task;
	ArchiveRecurrentTaskRescheduler testee;

	@Before
	public void setUp() {
		logger = LoggerFactory.getLogger(ArchiveRecurrentTaskRescheduler.class);
		runId = ArchiveTreatmentRunId.from("38efaa5c-6d46-419c-97e6-6e6c6d9cbed3");
		domain = ObmDomainUuid.of("f7d9e710-1863-48dc-af78-bdd59cf6d82f");
		scheduledTime = DateTime.parse("2024-11-1T01:04Z");
		
		mocks = EasyMock.createControl();
		schedulingService = mocks.createMock(ArchiveSchedulingService.class);
		scheduler = mocks.createMock(Scheduler.class);
		task = mocks.createMock(ArchiveDomainTask.class);
		testee = new ArchiveRecurrentTaskRescheduler(logger, schedulingService);
	}

	@Test
	public void onChangeShouldDoNothingWhenNew() {
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.NEW));
		mocks.verify();
	}
	
	@Test
	public void onChangeShouldDoNothingWhenWaiting() {
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.WAITING));
		mocks.verify();
	}
	
	@Test
	public void onChangeShouldDoNothingWhenRunning() {
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.RUNNING));
		mocks.verify();
	}
	
	@Test
	public void onChangeShouldDoNothingWhenCancel() {
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.CANCELED));
		mocks.verify();
	}
	
	@Test
	public void onChangeShouldDoNothingWhenFailedAndNotRecurrent() {
		expect(task.isRecurrent()).andReturn(false);
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.FAILED));
		mocks.verify();
	}
	
	@Test
	public void onChangeShouldDoNothingWhenTerminatedAndNotRecurrent() {
		expect(task.isRecurrent()).andReturn(false);
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.TERMINATED));
		mocks.verify();
	}
	
	@Test
	public void onChangeShouldRescheduleWhenFailedAndRecurrent() throws Exception {
		expect(task.isRecurrent()).andReturn(true);
		expect(task.getDomain()).andReturn(domain);
		expect(schedulingService.scheduleAsRecurrent(domain)).andReturn(runId);
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.FAILED));
		mocks.verify();
	}
	
	@Test
	public void onChangeShouldRescheduleWhenTerminatedAndRecurrent() throws Exception {
		expect(task.isRecurrent()).andReturn(true);
		expect(task.getDomain()).andReturn(domain);
		expect(schedulingService.scheduleAsRecurrent(domain)).andReturn(runId);
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.TERMINATED));
		mocks.verify();
	}
	
	@Test
	public void onChangeShouldNotPropagateExceptionWhenDaoException() throws Exception {
		expect(task.isRecurrent()).andReturn(true);
		expect(task.getDomain()).andReturn(domain);
		expect(schedulingService.scheduleAsRecurrent(domain)).andThrow(new DaoException("error"));
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.TERMINATED));
		mocks.verify();
	}
}
