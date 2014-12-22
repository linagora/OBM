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

package org.obm.imap.archive.services;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;
import org.obm.domain.dao.UserDao;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.ConfigurationState;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.ExcludedUser;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.exception.LoginMismatchException;
import org.obm.imap.archive.scheduling.ArchiveScheduler;
import org.obm.imap.archive.scheduling.ArchiveSchedulingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserExtId;

public class DomainConfigurationServiceTest {

	ObmDomain domain;
	Logger logger;
	IMocksControl control;
	DomainConfigurationDao domainConfigurationDao;
	UserDao userDao;
	ArchiveSchedulingService schedulingService;
	ArchiveScheduler scheduler;
	DomainConfigurationService testee;

	@Before
	public void setup() {
		domain = ObmDomain.builder().uuid(ObmDomainUuid.of("f1dabddf-7da2-412b-8159-71f3428e902f")).build();
		logger = LoggerFactory.getLogger(getClass());
		
		control = EasyMock.createControl();
		domainConfigurationDao = control.createMock(DomainConfigurationDao.class);
		userDao = control.createMock(UserDao.class);
		scheduler = control.createMock(ArchiveScheduler.class);
		schedulingService = control.createMock(ArchiveSchedulingService.class);

		testee = new DomainConfigurationService(logger, domainConfigurationDao, userDao, schedulingService, scheduler);
	}

	@Test
	public void updateOrCreateShouldCreateAndScheduleWhenNewAndEnabled() throws Exception {
		DomainConfiguration config = configuration(ConfigurationState.ENABLE);

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
		DomainConfiguration config = configuration(ConfigurationState.DISABLE);

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
		DomainConfiguration previousConfig = configuration(ConfigurationState.DISABLE);
		DomainConfiguration config = configuration(ConfigurationState.ENABLE);

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
		DomainConfiguration previousConfig = configuration(ConfigurationState.ENABLE);
		DomainConfiguration config = configuration(ConfigurationState.DISABLE);

		expect(domainConfigurationDao.get(domain)).andReturn(previousConfig);
		domainConfigurationDao.update(config);
		expectLastCall();
		
		scheduler.clearDomain(domain.getUuid());
		expectLastCall();
		
		control.replay();
		testee.updateOrCreate(config);
		control.verify();
	}

	@Test(expected=LoginMismatchException.class)
	public void updateOrCreateShouldThrowWhenBadLogin() throws Exception {
		ObmUser obmUser = control.createMock(ObmUser.class);
		UserExtId userId = UserExtId.valueOf("19683370-856f-44f5-8ca1-74284445ad17");
		expect(obmUser.getLogin())
			.andReturn("login");
		
		expect(userDao.getByExtId(userId, domain))
			.andReturn(obmUser);
		
		DomainConfiguration domainConfiguration = DomainConfiguration
				.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(
						SchedulingConfiguration.builder()
								.time(LocalTime.parse("22:15"))
								.recurrence(ArchiveRecurrence.daily()).build())
				.archiveMainFolder("arChive")
				.excludedUsers(ImmutableList.of(ExcludedUser.builder()
						.id(userId)
						.login("badlogin")
						.build()))
				.build();
		
		try {
			control.replay();
			testee.updateOrCreate(domainConfiguration);
		} finally {
			control.verify();
		}
	}
	
	private DomainConfiguration configuration(ConfigurationState state) {
		return DomainConfiguration
				.builder()
				.domain(domain)
				.state(state)
				.schedulingConfiguration(
						SchedulingConfiguration.builder()
								.time(LocalTime.parse("22:15"))
								.recurrence(ArchiveRecurrence.daily()).build())
				.archiveMainFolder("arChive")
				.build();
	}
}
