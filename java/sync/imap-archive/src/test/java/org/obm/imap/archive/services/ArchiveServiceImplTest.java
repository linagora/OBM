/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.exception.DomainConfigurationException;

import pl.wkr.fluentrule.api.FluentExpectedException;

import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomainUuid;


public class ArchiveServiceImplTest {

	private IMocksControl control;
	
	private DomainConfigurationDao domainConfigurationDao;
	private DateTimeProvider dateTimeProvider;
	private LogFileService logFileService;
	
	private ArchiveServiceImpl archiveService;

	@Rule
	public FluentExpectedException expectedException = FluentExpectedException.none();

	
	@Before
	public void setup() {
		control = createControl();
		domainConfigurationDao = control.createMock(DomainConfigurationDao.class);
		dateTimeProvider = control.createMock(DateTimeProvider.class);
		logFileService = control.createMock(LogFileService.class);
		
		archiveService = new ArchiveServiceImpl(domainConfigurationDao, dateTimeProvider, logFileService, Boolean.FALSE);
	}
	
	@Test
	public void archiveShouldThrowWhenNoDomainFound() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		expect(domainConfigurationDao.get(domainId))
			.andReturn(null);
		
		expectedException
			.expect(DomainConfigurationException.class)
			.hasMessage("The IMAP Archive configuration is not defined for the domain: 'fc2f915e-9df4-4560-b141-7b4c7ddecdd6'");
		
		control.replay();
		archiveService.archive(domainId, ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77"), null);
		control.verify();
	}
	
	@Test
	public void archiveShouldThrowWhenConfigurationDisable() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		expect(domainConfigurationDao.get(domainId))
			.andReturn(DomainConfiguration.builder()
					.domainId(domainId)
					.enabled(false)
					.build());
		
		expectedException
			.expect(DomainConfigurationException.class)
			.hasMessage("The IMAP Archive service is disable for the domain: 'fc2f915e-9df4-4560-b141-7b4c7ddecdd6'");
		
		control.replay();
		archiveService.archive(domainId, ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77"), null);
		control.verify();
	}
	
	@Test
	public void archiveShouldReturnSuccessAndWrite2TimesWhenConfigurationEnableAndWaitingFor2Seconds() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		expect(domainConfigurationDao.get(domainId))
			.andReturn(DomainConfiguration.builder()
					.domainId(domainId)
					.enabled(true)
					.schedulingConfiguration(SchedulingConfiguration.builder()
							.time(LocalTime.parse("22:15"))
							.recurrence(ArchiveRecurrence.daily())
							.build())
					.build());
		
		expect(dateTimeProvider.now()).andReturn(DateTime.now(DateTimeZone.UTC));
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77");
		
		control.replay();
		archiveService.archive(domainId, runId, null);
		int twoSeconds = 2000;
		Thread.sleep(twoSeconds);
		control.verify();
	}
}
