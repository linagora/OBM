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
package org.obm.imap.archive.startup;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveRunningTreatment;
import org.obm.imap.archive.beans.ArchiveScheduledTreatment;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTerminatedTreatment;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.scheduling.ArchiveDomainTask;
import org.obm.imap.archive.scheduling.ArchiveScheduler;
import org.obm.imap.archive.scheduling.ArchiveDomainTaskFactory;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.linagora.scheduling.ScheduledTask;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class RestoreTasksOnStartupHandlerTest {

	Logger logger = LoggerFactory.getLogger(RestoreTasksOnStartupHandlerTest.class);
	
	ObmDomainUuid domainUuid;
	IMocksControl control;
	ArchiveScheduler scheduler;
	ArchiveTreatmentDao archiveTreatmentDao;
	ArchiveDomainTaskFactory taskFactory;
	RestoreTasksOnStartupHandler testee;
	
	@Before
	public void setUp() {
		domainUuid = ObmDomainUuid.of("b7e91835-68de-498f-bff8-97d43acf222c");
		control = createControl();
		archiveTreatmentDao = control.createMock(ArchiveTreatmentDao.class);
		scheduler = control.createMock(ArchiveScheduler.class);
		taskFactory = control.createMock(ArchiveDomainTaskFactory.class);
		testee = new RestoreTasksOnStartupHandler(logger, scheduler, archiveTreatmentDao, taskFactory);
	}

	@Test
	public void startingShouldDoNothingIfNoEntry() throws DaoException {
		expect(archiveTreatmentDao.findAllScheduledOrRunning())
			.andReturn(ImmutableList.<ArchiveTreatment>of());
		
		control.replay();
		testee.starting();
		control.verify();
	}
	
	@Test
	public void startingShouldReScheduleWhenScheduledEntry() throws DaoException {
		DateTime when = DateTime.parse("2014-12-2T11:35Z");
		DateTime higherBoundary = DateTime.parse("2014-12-1T01:01Z");
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("aee2d1ab-b237-4077-a61b-a85e3cb67742");
		
		expect(archiveTreatmentDao.findAllScheduledOrRunning()).andReturn(ImmutableList.<ArchiveTreatment>of(
			ArchiveScheduledTreatment
				.forDomain(domainUuid)
				.runId(runId)
				.recurrent(true)
				.higherBoundary(higherBoundary)
				.scheduledAt(when)
				.build()
			));
		
		ArchiveDomainTask archiveTask = control.createMock(ArchiveDomainTask.class);
		expect(taskFactory.createAsRecurrent(domainUuid, when, higherBoundary, runId)).andReturn(archiveTask);
		ScheduledTask<ArchiveDomainTask> task = control.createMock(ScheduledTask.class);
		expect(scheduler.schedule(archiveTask)).andReturn(task);
		
		control.replay();
		testee.starting();
		control.verify();
	}
	
	@Test
	public void startingShouldMoveAsErrorWhenRunningEntry() throws Exception {
		ArchiveRunningTreatment runningTreatment = ArchiveRunningTreatment
			.forDomain(domainUuid)
			.runId("aee2d1ab-b237-4077-a61b-a85e3cb67742")
			.recurrent(true)
			.higherBoundary(DateTime.parse("2014-12-1T01:01Z"))
			.scheduledAt(DateTime.parse("2014-12-2T11:35Z"))
			.startedAt(DateTime.parse("2014-12-3T01:01Z"))
			.build();
		
		expect(archiveTreatmentDao.findAllScheduledOrRunning())
			.andReturn(ImmutableList.<ArchiveTreatment>of(runningTreatment));
		
		archiveTreatmentDao.update(runningTreatment.asError(ArchiveTreatment.FAILED_AT_UNKOWN_DATE));
		
		control.replay();
		testee.starting();
		control.verify();
	}
	
	@Test
	public void startingShouldNotBeBrokenWhenOtherStatus() throws DaoException {
		expect(archiveTreatmentDao.findAllScheduledOrRunning()).andReturn(ImmutableList.<ArchiveTreatment>of(
			ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId("aee2d1ab-b237-4077-a61b-a85e3cb67742")
				.recurrent(true)
				.higherBoundary(DateTime.parse("2014-12-1T01:01Z"))
				.scheduledAt(DateTime.parse("2014-12-2T11:35Z"))
				.startedAt(DateTime.parse("2014-12-3T01:01Z"))
				.terminatedAt(DateTime.parse("2014-12-4T01:01Z"))
				.status(ArchiveStatus.SUCCESS)
				.build()
			));
		
		control.replay();
		testee.starting();
		control.verify();
	}
}
