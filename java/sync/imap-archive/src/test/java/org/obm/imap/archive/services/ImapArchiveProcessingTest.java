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
import static org.assertj.guava.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obm.imap.archive.beans.ArchiveConfiguration;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTerminatedTreatment;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.Boundaries;
import org.obm.imap.archive.beans.ConfigurationState;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.ExcludedUser;
import org.obm.imap.archive.beans.ImapFolder;
import org.obm.imap.archive.beans.Limit;
import org.obm.imap.archive.beans.ProcessedFolder;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationService;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationServiceImpl;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.dao.ProcessedFolderDao;
import org.obm.imap.archive.exception.ImapArchiveProcessingException;
import org.obm.imap.archive.logging.LoggerAppenders;
import org.obm.imap.archive.mailbox.MailboxImpl;
import org.obm.imap.archive.services.ImapArchiveProcessing.ProcessedTask;
import org.obm.push.exception.ImapTimeoutException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.minig.imap.StoreClient;
import org.slf4j.LoggerFactory;

import pl.wkr.fluentrule.api.FluentExpectedException;
import ch.qos.logback.classic.Logger;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.system.ObmSystemUser;
import fr.aliacom.obm.common.user.UserExtId;

public class ImapArchiveProcessingTest {

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
	private LoggerAppenders loggerAppenders;
	
	private ImapArchiveProcessing imapArchiveProcessing;

	@Before
	public void setup() throws IOException {
		control = createControl();
		dateTimeProvider = control.createMock(DateTimeProvider.class);
		schedulingDatesService = control.createMock(SchedulingDatesService.class);
		storeClientFactory = control.createMock(StoreClientFactory.class);
		archiveTreatmentDao = control.createMock(ArchiveTreatmentDao.class);
		processedFolderDao = control.createMock(ProcessedFolderDao.class);
		imapArchiveConfigurationService = control.createMock(ImapArchiveConfigurationService.class);
		expect(imapArchiveConfigurationService.getArchiveMainFolder())
			.andReturn("ARCHIVE").anyTimes();
		expect(imapArchiveConfigurationService.getCyrusPartitionSuffix())
			.andReturn("archive").anyTimes();
		expect(imapArchiveConfigurationService.getProcessingBatchSize())
			.andReturn(20).anyTimes();
		logger = (Logger) LoggerFactory.getLogger(temporaryFolder.newFile().getAbsolutePath());
		loggerAppenders = control.createMock(LoggerAppenders.class);
		
		imapArchiveProcessing = new ImapArchiveProcessing(dateTimeProvider, 
				schedulingDatesService, storeClientFactory, archiveTreatmentDao, processedFolderDao, imapArchiveConfigurationService);
	}
	
	@Test
	public void archiveShouldWork() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		ObmDomain domain = ObmDomain.builder().uuid(domainId).name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.daily())
						.time(LocalTime.parse("13:23"))
						.build())
				.build();
		expect(archiveTreatmentDao.findLastTerminated(domainId, Limit.from(1)))
			.andReturn(ImmutableList.<ArchiveTreatment> of());
		
		DateTime treatmentDate = DateTime.parse("2014-08-27T12:18:00.000Z");
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate).times(4);
		
		DateTime lowerBoundary = new DateTime(0, DateTimeZone.UTC);
		DateTime higherBoundary = DateTime.parse("2014-08-26T12:18:00.000Z");
		expect(schedulingDatesService.higherBoundary(treatmentDate, RepeatKind.DAILY))
			.andReturn(higherBoundary);
		
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/usera@mydomain.org", true, false),
				new ListInfo("user/usera/Drafts@mydomain.org", true, false),
				new ListInfo("user/usera/SPAM@mydomain.org", true, false));
		ListResult listResult = new ListResult(3);
		listResult.addAll(expectedListInfos);
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll())
			.andReturn(listResult);
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77");
		expectImapCommandsOnMailboxProcessing("user/usera@mydomain.org", "user/usera/ARCHIVE/2014/INBOX@mydomain.org", "user/usera/TEMPORARY_ARCHIVE_FOLDER/INBOX@mydomain.org", 
				ImmutableSet.of(Range.closed(1l, 10l)), lowerBoundary, higherBoundary, treatmentDate, runId, storeClient);
		expectImapCommandsOnMailboxProcessing("user/usera/Drafts@mydomain.org", "user/usera/ARCHIVE/2014/Drafts@mydomain.org", "user/usera/TEMPORARY_ARCHIVE_FOLDER/Drafts@mydomain.org", 
				ImmutableSet.of(Range.closed(3l, 22l), Range.closed(23l, 42l), Range.closed(43l, 62l), Range.closed(63l, 82l), Range.closed(83l, 100l)), 
				lowerBoundary, higherBoundary, treatmentDate, runId, storeClient);
		expectImapCommandsOnMailboxProcessing("user/usera/SPAM@mydomain.org", "user/usera/ARCHIVE/2014/SPAM@mydomain.org", "user/usera/TEMPORARY_ARCHIVE_FOLDER/SPAM@mydomain.org", 
				ImmutableSet.of(Range.singleton(1230l)), lowerBoundary, higherBoundary, treatmentDate, runId, storeClient);
		
		storeClient.close();
		expectLastCall();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient).times(4);
		
		control.replay();
		imapArchiveProcessing.archive(new ArchiveConfiguration(domainConfiguration, null, null, runId, logger, loggerAppenders, false));
		control.verify();
	}
	
	@Test
	public void lastUidShouldBeDefaultWhenNoPreviousTreatmentAndNoMails() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		ObmDomain domain = ObmDomain.builder().uuid(domainId).name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.daily())
						.time(LocalTime.parse("13:23"))
						.build())
				.build();
		expect(archiveTreatmentDao.findLastTerminated(domainId, Limit.from(1)))
			.andReturn(ImmutableList.<ArchiveTreatment> of());
		
		DateTime treatmentDate = DateTime.parse("2014-08-27T12:18:00.000Z");
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate).times(2);
		
		DateTime higherBoundary = DateTime.parse("2014-08-26T12:18:00.000Z");
		expect(schedulingDatesService.higherBoundary(treatmentDate, RepeatKind.DAILY))
			.andReturn(higherBoundary);
		
		List<ListInfo> expectedListInfos = ImmutableList.of(new ListInfo("user/usera@mydomain.org", true, false));
		ListResult listResult = new ListResult(1);
		listResult.addAll(expectedListInfos);
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll())
			.andReturn(listResult);
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77");
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.select("user/usera@mydomain.org")).andReturn(true);
		expect(storeClient.uidSearch(SearchQuery.builder()
				.after(new DateTime(0, DateTimeZone.UTC).toDate())
				.before(higherBoundary.toDate())
				.build()))
			.andReturn(MessageSet.empty());
		
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate);
		processedFolderDao.insert(ProcessedFolder.builder()
				.runId(runId)
				.folder(ImapFolder.from("user/usera@mydomain.org"))
				.lastUid(ImapArchiveProcessing.DEFAULT_LAST_UID)
				.start(treatmentDate)
				.end(treatmentDate)
				.status(ArchiveStatus.SUCCESS)
				.build());
		expectLastCall();
		
		storeClient.close();
		expectLastCall().times(2);
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient).times(2);
		
		control.replay();
		imapArchiveProcessing.archive(new ArchiveConfiguration(domainConfiguration, null, null, runId, logger, loggerAppenders, false));
		control.verify();
	}
	
	@Test
	public void uidNextShouldBeEqualsToMaxPlusOneWhenNoPreviousTreatment() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		ObmDomain domain = ObmDomain.builder().uuid(domainId).name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.daily())
						.time(LocalTime.parse("13:23"))
						.build())
				.build();
		expect(archiveTreatmentDao.findLastTerminated(domainId, Limit.from(1)))
			.andReturn(ImmutableList.<ArchiveTreatment> of());
		
		DateTime treatmentDate = DateTime.parse("2014-08-27T12:18:00.000Z");
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate).times(2);
		
		DateTime lowerBoundary = new DateTime(0, DateTimeZone.UTC);
		DateTime higherBoundary = DateTime.parse("2014-08-26T12:18:00.000Z");
		expect(schedulingDatesService.higherBoundary(treatmentDate, RepeatKind.DAILY))
			.andReturn(higherBoundary);
		
		List<ListInfo> expectedListInfos = ImmutableList.of(new ListInfo("user/usera@mydomain.org", true, false));
		ListResult listResult = new ListResult(1);
		listResult.addAll(expectedListInfos);
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll())
			.andReturn(listResult);
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77");
		expectImapCommandsOnMailboxProcessing("user/usera@mydomain.org", "user/usera/ARCHIVE/2014/INBOX@mydomain.org", "user/usera/TEMPORARY_ARCHIVE_FOLDER/INBOX@mydomain.org", 
				ImmutableSet.of(Range.closed(1l, 10l)), lowerBoundary, higherBoundary, treatmentDate, runId, storeClient);
		
		storeClient.close();
		expectLastCall();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient).times(2);
		
		control.replay();
		imapArchiveProcessing.archive(new ArchiveConfiguration(domainConfiguration, null, null, runId, logger, loggerAppenders, false));
		control.verify();
	}
	
	@Test
	public void uidNextShouldBeEqualsToPreviousWhenPreviousTreatmentAndNoMails() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		ObmDomain domain = ObmDomain.builder().uuid(domainId).name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.daily())
						.time(LocalTime.parse("13:23"))
						.build())
				.build();
		
		DateTime previousTreatmentDate = DateTime.parse("2014-08-25T12:18:00.000Z");
		ArchiveTreatmentRunId previousRunId = ArchiveTreatmentRunId.from("800de692-f977-446b-8fee-978cf5b3d7c1");
		expect(archiveTreatmentDao.findLastTerminated(domainId, Limit.from(1)))
			.andReturn(ImmutableList.<ArchiveTreatment> of(ArchiveTerminatedTreatment.builder(domainId)
					.runId(previousRunId)
					.status(ArchiveStatus.SUCCESS)
					.recurrent(true)
					.scheduledAt(previousTreatmentDate)
					.startedAt(previousTreatmentDate)
					.terminatedAt(previousTreatmentDate)
					.higherBoundary(previousTreatmentDate)
					.build()));
		
		ImapFolder imapFolder = ImapFolder.from("user/usera@mydomain.org");
		expect(processedFolderDao.get(previousRunId, imapFolder))
			.andReturn(Optional.of(ProcessedFolder.builder()
					.runId(previousRunId)
					.folder(imapFolder)
					.lastUid(123)
					.start(previousTreatmentDate)
					.end(previousTreatmentDate)
					.status(ArchiveStatus.SUCCESS)
					.build()));
		
		DateTime treatmentDate = DateTime.parse("2014-08-27T12:18:00.000Z");
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate).times(2);
		
		DateTime higherBoundary = DateTime.parse("2014-08-26T12:18:00.000Z");
		expect(schedulingDatesService.higherBoundary(treatmentDate, RepeatKind.DAILY))
			.andReturn(higherBoundary);
		
		List<ListInfo> expectedListInfos = ImmutableList.of(new ListInfo("user/usera@mydomain.org", true, false));
		ListResult listResult = new ListResult(1);
		listResult.addAll(expectedListInfos);
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll())
			.andReturn(listResult);
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77");
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.select("user/usera@mydomain.org")).andReturn(true);
		expect(storeClient.uidSearch(SearchQuery.builder()
				.after(previousTreatmentDate.toDate())
				.before(higherBoundary.toDate())
				.build()))
			.andReturn(MessageSet.empty());
		
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate);
		processedFolderDao.insert(ProcessedFolder.builder()
				.runId(runId)
				.folder(imapFolder)
				.lastUid(123)
				.start(treatmentDate)
				.end(treatmentDate)
				.status(ArchiveStatus.SUCCESS)
				.build());
		expectLastCall();
		
		storeClient.close();
		expectLastCall().times(2);
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient).times(2);
		
		control.replay();
		imapArchiveProcessing.archive(new ArchiveConfiguration(domainConfiguration, null, null, runId, logger, loggerAppenders, false));
		control.verify();
	}
	
	@Test
	public void archiveShouldContinueWhenAnExceptionIsThrownByAFolderProcessing() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		ObmDomain domain = ObmDomain.builder().uuid(domainId).name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.daily())
						.time(LocalTime.parse("13:23"))
						.build())
				.build();
		
		expect(archiveTreatmentDao.findLastTerminated(domainId, Limit.from(1)))
			.andReturn(ImmutableList.<ArchiveTreatment> of());
		
		DateTime treatmentDate = DateTime.parse("2014-08-27T12:18:00.000Z");
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate).times(3);
		
		DateTime lowerBoundary = new DateTime(0, DateTimeZone.UTC);
		DateTime higherBoundary = DateTime.parse("2014-08-26T12:18:00.000Z");
		expect(schedulingDatesService.higherBoundary(treatmentDate, RepeatKind.DAILY))
			.andReturn(higherBoundary);
		
		String failingMailbox = "user/usera/Drafts@mydomain.org";
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/usera@mydomain.org", true, false),
				new ListInfo(failingMailbox, true, false),
				new ListInfo("user/usera/SPAM@mydomain.org", true, false));
		ListResult listResult = new ListResult(3);
		listResult.addAll(expectedListInfos);
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll())
			.andReturn(listResult);
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77");
		expectImapCommandsOnMailboxProcessing("user/usera@mydomain.org", "user/usera/ARCHIVE/2014/INBOX@mydomain.org", "user/usera/TEMPORARY_ARCHIVE_FOLDER/INBOX@mydomain.org", 
				ImmutableSet.of(Range.closed(1l, 10l)), lowerBoundary, higherBoundary, treatmentDate, runId, storeClient);
		
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.select(failingMailbox)).andReturn(false);
		expect(storeClient.setAcl(failingMailbox, ObmSystemUser.CYRUS, MailboxImpl.ALL_IMAP_RIGHTS)).andReturn(false);
		storeClient.close();
		expectLastCall();
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate).times(2);
		ImapFolder failingImapFolder = ImapFolder.from(failingMailbox);
		processedFolderDao.insert(ProcessedFolder.builder()
			.runId(runId)
			.folder(failingImapFolder)
			.lastUid(-1)
			.start(treatmentDate)
			.end(treatmentDate)
			.status(ArchiveStatus.ERROR)
			.build());
		expectLastCall();
		
		expectImapCommandsOnMailboxProcessing("user/usera/SPAM@mydomain.org", "user/usera/ARCHIVE/2014/SPAM@mydomain.org", "user/usera/TEMPORARY_ARCHIVE_FOLDER/SPAM@mydomain.org",
				ImmutableSet.of(Range.closed(3l, 22l), Range.closed(23l, 42l), Range.closed(43l, 62l), Range.closed(63l, 82l), Range.closed(83l, 100l)),
				lowerBoundary, higherBoundary, treatmentDate, runId, storeClient);
		
		storeClient.close();
		expectLastCall();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient).times(4);
		
		expectedException.expectCause(ImapArchiveProcessingException.class);
		
		control.replay();
		imapArchiveProcessing.archive(new ArchiveConfiguration(domainConfiguration, null, null, runId, logger, loggerAppenders, false));
		control.verify();
	}
	
	@Test
	public void archiveShouldContinuePreviousTreatmentWhenPreviousWasInError() throws Exception {
		// First launch
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		ObmDomain domain = ObmDomain.builder().uuid(domainId).name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.state(ConfigurationState.ENABLE)
				.schedulingConfiguration(SchedulingConfiguration.builder()
						.recurrence(ArchiveRecurrence.daily())
						.time(LocalTime.parse("13:23"))
						.build())
				.build();
		
		expect(archiveTreatmentDao.findLastTerminated(domainId, Limit.from(1)))
			.andReturn(ImmutableList.<ArchiveTreatment> of());
		
		DateTime treatmentDate = DateTime.parse("2014-08-27T12:18:00.000Z");
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate).times(3);
		
		DateTime lowerBoundary = new DateTime(0, DateTimeZone.UTC);
		DateTime higherBoundary = DateTime.parse("2014-08-26T12:18:00.000Z");
		expect(schedulingDatesService.higherBoundary(treatmentDate, RepeatKind.DAILY))
			.andReturn(higherBoundary);
		
		String failingMailbox = "user/usera/Drafts@mydomain.org";
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/usera@mydomain.org", true, false),
				new ListInfo(failingMailbox, true, false),
				new ListInfo("user/usera/SPAM@mydomain.org", true, false));
		ListResult listResult = new ListResult(3);
		listResult.addAll(expectedListInfos);
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll())
			.andReturn(listResult);
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77");
		expectImapCommandsOnMailboxProcessing("user/usera@mydomain.org", "user/usera/ARCHIVE/2014/INBOX@mydomain.org", "user/usera/TEMPORARY_ARCHIVE_FOLDER/INBOX@mydomain.org", 
				ImmutableSet.of(Range.closed(1l, 10l)), lowerBoundary, higherBoundary, treatmentDate, runId, storeClient);
		
		expectImapCommandsOnMailboxProcessingFails("user/usera/Drafts@mydomain.org", "user/usera/ARCHIVE/2014/Drafts@mydomain.org", "user/usera/TEMPORARY_ARCHIVE_FOLDER/Drafts@mydomain.org",
				ImmutableSet.of(Range.closed(1l, 20l), Range.closed(21l, 40l), Range.closed(41l, 60l)), 20l,
				lowerBoundary, higherBoundary, treatmentDate, runId, storeClient);
		
		expectImapCommandsOnMailboxProcessing("user/usera/SPAM@mydomain.org", "user/usera/ARCHIVE/2014/SPAM@mydomain.org", "user/usera/TEMPORARY_ARCHIVE_FOLDER/SPAM@mydomain.org",
				ImmutableSet.of(Range.closed(3l, 22l), Range.closed(23l, 42l), Range.closed(43l, 62l), Range.closed(63l, 82l), Range.closed(83l, 100l)),
				lowerBoundary, higherBoundary, treatmentDate, runId, storeClient);
		
		storeClient.close();
		expectLastCall();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient).times(4);
		
		// Continuing previous treatment
		expect(archiveTreatmentDao.findLastTerminated(domainId, Limit.from(1)))
			.andReturn(ImmutableList.<ArchiveTreatment> of(ArchiveTerminatedTreatment.builder(domainId)
					.runId(runId)
					.status(ArchiveStatus.ERROR)
					.recurrent(false)
					.scheduledAt(treatmentDate)
					.startedAt(treatmentDate)
					.terminatedAt(treatmentDate)
					.higherBoundary(higherBoundary)
					.build()));
	
		expect(schedulingDatesService.lowerBoundary(treatmentDate, RepeatKind.DAILY))
			.andReturn(lowerBoundary);
		
		ArchiveTreatmentRunId secondRunId = ArchiveTreatmentRunId.from("70044a54-1269-49dd-8e17-991b83816c72");
		expectImapCommandsOnAlreadyProcessedMailbox("user/usera@mydomain.org", treatmentDate, lowerBoundary, higherBoundary, runId, secondRunId, 10, storeClient);
		
		expect(processedFolderDao.get(runId, ImapFolder.from("user/usera/Drafts@mydomain.org")))
			.andReturn(Optional.<ProcessedFolder> absent());
		expectImapCommandsOnMailboxProcessing("user/usera/Drafts@mydomain.org", "user/usera/ARCHIVE/2014/Drafts@mydomain.org", "user/usera/TEMPORARY_ARCHIVE_FOLDER/Drafts@mydomain.org", 
				ImmutableSet.of(Range.closed(1l, 20l), Range.closed(21l, 40l), Range.closed(41l, 60l)), 
				lowerBoundary, higherBoundary, treatmentDate, secondRunId, storeClient);
		
		expectImapCommandsOnAlreadyProcessedMailbox("user/usera/SPAM@mydomain.org", treatmentDate, lowerBoundary, higherBoundary, runId, secondRunId, 100, storeClient);
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient).times(4);
		expect(storeClient.listAll())
			.andReturn(listResult);
		storeClient.login(false);
		expectLastCall();
		
		storeClient.close();
		expectLastCall();

		try {
			control.replay();
			imapArchiveProcessing.archive(new ArchiveConfiguration(domainConfiguration, null, null, runId, logger, loggerAppenders, false));
		} catch (Exception e) {
			imapArchiveProcessing.archive(new ArchiveConfiguration(domainConfiguration, null, null, secondRunId, logger, loggerAppenders, false));
		} finally {
			control.verify();
		}
	}

	private void expectImapCommandsOnAlreadyProcessedMailbox(String mailbox, DateTime treatmentDate, DateTime lowerBoundary, DateTime higherBoundary, 
			ArchiveTreatmentRunId runId, ArchiveTreatmentRunId secondRunId, long lastUid, StoreClient storeClient) throws Exception {
		
		storeClient.login(false);
		expectLastCall();
		
		expect(storeClient.select(mailbox)).andReturn(true);
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate).times(2);
		ImapFolder imapFolder = ImapFolder.from(mailbox);
		expect(processedFolderDao.get(runId, imapFolder))
			.andReturn(Optional.of(ProcessedFolder.builder()
					.runId(runId)
					.folder(imapFolder)
					.lastUid(lastUid)
					.start(treatmentDate)
					.end(treatmentDate)
					.status(ArchiveStatus.SUCCESS)
					.build()));
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate);
		expect(storeClient.uidSearch(SearchQuery.builder()
				.after(lowerBoundary.toDate())
				.before(higherBoundary.toDate())
				.build()))
			.andReturn(MessageSet.builder().add(Range.closed(0l, lastUid)).build());
		processedFolderDao.insert(ProcessedFolder.builder()
				.runId(secondRunId)
				.folder(imapFolder)
				.lastUid(lastUid)
				.start(treatmentDate)
				.end(treatmentDate)
				.status(ArchiveStatus.SUCCESS)
				.build());
		expectLastCall();
		storeClient.close();
		expectLastCall();
	}
	
	private void expectImapCommandsOnMailboxProcessingFails(String mailboxName, String archiveMailboxName, String temporaryMailboxName, Set<Range<Long>> uids,
				long lastUid, DateTime lowerBoundary, DateTime higherBoundary, DateTime treatmentDate, ArchiveTreatmentRunId runId, StoreClient storeClient) 
			throws Exception {
		
		MessageSet.Builder messageSetBuilder = MessageSet.builder();
		for (Range<Long> range : uids) {
			messageSetBuilder.add(range);
		}
		MessageSet messageSet = messageSetBuilder.build();
		
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.select(mailboxName)).andReturn(true);
		expect(storeClient.uidSearch(SearchQuery.builder()
				.after(lowerBoundary.toDate())
				.before(higherBoundary.toDate())
				.build()))
			.andReturn(messageSet);
		
		expectCreateMailbox(mailboxName, archiveMailboxName, storeClient);
		
		expectCreateMailbox(mailboxName, temporaryMailboxName, storeClient);
		expect(storeClient.uidCopy(messageSet, temporaryMailboxName)).andReturn(messageSet);
		
		expectCopyPartitionFailsOnSecond(archiveMailboxName, uids, storeClient);
		
		expect(storeClient.delete(temporaryMailboxName)).andReturn(true);
		
		storeClient.close();
		expectLastCall();
		
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate).times(2);
		processedFolderDao.insert(ProcessedFolder.builder()
				.runId(runId)
				.folder(ImapFolder.from(mailboxName))
				.lastUid(lastUid)
				.start(treatmentDate)
				.end(treatmentDate)
				.status(ArchiveStatus.ERROR)
				.build());
		expectLastCall();
	}
	
	private void expectImapCommandsOnMailboxProcessing(String mailboxName, String archiveMailboxName, String temporaryMailboxName, Set<Range<Long>> uids,
				DateTime lowerBoundary, DateTime higherBoundary, DateTime treatmentDate, ArchiveTreatmentRunId runId, StoreClient storeClient) 
			throws Exception {
		
		MessageSet.Builder messageSetBuilder = MessageSet.builder();
		for (Range<Long> range : uids) {
			messageSetBuilder.add(range);
		}
		MessageSet messageSet = messageSetBuilder.build();
		
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.select(mailboxName)).andReturn(true);
		expect(storeClient.uidSearch(SearchQuery.builder()
				.after(lowerBoundary.toDate())
				.before(higherBoundary.toDate())
				.build()))
			.andReturn(messageSet);
		
		expectCreateMailbox(mailboxName, archiveMailboxName, storeClient);
		
		expectCreateMailbox(mailboxName, temporaryMailboxName, storeClient);
		expect(storeClient.uidCopy(messageSet, temporaryMailboxName)).andReturn(messageSet);
		
		expectCopyPartition(archiveMailboxName, uids, storeClient);
		
		expect(storeClient.delete(temporaryMailboxName)).andReturn(true);
		expect(storeClient.select(archiveMailboxName)).andReturn(true);
		expect(storeClient.uidStore(messageSet, new FlagsList(ImmutableSet.of(Flag.SEEN)), true)).andReturn(true);
		
		storeClient.close();
		expectLastCall();
		
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate);
		processedFolderDao.insert(ProcessedFolder.builder()
				.runId(runId)
				.folder(ImapFolder.from(mailboxName))
				.lastUid(Ordering.natural().max(messageSet.asDiscreteValues()))
				.start(treatmentDate)
				.end(treatmentDate)
				.status(ArchiveStatus.SUCCESS)
				.build());
		expectLastCall();
	}

	private void expectCreateMailbox(String mailboxName, String archiveMailboxName, StoreClient storeClient) throws MailboxNotFoundException {
		expect(storeClient.select(archiveMailboxName)).andReturn(false);
		expect(storeClient.create(archiveMailboxName, "mydomain_org_archive")).andReturn(true);
		expect(storeClient.setAcl(archiveMailboxName, ObmSystemUser.CYRUS, MailboxImpl.ALL_IMAP_RIGHTS)).andReturn(true);
		expect(storeClient.setAcl(archiveMailboxName, "usera@mydomain.org", MailboxImpl.READ_IMAP_RIGHTS)).andReturn(true);
		expect(storeClient.select(archiveMailboxName)).andReturn(true);
		expect(storeClient.select(mailboxName)).andReturn(true);
	}

	private void expectCopyPartitionFailsOnSecond(String archiveMailboxName, Set<Range<Long>> uids, StoreClient storeClient) throws MailboxNotFoundException {
		MessageSet firstMessageSet = MessageSet.builder()
				.add(Iterables.get(uids, 0))
				.build();
		expect(storeClient.uidCopy(firstMessageSet, archiveMailboxName)).andReturn(firstMessageSet);
		
		MessageSet secondMessageSet = MessageSet.builder()
				.add(Iterables.get(uids, 1))
				.build();
		expect(storeClient.uidCopy(secondMessageSet, archiveMailboxName)).andThrow(new ImapTimeoutException());
	}

	private void expectCopyPartition(String archiveMailboxName, Set<Range<Long>> uids, StoreClient storeClient) throws MailboxNotFoundException {
		for (Range<Long> partition : uids) {
			MessageSet messageSet = MessageSet.builder()
					.add(partition)
					.build();
			
			expect(storeClient.uidCopy(messageSet, archiveMailboxName)).andReturn(messageSet);
		}
	}
	
	@Test
	public void previousArchiveTreatmentShouldBeAbsentWhenNone() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		expect(archiveTreatmentDao.findLastTerminated(domainId, Limit.from(1)))
			.andReturn(ImmutableList.<ArchiveTreatment> of());
		
		control.replay();
		Optional<ArchiveTreatment> previousArchiveTreatment = imapArchiveProcessing.previousArchiveTreatment(domainId);
		control.verify();
		assertThat(previousArchiveTreatment).isAbsent();
	}
	
	@Test
	public void previousArchiveTreatmentShouldReturnPrevious() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		expect(archiveTreatmentDao.findLastTerminated(domainId, Limit.from(1)))
			.andReturn(ImmutableList.<ArchiveTreatment> of(ArchiveTreatment.builder(domainId)
					.runId(ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77"))
					.recurrent(true)
					.scheduledAt(DateTime.parse("2014-08-26T08:46:00.000Z"))
					.higherBoundary(DateTime.parse("2014-09-26T08:46:00.000Z"))
					.status(ArchiveStatus.SCHEDULED)
					.build()));
		
		control.replay();
		Optional<ArchiveTreatment> previousArchiveTreatment = imapArchiveProcessing.previousArchiveTreatment(domainId);
		control.verify();
		assertThat(previousArchiveTreatment).isPresent();
	}
	
	@Test
	public void calculateBoundariesWhenNoPreviousArchiveTreatment() {
		DateTime start = DateTime.parse("2014-08-26T08:46:00.000Z");
		DateTime higherBoundary = DateTime.parse("2014-07-26T08:46:00.000Z");
		expect(schedulingDatesService.higherBoundary(start, RepeatKind.MONTHLY))
			.andReturn(higherBoundary);
		
		control.replay();
		Boundaries boundaries = imapArchiveProcessing.calculateBoundaries(start, RepeatKind.MONTHLY, Optional.<ArchiveTreatment> absent(), logger);
		control.verify();
		assertThat(boundaries.getLowerBoundary()).isEqualTo(new DateTime(0, DateTimeZone.UTC));
		assertThat(boundaries.getHigherBoundary()).isEqualTo(higherBoundary);
	}
	
	@Test
	public void calculateBoundariesShouldContinueWhenPreviousArchiveTreatmentIsInError() {
		DateTime start = DateTime.parse("2014-08-26T08:46:00.000Z");
		DateTime higherBoundary = DateTime.parse("2014-07-26T08:46:00.000Z");
		
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		ArchiveTreatment archiveTreatment = ArchiveTreatment.builder(domainId)
			.runId(ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77"))
			.recurrent(true)
			.scheduledAt(start)
			.higherBoundary(higherBoundary)
			.status(ArchiveStatus.ERROR)
			.build();
		
		DateTime lowerBoundary = DateTime.parse("2014-06-26T08:46:00.000Z");
		expect(schedulingDatesService.lowerBoundary(start, RepeatKind.MONTHLY))
			.andReturn(lowerBoundary);
		
		control.replay();
		Boundaries boundaries = imapArchiveProcessing.calculateBoundaries(start, RepeatKind.MONTHLY, Optional.fromNullable(archiveTreatment), logger);
		control.verify();
		assertThat(boundaries.getLowerBoundary()).isEqualTo(lowerBoundary);
		assertThat(boundaries.getHigherBoundary()).isEqualTo(higherBoundary);
	}
	
	@Test
	public void calculateBoundariesShouldBeNextWhenPreviousArchiveTreatmentIsSuccess() {
		DateTime start = DateTime.parse("2014-08-26T08:46:00.000Z");
		DateTime lowerBoundary = DateTime.parse("2014-06-26T08:46:00.000Z");
		
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		ArchiveTreatment archiveTreatment = ArchiveTreatment.builder(domainId)
			.runId(ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77"))
			.recurrent(true)
			.scheduledAt(start)
			.higherBoundary(lowerBoundary)
			.status(ArchiveStatus.SUCCESS)
			.build();
		
		DateTime higherBoundary = DateTime.parse("2014-07-26T08:46:00.000Z");
		expect(schedulingDatesService.higherBoundary(start, RepeatKind.MONTHLY))
			.andReturn(higherBoundary);
		
		control.replay();
		Boundaries boundaries = imapArchiveProcessing.calculateBoundaries(start, RepeatKind.MONTHLY, Optional.fromNullable(archiveTreatment), logger);
		control.verify();
		assertThat(boundaries.getLowerBoundary()).isEqualTo(lowerBoundary);
		assertThat(boundaries.getHigherBoundary()).isEqualTo(higherBoundary);
	}
	
	@Test
	public void listImapFoldersShouldListAllWhenNoExcludedFolder() throws Exception {
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/usera@mydomain.org", true, false),
				new ListInfo("user/usera/Drafts@mydomain.org", true, false),
				new ListInfo("user/usera/SPAM@mydomain.org", true, false),
				new ListInfo("user/usera/Sent@mydomain.org", true, false),
				new ListInfo("user/usera/Excluded@mydomain.org", true, false),
				new ListInfo("user/usera/Excluded/subfolder@mydomain.org", true, false));
		ListResult listResult = new ListResult(6);
		listResult.addAll(expectedListInfos);
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll())
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
				.build();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);
		
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"), logger, null, false);
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.boundaries(Boundaries.builder()
						.lowerBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = imapArchiveProcessing.listImapFolders(processedTask);
		control.verify();
		assertThat(listImapFolders).containsOnly(FluentIterable.from(expectedListInfos).toArray(ListInfo.class));
	}
	
	@Test
	public void listImapFoldersShouldFilterWhenExcludedFolder() throws Exception {
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/usera@mydomain.org", true, false),
				new ListInfo("user/usera/Drafts@mydomain.org", true, false),
				new ListInfo("user/usera/SPAM@mydomain.org", true, false),
				new ListInfo("user/usera/Sent@mydomain.org", true, false));
		ListResult listResult = new ListResult(6);
		listResult.addAll(expectedListInfos);
		listResult.add(new ListInfo("user/usera/Excluded@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/Excluded/subfolder@mydomain.org", true, false));
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll())
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
			.excludedFolder("Excluded")
			.build();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);
		
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"), logger, null, false);
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.boundaries(Boundaries.builder()
						.lowerBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = imapArchiveProcessing.listImapFolders(processedTask);
		control.verify();
		assertThat(listImapFolders).containsOnly(FluentIterable.from(expectedListInfos).toArray(ListInfo.class));
	}
	
	@Test
	public void listImapFoldersShouldFilterWhenExcludedUsers() throws Exception {
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
		expect(storeClient.listAll())
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
			.excludedUsers(ImmutableList.of(ExcludedUser.builder()
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
				.boundaries(Boundaries.builder()
						.lowerBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = imapArchiveProcessing.listImapFolders(processedTask);
		control.verify();
		assertThat(listImapFolders).containsOnly(FluentIterable.from(expectedListInfos).toArray(ListInfo.class));
	}
	
	@Test
	public void listImapFoldersShouldNotFilterWhenPathDoesntStartWithExcludedUser() throws Exception {
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/user/usera@mydomain.org", true, false));
		ListResult listResult = new ListResult(1);
		listResult.addAll(expectedListInfos);
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll())
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
			.excludedUsers(ImmutableList.of(ExcludedUser.builder()
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
				.boundaries(Boundaries.builder()
						.lowerBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = imapArchiveProcessing.listImapFolders(processedTask);
		control.verify();
		assertThat(listImapFolders).containsOnly(FluentIterable.from(expectedListInfos).toArray(ListInfo.class));
	}
	
	@Test
	public void listImapFoldersShouldFilterArchiveFolder() throws Exception {
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/usera@mydomain.org", true, false),
				new ListInfo("user/usera/Drafts@mydomain.org", true, false),
				new ListInfo("user/usera/SPAM@mydomain.org", true, false),
				new ListInfo("user/usera/Sent@mydomain.org", true, false),
				new ListInfo("user/usera/Excluded@mydomain.org", true, false),
				new ListInfo("user/usera/Excluded/subfolder@mydomain.org", true, false));
		ListResult listResult = new ListResult(6);
		listResult.addAll(expectedListInfos);
		listResult.add(new ListInfo("user/usera/" + ImapArchiveConfigurationServiceImpl.DEFAULT_ARCHIVE_MAIN_FOLDER + "/Excluded@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/" + ImapArchiveConfigurationServiceImpl.DEFAULT_ARCHIVE_MAIN_FOLDER + "/Excluded/subfolder@mydomain.org", true, false));
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll())
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
				.build();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);

		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7");
		ArchiveConfiguration archiveConfiguration = new ArchiveConfiguration(
				domainConfiguration, null, null, runId, logger, null, false);
		
		control.replay();
		
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveConfiguration(archiveConfiguration)
				.boundaries(Boundaries.builder()
						.lowerBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = imapArchiveProcessing.listImapFolders(processedTask);
		control.verify();
		assertThat(listImapFolders).containsOnly(FluentIterable.from(expectedListInfos).toArray(ListInfo.class));
	}
	
	@Test
	public void searchMailUidsWhenNoPreviousUidNext() throws Exception {
		DateTime lowerBoundary = DateTime.parse("2014-06-26T08:46:00.000Z");
		DateTime higherBoundary = DateTime.parse("2014-07-26T08:46:00.000Z");
		Boundaries boundaries = Boundaries.builder()
				.lowerBoundary(lowerBoundary)
				.higherBoundary(higherBoundary)
				.build();
		StoreClient storeClient = control.createMock(StoreClient.class);
		expect(storeClient.uidSearch(SearchQuery.builder()
				.after(lowerBoundary.toDate())
				.before(higherBoundary.toDate())
				.build()))
			.andReturn(MessageSet.builder()
					.add(Range.<Long> openClosed(4l, 12l))
					.build());
		
		control.replay();
		FluentIterable<Long> searchMailUids = imapArchiveProcessing.searchMailUids(MailboxImpl.from("mailbox", logger, storeClient), boundaries, Optional.<Long> absent());
		control.verify();
		assertThat(searchMailUids).containsOnly(5l, 6l, 7l, 8l, 9l, 10l, 11l, 12l);
	}
	
	@Test
	public void searchMailUidsShouldReturnAllUidsWhenPreviousUidnextIsLower() throws Exception {
		DateTime lowerBoundary = DateTime.parse("2014-06-26T08:46:00.000Z");
		DateTime higherBoundary = DateTime.parse("2014-07-26T08:46:00.000Z");
		Boundaries boundaries = Boundaries.builder()
				.lowerBoundary(lowerBoundary)
				.higherBoundary(higherBoundary)
				.build();
		StoreClient storeClient = control.createMock(StoreClient.class);
		expect(storeClient.uidSearch(SearchQuery.builder()
				.after(lowerBoundary.toDate())
				.before(higherBoundary.toDate())
				.build()))
			.andReturn(MessageSet.builder()
					.add(Range.<Long> openClosed(4l, 12l))
					.build());
		
		control.replay();
		FluentIterable<Long> searchMailUids = imapArchiveProcessing.searchMailUids(MailboxImpl.from("mailbox", logger, storeClient), boundaries, Optional.of(2l));
		control.verify();
		assertThat(searchMailUids).containsOnly(5l, 6l, 7l, 8l, 9l, 10l, 11l, 12l);
	}
	
	@Test
	public void searchMailUidsShouldReturnFilteredUidsWhenPreviousLastUidIsInRange() throws Exception {
		DateTime lowerBoundary = DateTime.parse("2014-06-26T08:46:00.000Z");
		DateTime higherBoundary = DateTime.parse("2014-07-26T08:46:00.000Z");
		Boundaries boundaries = Boundaries.builder()
				.lowerBoundary(lowerBoundary)
				.higherBoundary(higherBoundary)
				.build();
		StoreClient storeClient = control.createMock(StoreClient.class);
		expect(storeClient.uidSearch(SearchQuery.builder()
				.after(lowerBoundary.toDate())
				.before(higherBoundary.toDate())
				.build()))
			.andReturn(MessageSet.builder()
					.add(Range.<Long> openClosed(4l, 12l))
					.build());
		
		control.replay();
		FluentIterable<Long> searchMailUids = imapArchiveProcessing.searchMailUids(MailboxImpl.from("mailbox", logger, storeClient), boundaries, Optional.of(6l));
		control.verify();
		assertThat(searchMailUids).containsOnly(7l, 8l, 9l, 10l, 11l, 12l);
	}
	
	@Test
	public void previousUidnextShouldReturnAbsentWhenNoPreviousArchiveTreatment() throws Exception {
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		control.replay();
		Optional<Long> previousUidnext = imapArchiveProcessing.previousLastUid(MailboxImpl.from("mailbox", logger, storeClient), Optional.<ArchiveTreatment> absent());
		control.verify();
		assertThat(previousUidnext).isAbsent();
	}
	
	@Test
	public void previousUidnextShouldReturnAbsentWhenNoPreviousProcessedFolder() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77");
		ArchiveTreatment archiveTreatment = ArchiveTreatment.builder(ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6"))
				.runId(runId)
				.recurrent(true)
				.scheduledAt(DateTime.parse("2014-08-26T08:46:00.000Z"))
				.higherBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		String mailbox = "mailbox";
		expect(processedFolderDao.get(runId, ImapFolder.from(mailbox)))
			.andReturn(Optional.<ProcessedFolder> absent());
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		control.replay();
		Optional<Long> previousUidnext = imapArchiveProcessing.previousLastUid(MailboxImpl.from("mailbox", logger, storeClient), Optional.fromNullable(archiveTreatment));
		control.verify();
		assertThat(previousUidnext).isAbsent();
	}
	
	@Test
	public void previousUidnextShouldReturnPreviousUidnext() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77");
		ArchiveTreatment archiveTreatment = ArchiveTreatment.builder(ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6"))
				.runId(runId)
				.recurrent(true)
				.scheduledAt(DateTime.parse("2014-08-26T08:46:00.000Z"))
				.higherBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		String mailbox = "mailbox";
		ImapFolder imapFolder = ImapFolder.from(mailbox);
		long expectedUidnext = 5;
		expect(processedFolderDao.get(runId, imapFolder))
			.andReturn(Optional.fromNullable(ProcessedFolder.builder()
					.runId(ArchiveTreatmentRunId.from("1d7b24df-cdb1-42f2-b3e1-2c1238b2c071"))
					.folder(imapFolder)
					.lastUid(expectedUidnext)
					.start(DateTime.parse("2014-05-26T08:46:00.000Z"))
					.end(DateTime.parse("2014-05-26T08:46:01.000Z"))
					.status(ArchiveStatus.SUCCESS)
					.build()));
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		control.replay();
		Optional<Long> previousUidnext = imapArchiveProcessing.previousLastUid(MailboxImpl.from(mailbox, logger, storeClient), Optional.fromNullable(archiveTreatment));
		control.verify();
		assertThat(previousUidnext).isPresent();
		assertThat(previousUidnext.get()).isEqualTo(expectedUidnext);
	}
	
	@Test
	public void continuePreviousShouldBeFalseWhenPreviousArchiveTreatmentIsAbsent() {
		
		control.replay();
		boolean continuePrevious = imapArchiveProcessing.continuePrevious(Optional.<ArchiveTreatment> absent(), DateTime.now());
		control.verify();
		assertThat(continuePrevious).isFalse();
	}
	
	@Test
	public void continuePreviousShouldBeFalseWhenPreviousArchiveTreatmentIsPresentAndHigherBoundaryDoesntMatch() {
		ObmDomainUuid domainId = ObmDomainUuid.of("b1a32567-05de-4d06-b699-ad94a7c59744");
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("011ce5f5-b56a-44b3-a2e6-e19942684d45");
		
		DateTime previousHigherBoundary = DateTime.parse("2014-07-23T23:59:59.999Z");
		Optional<ArchiveTreatment> previousArchiveTreatment = Optional.<ArchiveTreatment> of(ArchiveTreatment.builder(domainId)
			.runId(runId)
			.recurrent(true)
			.status(ArchiveStatus.ERROR)
			.scheduledAt(DateTime.now())
			.higherBoundary(previousHigherBoundary)
			.build());
		
		control.replay();
		boolean continuePrevious = imapArchiveProcessing.continuePrevious(previousArchiveTreatment, DateTime.now());
		control.verify();
		assertThat(continuePrevious).isFalse();
	}
	
	@Test
	public void continuePreviousShouldBeTrueWhenPreviousArchiveTreatmentIsPresentAndHigherBoundaryMatch() {
		ObmDomainUuid domainId = ObmDomainUuid.of("b1a32567-05de-4d06-b699-ad94a7c59744");
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("011ce5f5-b56a-44b3-a2e6-e19942684d45");
		
		DateTime previousHigherBoundary = DateTime.parse("2014-07-23T23:59:59.999Z");
		Optional<ArchiveTreatment> previousArchiveTreatment = Optional.<ArchiveTreatment> of(ArchiveTreatment.builder(domainId)
			.runId(runId)
			.recurrent(true)
			.status(ArchiveStatus.ERROR)
			.scheduledAt(DateTime.now())
			.higherBoundary(previousHigherBoundary)
			.build());
		
		control.replay();
		boolean continuePrevious = imapArchiveProcessing.continuePrevious(previousArchiveTreatment, previousHigherBoundary);
		control.verify();
		assertThat(continuePrevious).isTrue();
	}
	
	@Test
	public void continuePreviousShouldBeFalseWhenPreviousArchiveTreatmentIsSuccess() {
		ObmDomainUuid domainId = ObmDomainUuid.of("b1a32567-05de-4d06-b699-ad94a7c59744");
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("011ce5f5-b56a-44b3-a2e6-e19942684d45");
		
		DateTime previousHigherBoundary = DateTime.parse("2014-07-23T23:59:59.999Z");
		Optional<ArchiveTreatment> previousArchiveTreatment = Optional.<ArchiveTreatment> of(ArchiveTreatment.builder(domainId)
			.runId(runId)
			.recurrent(true)
			.status(ArchiveStatus.SUCCESS)
			.scheduledAt(DateTime.now())
			.higherBoundary(previousHigherBoundary)
			.build());
		
		control.replay();
		boolean continuePrevious = imapArchiveProcessing.continuePrevious(previousArchiveTreatment, previousHigherBoundary);
		control.verify();
		assertThat(continuePrevious).isFalse();
	}
}
