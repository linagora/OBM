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

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.io.IOException;
import java.util.List;

import org.easymock.IMocksControl;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obm.imap.archive.beans.ArchiveConfiguration;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.ConfigurationState;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationService;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationServiceImpl;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.dao.ProcessedFolderDao;
import org.obm.imap.archive.exception.TestingModeRequiredException;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.minig.imap.StoreClient;
import org.slf4j.LoggerFactory;

import pl.wkr.fluentrule.api.FluentExpectedException;
import ch.qos.logback.classic.Logger;

import com.google.common.collect.ImmutableList;
import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;


public class ResetImapArchiveProcessingTest {

	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
	@Rule public FluentExpectedException expectedException = FluentExpectedException.none();
	
	private IMocksControl control;
	private DateTimeProvider dateTimeProvider;
	private SchedulingDatesService schedulingDatesService;
	private StoreClientFactory storeClientFactory;
	private ArchiveTreatmentDao archiveTreatmentDao;
	private ProcessedFolderDao processedFolderDao;
	private ImapArchiveConfigurationService imapArchiveConfigurationService;
	private Logger logger;
	
	private ResetImapArchiveProcessing testee;

	@Before
	public void setup() throws IOException {
		control = createControl();
		dateTimeProvider = control.createMock(DateTimeProvider.class);
		schedulingDatesService = control.createMock(SchedulingDatesService.class);
		storeClientFactory = control.createMock(StoreClientFactory.class);
		archiveTreatmentDao = control.createMock(ArchiveTreatmentDao.class);
		processedFolderDao = control.createMock(ProcessedFolderDao.class);
		imapArchiveConfigurationService = control.createMock(ImapArchiveConfigurationService.class);
		expect(imapArchiveConfigurationService.getCyrusPartitionSuffix())
			.andReturn("archive").anyTimes();
		expect(imapArchiveConfigurationService.getProcessingBatchSize())
			.andReturn(20).anyTimes();
		expect(imapArchiveConfigurationService.getQuotaMaxSize())
			.andReturn(ImapArchiveConfigurationServiceImpl.DEFAULT_QUOTA_MAX_SIZE).anyTimes();
		logger = (Logger) LoggerFactory.getLogger(temporaryFolder.newFile().getAbsolutePath());
		
		testee = new ResetImapArchiveProcessing(dateTimeProvider, 
				schedulingDatesService, storeClientFactory, archiveTreatmentDao, processedFolderDao, imapArchiveConfigurationService, true);
	}

	@Test
	public void listImapFoldersShouldFilderArchiveFolders() throws Exception {
		String archiveMainFolder = "arChive";
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/usera/" + archiveMainFolder + "/Excluded@mydomain.org", true, false),
				new ListInfo("user/usera/" + archiveMainFolder + "/Excluded/subfolder@mydomain.org", true, false));
		ListResult listResult = new ListResult(6);
		listResult.addAll(expectedListInfos);
		listResult.add(new ListInfo("user/usera@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/Drafts@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/SPAM@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/Sent@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/Excluded@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/Excluded/subfolder@mydomain.org", true, false));
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll(ImapArchiveProcessing.USERS_REFERENCE_NAME, ImapArchiveProcessing.INBOX_MAILBOX_NAME))
			.andReturn(listResult);
		storeClient.close();
		expectLastCall();
		
		ObmDomain domain = ObmDomain.builder().name("mydomain.org").uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.daily())
						.time(LocalTime.parse("13:23"))
						.build())
				.archiveMainFolder(archiveMainFolder)
				.build();

		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);

		control.replay();
		ImmutableList<ListInfo> listImapFolders = testee.listImapFolders(domain, logger, domainConfiguration);
		control.verify();
		
		assertThat(listImapFolders).isEqualTo(expectedListInfos);
	}
	
	@Test(expected=TestingModeRequiredException.class)
	public void resetShouldThrowWhenNotInTestingMode() {
		ResetImapArchiveProcessing resetImapArchiveProcessing = new ResetImapArchiveProcessing(dateTimeProvider, 
				schedulingDatesService, storeClientFactory, archiveTreatmentDao, processedFolderDao, imapArchiveConfigurationService, false);
		
		
		ObmDomain domain = ObmDomain.builder().name("mydomain.org").uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.daily())
						.time(LocalTime.parse("13:23"))
						.build())
				.archiveMainFolder("arChive")
				.build();

		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7");
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, runId, logger, null, false);
		
		control.replay();
		resetImapArchiveProcessing.archive(archiveConfiguration);
		control.verify();
	}
	
	@Test
	public void resetShouldRunWhenTestingMode() throws Exception {
		ObmDomain domain = ObmDomain.builder().name("mydomain.org").uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build();
		archiveTreatmentDao.deleteAll(domain.getUuid());
		expectLastCall();
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll(ImapArchiveProcessing.USERS_REFERENCE_NAME, ImapArchiveProcessing.INBOX_MAILBOX_NAME))
			.andReturn(new ListResult(0));
		storeClient.close();
		expectLastCall();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);

		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.daily())
						.time(LocalTime.parse("13:23"))
						.build())
				.archiveMainFolder("arChive")
				.build();

		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7");
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, runId, logger, null, false);
		
		control.replay();
		testee.archive(archiveConfiguration);
		control.verify();
	}
	
	@Test
	public void resetShouldDeleteImapFolders() throws Exception {
		ObmDomain domain = ObmDomain.builder().name("mydomain.org").uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build();
		archiveTreatmentDao.deleteAll(domain.getUuid());
		expectLastCall();
		
		String archiveMainFolder = "arChive";
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall().times(3);
		ListResult listResult = new ListResult(2);
		listResult.add(new ListInfo("user/usera/" + archiveMainFolder + "/Excluded@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/" + archiveMainFolder + "/Excluded/subfolder@mydomain.org", true, false));
		expect(storeClient.listAll(ImapArchiveProcessing.USERS_REFERENCE_NAME, ImapArchiveProcessing.INBOX_MAILBOX_NAME))
			.andReturn(listResult);
		expect(storeClient.delete("user/usera/" + archiveMainFolder + "/Excluded@mydomain.org"))
			.andReturn(true);
		expect(storeClient.delete("user/usera/" + archiveMainFolder + "/Excluded/subfolder@mydomain.org"))
			.andReturn(true);
		storeClient.close();
		expectLastCall().times(3);
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient).times(3);

		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.daily())
						.time(LocalTime.parse("13:23"))
						.build())
				.archiveMainFolder(archiveMainFolder)
				.build();

		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7");
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, runId, logger, null, false);
		
		control.replay();
		testee.archive(archiveConfiguration);
		control.verify();
	}
	
	@Test(expected=RuntimeException.class)
	public void resetShouldStopWhenExceptionOccured() throws Exception {
		ObmDomain domain = ObmDomain.builder().name("mydomain.org").uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).build();
		archiveTreatmentDao.deleteAll(domain.getUuid());
		expectLastCall();
		
		String archiveMainFolder = "arChive";
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall().times(3);
		ListResult listResult = new ListResult(2);
		listResult.add(new ListInfo("user/usera/" + archiveMainFolder + "/Excluded@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/" + archiveMainFolder + "/Excluded/subfolder@mydomain.org", true, false));
		expect(storeClient.listAll(ImapArchiveProcessing.USERS_REFERENCE_NAME, ImapArchiveProcessing.INBOX_MAILBOX_NAME))
			.andReturn(listResult);
		expect(storeClient.delete("user/usera/" + archiveMainFolder + "/Excluded@mydomain.org"))
			.andReturn(false);
		storeClient.close();
		expectLastCall().times(3);
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient).times(3);

		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.daily())
						.time(LocalTime.parse("13:23"))
						.build())
				.archiveMainFolder(archiveMainFolder)
				.build();

		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7");
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, runId, logger, null, false);
		
		control.replay();
		testee.archive(archiveConfiguration);
		control.verify();
	}
}
