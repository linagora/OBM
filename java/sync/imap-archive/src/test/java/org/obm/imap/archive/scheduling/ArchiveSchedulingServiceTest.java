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

import static org.easymock.EasyMock.expect;

import java.util.UUID;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.scheduling.ArchiveDomainTask.Factory;
import org.obm.imap.archive.services.SchedulingDatesService;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.push.utils.UUIDFactory;

import com.linagora.scheduling.ScheduledTask;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveSchedulingServiceTest {

	ObmDomainUuid domain;
	IMocksControl mocks;
	ArchiveScheduler scheduler;
	ArchiveDomainTask.Factory taskFactory;
	UUIDFactory uuidFactory;
	SchedulingDatesService schedulingDatesService;
	DomainConfigurationDao domainConfigDao;
	ArchiveSchedulingService testee;

	@Before
	public void setUp() {
		domain = ObmDomainUuid.of("f1dabddf-7da2-412b-8159-71f3428e902f");
		
		mocks = EasyMock.createControl();
		scheduler = mocks.createMock(ArchiveScheduler.class);
		taskFactory = mocks.createMock(Factory.class);
		uuidFactory = mocks.createMock(UUIDFactory.class);
		schedulingDatesService = mocks.createMock(SchedulingDatesService.class);
		domainConfigDao = mocks.createMock(DomainConfigurationDao.class);
		
		testee = new ArchiveSchedulingService(scheduler, taskFactory, uuidFactory, schedulingDatesService, domainConfigDao);
	}

	@Test
	public void scheduleShouldFindHigherBoundaryThenSchedule() throws Exception {
		testSchedule(
			DomainConfiguration.builder()
				.domainId(domain)
				.enabled(true)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build());
	}

	@Test
	public void scheduleShouldNotCheckEnabledStatus() throws Exception {
		testSchedule(
			DomainConfiguration.builder()
				.domainId(domain)
				.enabled(false)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build());
	}

	private void testSchedule(DomainConfiguration config) throws DaoException {
		DateTime when = DateTime.parse("2024-01-1T05:04Z");
		DateTime higherBoundary = DateTime.parse("2024-02-1T05:04Z");
		UUID runUuid = UUID.fromString("ecd08c0d-70aa-4a04-8a18-57fe7afe1404");
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from(runUuid);
		ArchiveDomainTask task = mocks.createMock(ArchiveDomainTask.class);
		ScheduledTask<ArchiveDomainTask> scheduled = mocks.createMock(ScheduledTask.class);

		expect(domainConfigDao.get(domain)).andReturn(config);
		expect(schedulingDatesService.higherBoundary(when, config.getRepeatKind())).andReturn(higherBoundary);
		expect(uuidFactory.randomUUID()).andReturn(runUuid);
		expect(taskFactory.create(domain, when, higherBoundary, runId)).andReturn(task);
		expect(scheduler.schedule(task)).andReturn(scheduled);
		
		mocks.replay();
		testee.schedule(domain, when);
		mocks.verify();
	}

	@Test
	public void scheduleByConfigShouldGetDatesThenSchedule() {
		testScheduleByConfig(
			DomainConfiguration.builder()
				.domainId(domain)
				.enabled(true)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build());
	}

	@Test
	public void scheduleByConfigShouldNotCheckEnabledStatus() {
		testScheduleByConfig(
			DomainConfiguration.builder()
				.domainId(domain)
				.enabled(false)
				.schedulingConfiguration(
					SchedulingConfiguration.builder()
						.time(LocalTime.parse("22:15"))
						.recurrence(ArchiveRecurrence.daily()).build())
				.build());
	}

	private void testScheduleByConfig(DomainConfiguration config) {
		DateTime when = DateTime.parse("2024-01-1T05:04Z");
		DateTime higherBoundary = DateTime.parse("2024-02-1T05:04Z");
		UUID runUuid = UUID.fromString("ecd08c0d-70aa-4a04-8a18-57fe7afe1404");
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from(runUuid);
		ArchiveDomainTask task = mocks.createMock(ArchiveDomainTask.class);
		ScheduledTask<ArchiveDomainTask> scheduled = mocks.createMock(ScheduledTask.class);
		
		expect(schedulingDatesService.nextTreatmentDate(config.getSchedulingConfiguration())).andReturn(when);
		expect(schedulingDatesService.higherBoundary(when, config.getRepeatKind())).andReturn(higherBoundary);
		expect(uuidFactory.randomUUID()).andReturn(runUuid);
		expect(taskFactory.create(domain, when, higherBoundary, runId)).andReturn(task);
		expect(scheduler.schedule(task)).andReturn(scheduled);
		
		mocks.replay();
		testee.schedule(config);
		mocks.verify();
	}
}
