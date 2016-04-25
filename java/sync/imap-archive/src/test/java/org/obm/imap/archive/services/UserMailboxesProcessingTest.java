/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2016  Linagora
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.io.IOException;
import java.util.List;

import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obm.imap.archive.beans.ArchiveConfiguration;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.ConfigurationState;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.HigherBoundary;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.beans.ScopeUser;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationService;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationServiceImpl;
import org.obm.imap.archive.dao.ProcessedFolderDao;
import org.obm.imap.archive.mailbox.TemporaryMailbox;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.minig.imap.StoreClient;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.linagora.scheduling.DateTimeProvider;

import ch.qos.logback.classic.Logger;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.user.UserExtId;
import pl.wkr.fluentrule.api.FluentExpectedException;

public class UserMailboxesProcessingTest {

	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
	@Rule public FluentExpectedException expectedException = FluentExpectedException.none();
	
	private IMocksControl control;
	private StoreClientFactory storeClientFactory;
	private Logger logger;
	
	private UserMailboxesProcessing sut;

	@Before
	public void setup() throws IOException {
		control = createControl();
		DateTimeProvider dateTimeProvider = control.createMock(DateTimeProvider.class);
		storeClientFactory = control.createMock(StoreClientFactory.class);
		ProcessedFolderDao processedFolderDao = control.createMock(ProcessedFolderDao.class);
		ImapArchiveConfigurationService imapArchiveConfigurationService = control.createMock(ImapArchiveConfigurationService.class);
		expect(imapArchiveConfigurationService.getCyrusPartitionSuffix())
			.andReturn("archive").anyTimes();
		expect(imapArchiveConfigurationService.getProcessingBatchSize())
			.andReturn(20).anyTimes();
		expect(imapArchiveConfigurationService.getQuotaMaxSize())
			.andReturn(ImapArchiveConfigurationServiceImpl.DEFAULT_QUOTA_MAX_SIZE).anyTimes();
		logger = (Logger) LoggerFactory.getLogger(temporaryFolder.newFile().getAbsolutePath());

		MailboxProcessing mailboxProcessing = new MailboxProcessing(dateTimeProvider, processedFolderDao, imapArchiveConfigurationService);
		sut = new UserMailboxesProcessing(storeClientFactory, mailboxProcessing);
	}

	
	@Test
	public void listImapFoldersShouldAppendDomainWhenMailboxesDoesntContainDomain() throws Exception {
		List<ListInfo> givenListInfos = ImmutableList.of(
				new ListInfo("user/usera/Drafts", true, false),
				new ListInfo("user/usera/SPAM", true, false),
				new ListInfo("user/usera/Sent", true, false),
				new ListInfo("user/usera/Excluded", true, false),
				new ListInfo("user/usera/Excluded/subfolder", true, false));
		ListResult listResult = new ListResult(6);
		listResult.addAll(givenListInfos);
		ListResult inboxListResult = new ListResult();
		inboxListResult.add(new ListInfo("user/usera", true, false));
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expectListImapFolders(storeClient, "usera", listResult);
		storeClient.close();
		expectLastCall();
		
		ObmDomain domain = ObmDomain.builder().name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
				.archiveMainFolder("arChive")
				.build();
		
		expect(storeClientFactory.createOnUserBackend("usera", domain))
			.andReturn(storeClient);
		
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"), logger, null, false);
		
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/usera@mydomain.org", true, false),
				new ListInfo("user/usera/Drafts@mydomain.org", true, false),
				new ListInfo("user/usera/SPAM@mydomain.org", true, false),
				new ListInfo("user/usera/Sent@mydomain.org", true, false),
				new ListInfo("user/usera/Excluded@mydomain.org", true, false),
				new ListInfo("user/usera/Excluded/subfolder@mydomain.org", true, false));
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.higherBoundary(HigherBoundary.builder()
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = sut.listImapFolders(inboxListResult.get(0), "usera", processedTask);
		control.verify();
		assertThat(listImapFolders).isEqualTo(expectedListInfos);
	}
	
	@Test
	public void listImapFoldersShouldListAllWhenNoExcludedFolder() throws Exception {
		ListResult inboxListResult = getUserMailboxList("usera", "");
		ListResult listResult = getUserMailboxList("usera", "/Drafts", "/SPAM", "/Sent", "/Excluded", "/Excluded/subfolder");
		StoreClient storeClient = control.createMock(StoreClient.class);

		storeClient.login(false);
		expectLastCall();
		expectListImapFolders(storeClient, "usera", listResult);
		storeClient.close();
		expectLastCall();
		
		ObmDomain domain = ObmDomain.builder().name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
				.archiveMainFolder("arChive")
				.build();
		
		expect(storeClientFactory.createOnUserBackend("usera", domain))
			.andReturn(storeClient);
		
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"), logger, null, false);
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.higherBoundary(HigherBoundary.builder()
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = sut.listImapFolders(inboxListResult.get(0), "usera", processedTask);
		control.verify();
		assertThat(listImapFolders).isEqualTo(getUserMailboxList("usera", "", "/Drafts", "/SPAM", "/Sent", "/Excluded", "/Excluded/subfolder"));
	}
	
	@Test
	public void listImapFoldersShouldFilterWhenExcludedFolder() throws Exception {
		ListResult inboxListResult = getUserMailboxList("usera", "");
		ListResult listResult = getUserMailboxList("usera", "/Drafts", "/SPAM", "/Sent", "/Excluded", "/Excluded/subfolder");
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		storeClient.login(false);
		expectLastCall();
		expectListImapFolders(storeClient, "usera", listResult);
		storeClient.close();
		expectLastCall();

		ObmDomain domain = ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
			.domain(domain)
			.state(ConfigurationState.ENABLE)
			.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
			.archiveMainFolder("arChive")
			.excludedFolder("Excluded")
			.build();
		
		expect(storeClientFactory.createOnUserBackend("usera", domain))
			.andReturn(storeClient);
		
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"), logger, null, false);
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.higherBoundary(HigherBoundary.builder()
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = sut.listImapFolders(inboxListResult.get(0), "usera", processedTask);
		control.verify();
		assertThat(listImapFolders).isEqualTo(getUserMailboxList("usera", "", "/Drafts", "/SPAM", "/Sent"));
	}
	
	@Test
	public void listUsersShouldFilterWhenScopeUsersAndScopeExcludes() throws Exception {
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/usera@mydomain.org", true, false),
				new ListInfo("user/usera/Drafts@mydomain.org", true, false),
				new ListInfo("user/usera/SPAM@mydomain.org", true, false),
				new ListInfo("user/usera/Sent@mydomain.org", true, false),
				new ListInfo("user/userc@mydomain.org", true, false),
				new ListInfo("user/userc/Drafts@mydomain.org", true, false),
				new ListInfo("user/userc/SPAM@mydomain.org", true, false),
				new ListInfo("user/userc/Sent@mydomain.org", true, false));
		ListResult listResult = new ListResult(12);
		listResult.addAll(expectedListInfos);
		listResult.add(new ListInfo("user/userb@mydomain.org", true, false));
		listResult.add(new ListInfo("user/userb/Drafts@mydomain.org", true, false));
		listResult.add(new ListInfo("user/userb/SPAM@mydomain.org", true, false));
		listResult.add(new ListInfo("user/userb/Sent@mydomain.org", true, false));
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll(UserMailboxesProcessing.USERS_REFERENCE_NAME, UserMailboxesProcessing.INBOX_MAILBOX_NAME))
			.andReturn(listResult);
		storeClient.close();
		expectLastCall();
		
		ObmDomain domain = ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
			.domain(domain)
			.state(ConfigurationState.ENABLE)
			.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
			.archiveMainFolder("arChive")
			.scopeUsers(ImmutableList.of(ScopeUser.builder()
					.id(UserExtId.valueOf("3fb10c50-52fa-4a48-9554-2ae8c9c734b9"))
					.login("userb")
					.build()))
			.build();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);
		
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"), logger, null, false);
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.higherBoundary(HigherBoundary.builder()
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = sut.listUsers(processedTask);
		control.verify();
		assertThat(listImapFolders).isEqualTo(expectedListInfos);
	}
	
	@Test
	public void listUsersShouldNotFilterWhenScopeUsersAndScopeIncludes() throws Exception {
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/userb@mydomain.org", true, false),
				new ListInfo("user/userb/Drafts@mydomain.org", true, false),
				new ListInfo("user/userb/SPAM@mydomain.org", true, false),
				new ListInfo("user/userb/Sent@mydomain.org", true, false));
		ListResult listResult = new ListResult(12);
		listResult.addAll(expectedListInfos);
		listResult.add(new ListInfo("user/usera@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/Drafts@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/SPAM@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/Sent@mydomain.org", true, false));
		listResult.add(new ListInfo("user/userc@mydomain.org", true, false));
		listResult.add(new ListInfo("user/userc/Drafts@mydomain.org", true, false));
		listResult.add(new ListInfo("user/userc/SPAM@mydomain.org", true, false));
		listResult.add(new ListInfo("user/userc/Sent@mydomain.org", true, false));
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll(UserMailboxesProcessing.USERS_REFERENCE_NAME, UserMailboxesProcessing.INBOX_MAILBOX_NAME))
			.andReturn(listResult);
		storeClient.close();
		expectLastCall();
		
		ObmDomain domain = ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
			.domain(domain)
			.state(ConfigurationState.ENABLE)
			.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
			.archiveMainFolder("arChive")
			.scopeUsersIncludes(true)
			.scopeUsers(ImmutableList.of(ScopeUser.builder()
					.id(UserExtId.valueOf("3fb10c50-52fa-4a48-9554-2ae8c9c734b9"))
					.login("userb")
					.build()))
			.build();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);
		
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"), logger, null, false);
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.higherBoundary(HigherBoundary.builder()
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = sut.listUsers(processedTask);
		control.verify();
		assertThat(listImapFolders).isEqualTo(expectedListInfos);
	}
	
	@Test
	public void listUsersShouldNotFilterWhenPathDoesntStartWithScopeUser() throws Exception {
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/user/usera@mydomain.org", true, false));
		ListResult listResult = new ListResult(1);
		listResult.addAll(expectedListInfos);
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll(UserMailboxesProcessing.USERS_REFERENCE_NAME, UserMailboxesProcessing.INBOX_MAILBOX_NAME))
			.andReturn(listResult);
		storeClient.close();
		expectLastCall();
		
		ObmDomain domain = ObmDomain.builder().uuid(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888")).name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
			.domain(domain)
			.state(ConfigurationState.ENABLE)
			.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
			.archiveMainFolder("arChive")
			.scopeUsers(ImmutableList.of(ScopeUser.builder()
					.id(UserExtId.valueOf("3fb10c50-52fa-4a48-9554-2ae8c9c734b9"))
					.login("usera")
					.build()))
			.build();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);
		
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"), logger, null, false);
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.higherBoundary(HigherBoundary.builder()
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = sut.listUsers(processedTask);
		control.verify();
		assertThat(listImapFolders).isEqualTo(expectedListInfos);
	}
	
	@Test
	public void listImapFoldersShouldFilterArchiveFolder() throws Exception {
		String archiveMainFolder = "arChive";
		ListResult inboxListResult = getUserMailboxList("usera", "");
		ListResult listResult = getUserMailboxList("usera", "/Drafts", "/SPAM", "/Sent", "/Excluded", "/Excluded/subfolder",
				"/" + archiveMainFolder + "/Excluded", "/" + archiveMainFolder + "/Excluded/subfolder");
		StoreClient storeClient = control.createMock(StoreClient.class);

		storeClient.login(false);
		expectLastCall();
		expectListImapFolders(storeClient, "usera", listResult);
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
		
		expect(storeClientFactory.createOnUserBackend("usera", domain))
			.andReturn(storeClient);

		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7");
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, runId, logger, null, false);
		
		control.replay();
		
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.higherBoundary(HigherBoundary.builder()
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = sut.listImapFolders(inboxListResult.get(0), "usera", processedTask);
		control.verify();
		assertThat(listImapFolders).isEqualTo(getUserMailboxList("usera", "", "/Drafts", "/SPAM", "/Sent", "/Excluded", "/Excluded/subfolder"));
	}
	
	@Test
	public void listImapFoldersShouldFilterTemporaryFolder() throws Exception {
		ListResult inboxListResult = getUserMailboxList("usera", "");
		ListResult listResult = getUserMailboxList("usera", "/Drafts", "/SPAM", "/Sent", "/Excluded", "/Excluded/subfolder",
				"/" + TemporaryMailbox.TEMPORARY_FOLDER + "/Excluded", "/" + TemporaryMailbox.TEMPORARY_FOLDER + "/Excluded/subfolder");
		StoreClient storeClient = control.createMock(StoreClient.class);

		storeClient.login(false);
		expectLastCall();
		expectListImapFolders(storeClient, "usera", listResult);
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
				.archiveMainFolder("arChive")
				.build();
		
		expect(storeClientFactory.createOnUserBackend("usera", domain))
			.andReturn(storeClient);

		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7");
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, runId, logger, null, false);
		
		control.replay();
		
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.higherBoundary(HigherBoundary.builder()
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = sut.listImapFolders(inboxListResult.get(0), "usera", processedTask);
		control.verify();
		assertThat(listImapFolders).isEqualTo(getUserMailboxList("usera", "", "/Drafts", "/SPAM", "/Sent", "/Excluded", "/Excluded/subfolder"));
	}
	
	@Test
	public void listImapFoldersShouldNotFailWhenMailboxFormatExceptionIsThrown() throws Exception {
		ListResult inboxListResult = getUserMailboxList("usera", "");
		ListResult listResult = getUserMailboxList("usera", "/Drafts", "/SPAM", "/Sent", "/Excluded");
		StoreClient storeClient = control.createMock(StoreClient.class);

		listResult.add(new ListInfo("bad", true, false));
		storeClient.login(false);
		expectLastCall();
		expectListImapFolders(storeClient, "usera", listResult);
		storeClient.close();
		expectLastCall();
		
		ObmDomain domain = ObmDomain.builder().name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
				.archiveMainFolder("arChive")
				.build();
		
		expect(storeClientFactory.createOnUserBackend("usera", domain))
			.andReturn(storeClient);
		
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"), logger, null, false);
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.higherBoundary(HigherBoundary.builder()
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = sut.listImapFolders(inboxListResult.get(0), "usera", processedTask);
		control.verify();
		assertThat(listImapFolders).isEqualTo(getUserMailboxList("usera", "", "/Drafts", "/SPAM", "/Sent", "/Excluded"));
	}
	
	@Test
	public void listUsersShouldAppendDomainWhenMailboxesDoesntContainDomain() throws Exception {
		List<ListInfo> givenListInfos = ImmutableList.of(
				new ListInfo("user/usera", true, false),
				new ListInfo("user/usera/Drafts", true, false),
				new ListInfo("user/usera/SPAM", true, false),
				new ListInfo("user/usera/Sent", true, false),
				new ListInfo("user/usera/Excluded", true, false));
		ListResult listResult = new ListResult(6);
		listResult.addAll(givenListInfos);
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll(UserMailboxesProcessing.USERS_REFERENCE_NAME, UserMailboxesProcessing.INBOX_MAILBOX_NAME))
			.andReturn(listResult);
		storeClient.close();
		expectLastCall();
		
		ObmDomain domain = ObmDomain.builder().name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
				.archiveMainFolder("arChive")
				.build();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);
		
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"), logger, null, false);
		
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/usera@mydomain.org", true, false),
				new ListInfo("user/usera/Drafts@mydomain.org", true, false),
				new ListInfo("user/usera/SPAM@mydomain.org", true, false),
				new ListInfo("user/usera/Sent@mydomain.org", true, false),
				new ListInfo("user/usera/Excluded@mydomain.org", true, false));
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.higherBoundary(HigherBoundary.builder()
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = sut.listUsers(processedTask);
		control.verify();
		assertThat(listImapFolders).isEqualTo(expectedListInfos);
	}
	
	@Test
	public void listUsersShouldFilterDomain() throws Exception {
		List<ListInfo> givenListInfos = ImmutableList.of(
				new ListInfo("user/usera@mydomain.org", true, false),
				new ListInfo("user/usera/Drafts@mydomain.org", true, false),
				new ListInfo("user/usera/SPAM@mydomain.org", true, false),
				new ListInfo("user/usera/Sent@otherdomain.org", true, false),
				new ListInfo("user/usera/Excluded@otherdomain.org", true, false));
		ListResult listResult = new ListResult(6);
		listResult.addAll(givenListInfos);
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll(UserMailboxesProcessing.USERS_REFERENCE_NAME, UserMailboxesProcessing.INBOX_MAILBOX_NAME))
			.andReturn(listResult);
		storeClient.close();
		expectLastCall();
		
		ObmDomain domain = ObmDomain.builder().name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
					.recurrence(ArchiveRecurrence.daily())
					.time(LocalTime.parse("13:23"))
					.build())
				.archiveMainFolder("arChive")
				.build();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);
		
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"), logger, null, false);
		
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/usera@mydomain.org", true, false),
				new ListInfo("user/usera/Drafts@mydomain.org", true, false),
				new ListInfo("user/usera/SPAM@mydomain.org", true, false));
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.higherBoundary(HigherBoundary.builder()
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = sut.listUsers(processedTask);
		control.verify();
		assertThat(listImapFolders).isEqualTo(expectedListInfos);
	}

	private void expectListImapFolders(StoreClient storeClient, String user, ListResult subFolderslistResult) {
		expect(storeClient.listAll(UserMailboxesProcessing.USERS_REFERENCE_NAME + "/" + user + "/", UserMailboxesProcessing.ALL_MAILBOXES_NAME))
			.andReturn(subFolderslistResult);
	}

	private ListResult getUserMailboxList(String user, String... mailboxes) {
		ListResult listResult = new ListResult();

		for (String mailbox : mailboxes) {
			listResult.add(new ListInfo("user/" + user + mailbox + "@mydomain.org", true, false));
		}

		return listResult;
	}
}
