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
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveRunningTreatment;
import org.obm.imap.archive.beans.ArchiveScheduledTreatment;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTerminatedTreatment;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentKind;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.scheduling.ArchiveDomainTask;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Events.TaskStatusChanged;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.slf4j.Logger;

import com.google.common.base.Optional;
import com.linagora.scheduling.DateTimeProvider;
import com.linagora.scheduling.ScheduledTask.State;
import com.linagora.scheduling.Scheduler;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveDaoTrackingTest {

	ArchiveTreatmentRunId runId;
	ObmDomainUuid domain;
	DateTime scheduledTime;
	DateTime startTime;
	DateTime endTime;
	DateTime higherBoundary;

	IMocksControl mocks;
	Logger logger;
	ArchiveTreatmentDao dao;
	DateTimeProvider timeProvider;
	Scheduler<ArchiveDomainTask> scheduler;
	ArchiveDomainTask task;
	ArchiveDaoTracking testee;

	@Before
	public void setUp() {
		runId = ArchiveTreatmentRunId.from("38efaa5c-6d46-419c-97e6-6e6c6d9cbed3");
		domain = ObmDomainUuid.of("f7d9e710-1863-48dc-af78-bdd59cf6d82f");
		scheduledTime = DateTime.parse("2024-11-1T01:04Z");
		startTime = DateTime.parse("2024-11-2T01:04Z");
		endTime = DateTime.parse("2024-11-5T01:04Z");
		higherBoundary = DateTime.parse("2024-12-1T01:04Z");
		
		mocks = EasyMock.createControl();
		task = mocks.createMock(ArchiveDomainTask.class);
		logger = mocks.createMock(Logger.class);
		dao = mocks.createMock(ArchiveTreatmentDao.class);
		timeProvider = mocks.createMock(DateTimeProvider.class);
		expect(task.getDomain()).andReturn(domain).anyTimes();
		expect(task.getHigherBoundary()).andReturn(higherBoundary).anyTimes();
		expect(task.getWhen()).andReturn(scheduledTime).anyTimes();
		expect(task.getRunId()).andReturn(runId).anyTimes();
		expect(task.isRecurrent()).andReturn(true).anyTimes();
		
		scheduler = Scheduler.<ArchiveDomainTask>builder().timeProvider(timeProvider).start();
		testee = new ArchiveDaoTracking(logger, dao, timeProvider);
	}

	@Test
	public void eventShouldDoNothingWhenTaskStateIsNew() {
		logger.info("A task has been created but the state {} is not tracked", State.NEW);
		expectLastCall();
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.NEW));
		mocks.verify();
	}
	
	@Test
	public void eventShouldInsertWhenTaskStateIsWaitingAndNotFound() throws Exception {
		logger.info("Insert a task as {} for domain {}, scheduled at {} with id {}", 
				ArchiveStatus.SCHEDULED, domain.get(), scheduledTime, runId);
		expectLastCall();
		expect(task.getArchiveTreatmentKind()).andReturn(ArchiveTreatmentKind.REAL_RUN);
		
		expect(dao.find(runId)).andReturn(Optional.<ArchiveTreatment>absent());
		dao.insert(ArchiveScheduledTreatment
			.forDomain(domain)
			.runId(runId)
			.recurrent(true)
			.higherBoundary(higherBoundary)
			.scheduledAt(scheduledTime)
			.build());
		expectLastCall();
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.WAITING));
		mocks.verify();
	}
	
	@Test
	public void eventShouldUpdateWhenTaskStateIsWaitingButFound() throws Exception {
		DateTime daoScheduledTime = DateTime.parse("2029-09-9T01:04Z");
		
		logger.info("Update a task as {} for domain {}, scheduled at {} with id {}", 
				State.WAITING, domain.get(), scheduledTime, runId);
		expectLastCall();
		expect(task.getArchiveTreatmentKind()).andReturn(ArchiveTreatmentKind.REAL_RUN);

		expect(dao.find(runId)).andReturn(Optional.<ArchiveTreatment>of(
				ArchiveScheduledTreatment
				.forDomain(domain)
				.runId(runId)
				.recurrent(true)
				.higherBoundary(higherBoundary)
				.scheduledAt(daoScheduledTime)
				.build()));
		
		dao.update(ArchiveScheduledTreatment
			.forDomain(domain)
			.runId(runId)
			.recurrent(true)
			.higherBoundary(higherBoundary)
			.scheduledAt(scheduledTime)
			.build());
		expectLastCall();
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.WAITING));
		mocks.verify();
	}
	
	@Test
	public void eventShouldUpdateWhenTaskStateIsRunningAndFound() throws Exception {
		DateTime daoScheduledTime = DateTime.parse("2029-09-9T01:04Z");
		
		expect(timeProvider.now()).andReturn(startTime);
		logger.info("Update a task as {} for domain {}, scheduled at {} with id {}", 
				State.RUNNING, domain.get(), scheduledTime, runId);
		expectLastCall();
		expect(task.getArchiveTreatmentKind()).andReturn(ArchiveTreatmentKind.REAL_RUN);

		expect(dao.find(runId)).andReturn(Optional.<ArchiveTreatment>of(
				ArchiveScheduledTreatment
				.forDomain(domain)
				.runId(runId)
				.recurrent(true)
				.higherBoundary(higherBoundary)
				.scheduledAt(daoScheduledTime)
				.build()));
		
		dao.update(ArchiveRunningTreatment
			.forDomain(domain)
			.runId(runId)
			.recurrent(true)
			.higherBoundary(higherBoundary)
			.scheduledAt(scheduledTime)
			.startedAt(startTime)
			.build());
		expectLastCall();
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.RUNNING));
		mocks.verify();
	}
	
	@Test
	public void eventShouldLogErrorWhenTaskStateIsRunningButNotFound() throws DaoException {
		expect(task.getArchiveTreatmentKind()).andReturn(ArchiveTreatmentKind.REAL_RUN);
		expect(dao.find(runId)).andReturn(Optional.<ArchiveTreatment>absent());
		
		logger.error("Only task with status {} can be created, received {}", State.WAITING, State.RUNNING);
		expectLastCall();
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.RUNNING));
		mocks.verify();
	}

	@Test
	public void eventShouldUpdateWhenTaskStateIsFailed() throws Exception {
		DateTime daoScheduledTime = DateTime.parse("2029-09-9T01:04Z");
		
		expect(timeProvider.now()).andReturn(endTime);
		logger.info("Update a task as {} for domain {}, scheduled at {} with id {}", 
				State.FAILED, domain.get(), scheduledTime, runId);
		expectLastCall();
		expect(task.getArchiveTreatmentKind()).andReturn(ArchiveTreatmentKind.REAL_RUN);

		expect(dao.find(runId)).andReturn(Optional.<ArchiveTreatment>of(
				ArchiveRunningTreatment
				.forDomain(domain)
				.runId(runId)
				.recurrent(true)
				.higherBoundary(higherBoundary)
				.scheduledAt(daoScheduledTime)
				.startedAt(startTime)
				.build()));
		
		dao.update(ArchiveTerminatedTreatment
			.forDomain(domain)
			.runId(runId)
			.recurrent(true)
			.higherBoundary(higherBoundary)
			.scheduledAt(scheduledTime)
			.startedAt(startTime)
			.terminatedAt(endTime)
			.status(ArchiveStatus.ERROR)
			.build());
		expectLastCall();
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.FAILED));
		mocks.verify();
	}

	@Test
	public void eventShouldUpdateWhenTaskStateIsSuccess() throws Exception {
		DateTime daoScheduledTime = DateTime.parse("2029-09-9T01:04Z");
		
		expect(timeProvider.now()).andReturn(endTime);
		logger.info("Update a task as {} for domain {}, scheduled at {} with id {}", 
				State.TERMINATED, domain.get(), scheduledTime, runId);
		expectLastCall();
		expect(task.getArchiveTreatmentKind()).andReturn(ArchiveTreatmentKind.REAL_RUN);

		expect(dao.find(runId)).andReturn(Optional.<ArchiveTreatment>of(
				ArchiveRunningTreatment
				.forDomain(domain)
				.runId(runId)
				.recurrent(true)
				.higherBoundary(higherBoundary)
				.scheduledAt(daoScheduledTime)
				.startedAt(startTime)
				.build()));
		
		dao.update(ArchiveTerminatedTreatment
			.forDomain(domain)
			.runId(runId)
			.recurrent(true)
			.higherBoundary(higherBoundary)
			.scheduledAt(scheduledTime)
			.startedAt(startTime)
			.terminatedAt(endTime)
			.status(ArchiveStatus.SUCCESS)
			.build());
		expectLastCall();
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.TERMINATED));
		mocks.verify();
	}
	
	@Test
	public void eventShouldLogErrorWhenTaskStateIsCanceledAndNotFound() throws DaoException {
		expect(task.getArchiveTreatmentKind()).andReturn(ArchiveTreatmentKind.REAL_RUN);
		expect(dao.find(runId)).andReturn(Optional.<ArchiveTreatment>absent());
		
		logger.error("Only task with status {} can be created, received {}", State.WAITING, State.CANCELED);
		expectLastCall();
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.CANCELED));
		mocks.verify();
	}
	
	@Test
	public void eventShouldRemoveWhenTaskStateIsCanceled() throws Exception {
		expect(task.getArchiveTreatmentKind()).andReturn(ArchiveTreatmentKind.REAL_RUN);
		expect(dao.find(runId)).andReturn(Optional.<ArchiveTreatment>of(
				ArchiveRunningTreatment
				.forDomain(domain)
				.runId(runId)
				.recurrent(true)
				.higherBoundary(higherBoundary)
				.scheduledAt(scheduledTime)
				.startedAt(startTime)
				.build()));
		
		logger.info("A task has been canceled {}", runId);
		expectLastCall();
		
		dao.remove(runId);
		expectLastCall();
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.CANCELED));
		mocks.verify();
	}
	
	@Test
	public void eventShouldLogWhenInsertFails() throws Exception {
		expect(task.getArchiveTreatmentKind()).andReturn(ArchiveTreatmentKind.REAL_RUN);
		expect(dao.find(runId)).andReturn(Optional.<ArchiveTreatment>absent());
		logger.info("Insert a task as {} for domain {}, scheduled at {} with id {}", 
				ArchiveStatus.SCHEDULED, domain.get(), scheduledTime, runId);
		expectLastCall();

		IllegalStateException expectedException = new IllegalStateException("message");
		dao.insert(ArchiveScheduledTreatment
			.forDomain(domain)
			.runId(runId)
			.recurrent(true)
			.higherBoundary(higherBoundary)
			.scheduledAt(scheduledTime)
			.build());
		expectLastCall().andThrow(expectedException);
		
		logger.error("Cannot insert or update a treatment", expectedException);
		expectLastCall();
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.WAITING));
		mocks.verify();
	}
	
	@Test
	public void eventShouldLogWhenUpdateFails() throws Exception {
		expect(timeProvider.now()).andReturn(endTime);
		logger.info("Update a task as {} for domain {}, scheduled at {} with id {}", 
				State.TERMINATED, domain.get(), scheduledTime, runId);
		expectLastCall();
		expect(task.getArchiveTreatmentKind()).andReturn(ArchiveTreatmentKind.REAL_RUN);

		expect(dao.find(runId)).andReturn(Optional.<ArchiveTreatment>of(
				ArchiveRunningTreatment
				.forDomain(domain)
				.runId(runId)
				.recurrent(true)
				.higherBoundary(higherBoundary)
				.scheduledAt(scheduledTime)
				.startedAt(startTime)
				.build()));
		
		IllegalStateException expectedException = new IllegalStateException("message");
		dao.update(ArchiveTerminatedTreatment
			.forDomain(domain)
			.runId(runId)
			.recurrent(true)
			.higherBoundary(higherBoundary)
			.scheduledAt(scheduledTime)
			.startedAt(startTime)
			.terminatedAt(endTime)
			.status(ArchiveStatus.SUCCESS)
			.build());
		expectLastCall().andThrow(expectedException);

		logger.error("Cannot insert or update a treatment", expectedException);
		expectLastCall();
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.TERMINATED));
		mocks.verify();
	}
	
	@Test
	public void eventShouldLogWhenRemoveFails() throws Exception {
		expect(task.getArchiveTreatmentKind()).andReturn(ArchiveTreatmentKind.REAL_RUN);
		expect(dao.find(runId)).andReturn(Optional.<ArchiveTreatment>of(
				ArchiveRunningTreatment
				.forDomain(domain)
				.runId(runId)
				.recurrent(true)
				.higherBoundary(higherBoundary)
				.scheduledAt(scheduledTime)
				.startedAt(startTime)
				.build()));
		
		IllegalStateException expectedException = new IllegalStateException("message");
		dao.remove(runId);
		expectLastCall().andThrow(expectedException);

		logger.error("Cannot insert or update a treatment", expectedException);
		expectLastCall();
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.CANCELED));
		mocks.verify();
	}
	
	@Test
	public void eventShouldNotPersistWhenNotARealRunTreatment() {
		expect(task.getArchiveTreatmentKind()).andReturn(ArchiveTreatmentKind.DRY_RUN);
		
		mocks.replay();
		testee.onTreatmentStateChange(new TaskStatusChanged(task, State.CANCELED));
		mocks.verify();
	}
}