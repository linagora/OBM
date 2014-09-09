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
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.scheduling.ArchiveScheduler;
import org.obm.imap.archive.scheduling.ArchiveSchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class DomainConfigurationServiceTest {

	ObmDomain domain;
	Logger logger;
	IMocksControl control;
	DomainConfigurationDao domainConfigurationDao;
	ArchiveSchedulingService schedulingService;
	ArchiveScheduler scheduler;
	DomainConfigurationService testee;

	@Before
	public void setup() {
		domain = ObmDomain.builder().uuid(ObmDomainUuid.of("f1dabddf-7da2-412b-8159-71f3428e902f")).build();
		logger = LoggerFactory.getLogger(getClass());
		
		control = EasyMock.createControl();
		domainConfigurationDao = control.createMock(DomainConfigurationDao.class);
		scheduler = control.createMock(ArchiveScheduler.class);
		schedulingService = control.createMock(ArchiveSchedulingService.class);

		testee = new DomainConfigurationService(logger, domainConfigurationDao,schedulingService, scheduler);
	}

	@Test
	public void updateOrCreateShouldCreateAndScheduleWhenNewAndEnabled() throws Exception {
		DomainConfiguration config = configurationAsEnabled(true);

		expect(domainConfigurationDao.get(domain)).andReturn(null);
		domainConfigurationDao.create(config);
		expectLastCall();
		
		scheduler.clearDomain(domain.getUuid());
		expectLastCall();

		expect(schedulingService.scheduleAsRecurrent(config))
			.andReturn(ArchiveTreatmentRunId.from("5879b689-ffb5-422c-bf8d-eab80b2eddd6"));
		
		control.replay();
		testee.updateOrCreate(config);
		control.verify();
	}

	@Test
	public void updateOrCreateShouldCreateButNotScheduleWhenNewButDisabled() throws Exception {
		DomainConfiguration config = configurationAsEnabled(false);

		expect(domainConfigurationDao.get(domain)).andReturn(null);
		domainConfigurationDao.create(config);
		expectLastCall();
		
		scheduler.clearDomain(domain.getUuid());
		expectLastCall();

		control.replay();
		testee.updateOrCreate(config);
		control.verify();
	}

	@Test
	public void updateOrCreateShouldUpdateAndScheduleWhenNotNewAndEnabled() throws Exception {
		DomainConfiguration previousConfig = configurationAsEnabled(false);
		DomainConfiguration config = configurationAsEnabled(true);

		expect(domainConfigurationDao.get(domain)).andReturn(previousConfig);
		domainConfigurationDao.update(config);
		expectLastCall();
		
		scheduler.clearDomain(domain.getUuid());
		expectLastCall();

		expect(schedulingService.scheduleAsRecurrent(config))
			.andReturn(ArchiveTreatmentRunId.from("5879b689-ffb5-422c-bf8d-eab80b2eddd6"));
		
		control.replay();
		testee.updateOrCreate(config);
		control.verify();
	}

	@Test
	public void updateOrCreateShouldUpdateButNotScheduleWhenNotNewAndDisabled() throws Exception {
		DomainConfiguration previousConfig = configurationAsEnabled(true);
		DomainConfiguration config = configurationAsEnabled(false);

		expect(domainConfigurationDao.get(domain)).andReturn(previousConfig);
		domainConfigurationDao.update(config);
		expectLastCall();
		
		scheduler.clearDomain(domain.getUuid());
		expectLastCall();
		
		control.replay();
		testee.updateOrCreate(config);
		control.verify();
	}

	private DomainConfiguration configurationAsEnabled(boolean enabled) {
		return DomainConfiguration
				.builder()
				.domain(domain)
				.enabled(enabled)
				.schedulingConfiguration(
						SchedulingConfiguration.builder()
								.time(LocalTime.parse("22:15"))
								.recurrence(ArchiveRecurrence.daily()).build())
				.build();
	}
}
