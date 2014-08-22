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

import org.easymock.IMocksControl;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.Boundaries;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.ImapFolder;
import org.obm.imap.archive.beans.ProcessedFolder;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.dao.ProcessedFolderDao;
import org.obm.imap.archive.exception.DomainConfigurationException;
import org.obm.imap.archive.exception.ImapArchiveProcessingException;
import org.obm.imap.archive.logging.LoggerAppenders;
import org.obm.imap.archive.scheduling.ArchiveDomainTask;
import org.obm.imap.archive.scheduling.TestArchiveDomainTaskFactory;
import org.obm.imap.archive.services.ImapArchiveProcessing.ProcessedTask;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
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
import com.google.common.collect.Range;
import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.system.ObmSystemUser;

public class ImapArchiveProcessingTest {

	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
	@Rule public FluentExpectedException expectedException = FluentExpectedException.none();
	
	private IMocksControl control;
	private DateTimeProvider dateTimeProvider;
	private SchedulingDatesService schedulingDatesService;
	private StoreClientFactory storeClientFactory;
	private DomainClient domainClient;
	private ArchiveTreatmentDao archiveTreatmentDao;
	private DomainConfigurationDao domainConfigurationDao;
	private ProcessedFolderDao processedFolderDao;
	private Logger logger;
	private LoggerAppenders loggerAppenders;
	
	private ImapArchiveProcessing imapArchiveProcessing;

	@Before
	public void setup() throws IOException {
		control = createControl();
		dateTimeProvider = control.createMock(DateTimeProvider.class);
		schedulingDatesService = control.createMock(SchedulingDatesService.class);
		storeClientFactory = control.createMock(StoreClientFactory.class);
		domainClient = control.createMock(DomainClient.class);
		archiveTreatmentDao = control.createMock(ArchiveTreatmentDao.class);
		domainConfigurationDao = control.createMock(DomainConfigurationDao.class);
		processedFolderDao = control.createMock(ProcessedFolderDao.class);
		logger = (Logger) LoggerFactory.getLogger(temporaryFolder.newFile().getAbsolutePath());
		loggerAppenders = control.createMock(LoggerAppenders.class);
		
		imapArchiveProcessing = new ImapArchiveProcessing(dateTimeProvider, schedulingDatesService, storeClientFactory, domainClient, archiveTreatmentDao, domainConfigurationDao, processedFolderDao);
	}
	
	@Test
	public void archiveShouldWork() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		ObmDomain domain = ObmDomain.builder().uuid(domainId).name("mydomain.org").build();
		expect(domainClient.getById(domainId))
			.andReturn(Optional.of(domain));
		expect(domainConfigurationDao.get(domainId))
			.andReturn(DomainConfiguration.builder()
					.domainId(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"))
					.enabled(true)
					.schedulingConfiguration(SchedulingConfiguration.builder()
							.recurrence(ArchiveRecurrence.daily())
							.time(LocalTime.parse("13:23"))
							.build())
					.build());
		
		expect(archiveTreatmentDao.findLastTerminated(domainId, 1))
			.andReturn(ImmutableList.<ArchiveTreatment> of());
		
		DateTime treatmentDate = DateTime.parse("2014-08-27T12:18:00.000Z");
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate).times(4);
		
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
		expectImapCommandsOnMailboxProcessing("user/usera@mydomain.org", "user/usera/ARCHIVE/2014/INBOX@mydomain.org", 
				Range.openClosed(1l, 10l), higherBoundary, storeClient);
		expectImapCommandsOnMailboxProcessing("user/usera/Drafts@mydomain.org", "user/usera/ARCHIVE/2014/Drafts@mydomain.org", 
				Range.openClosed(3l, 100l), higherBoundary, storeClient);
		expectImapCommandsOnMailboxProcessing("user/usera/SPAM@mydomain.org", "user/usera/ARCHIVE/2014/SPAM@mydomain.org", 
				Range.singleton(1230l), higherBoundary, storeClient);
		
		storeClient.close();
		expectLastCall();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient).times(4);
		
		control.replay();
		imapArchiveProcessing.archive(new TestArchiveDomainTaskFactory(logger, loggerAppenders).create(domainId, runId));
		control.verify();
	}
	
	@Test
	public void archiveShouldContinueWhenAnExceptionIsThrownByAFolderProcessing() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		ObmDomain domain = ObmDomain.builder().uuid(domainId).name("mydomain.org").build();
		expect(domainClient.getById(domainId))
			.andReturn(Optional.of(domain));
		expect(domainConfigurationDao.get(domainId))
			.andReturn(DomainConfiguration.builder()
					.domainId(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"))
					.enabled(true)
					.schedulingConfiguration(SchedulingConfiguration.builder()
							.recurrence(ArchiveRecurrence.daily())
							.time(LocalTime.parse("13:23"))
							.build())
					.build());
		
		expect(archiveTreatmentDao.findLastTerminated(domainId, 1))
			.andReturn(ImmutableList.<ArchiveTreatment> of());
		
		DateTime treatmentDate = DateTime.parse("2014-08-27T12:18:00.000Z");
		expect(dateTimeProvider.now())
			.andReturn(treatmentDate).times(3);
		
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
		expectImapCommandsOnMailboxProcessing("user/usera@mydomain.org", "user/usera/ARCHIVE/2014/INBOX@mydomain.org", 
				Range.openClosed(1l, 10l), higherBoundary, storeClient);
		
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.select(failingMailbox)).andReturn(false);
		expect(storeClient.setAcl(failingMailbox, ObmSystemUser.CYRUS, Mailbox.ALL_IMAP_RIGHTS)).andReturn(false);
		storeClient.close();
		expectLastCall();
		
		expectImapCommandsOnMailboxProcessing("user/usera/SPAM@mydomain.org", "user/usera/ARCHIVE/2014/SPAM@mydomain.org", 
				Range.openClosed(3l, 100l), higherBoundary, storeClient);
		
		storeClient.close();
		expectLastCall();
		
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient).times(4);
		
		expectedException.expectCause(ImapArchiveProcessingException.class);
		
		control.replay();
		imapArchiveProcessing.archive(new TestArchiveDomainTaskFactory(logger, loggerAppenders).create(domainId, runId));
		control.verify();
	}
	
	private void expectImapCommandsOnMailboxProcessing(String mailboxName, String archiveMailboxName, Range<Long> uids, DateTime higherBoundary, StoreClient storeClient) throws Exception {
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.select(mailboxName)).andReturn(true);
		expect(storeClient.uidSearch(SearchQuery.builder()
				.after(new DateTime(0, DateTimeZone.UTC).toDate())
				.before(higherBoundary.toDate())
				.build()))
			.andReturn(MessageSet.builder()
					.add(uids)
					.build());
		expect(storeClient.uidNext(mailboxName))
			.andReturn(uids.upperEndpoint());
		expect(storeClient.select(archiveMailboxName)).andReturn(false);
		expect(storeClient.create(archiveMailboxName, "mydomain_org_archive")).andReturn(true);
		expect(storeClient.setAcl(archiveMailboxName, ObmSystemUser.CYRUS, Mailbox.ALL_IMAP_RIGHTS)).andReturn(true);
		expect(storeClient.setAcl(archiveMailboxName, "usera@mydomain.org", Mailbox.READ_IMAP_RIGHTS)).andReturn(true);
		expect(storeClient.select(archiveMailboxName)).andReturn(true);
		expect(storeClient.select(mailboxName)).andReturn(true);
		storeClient.close();
		expectLastCall();
	}
	
	@Test
	public void archiveShouldThrowWhenNoDomainFound() {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		expect(domainClient.getById(domainId))
			.andReturn(Optional.<ObmDomain> absent());
		
		expectedException
			.expectCause(DomainNotFoundException.class)
			.hasMessage("The domain with the uuid fc2f915e-9df4-4560-b141-7b4c7ddecdd6 was not found");
		
		control.replay();
		imapArchiveProcessing.archive(new TestArchiveDomainTaskFactory(logger, loggerAppenders).create(domainId, ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77")));
		control.verify();
	}
	
	@Test
	public void archiveShouldThrowWhenNoDomainConfigurationFound() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		expect(domainClient.getById(domainId))
			.andReturn(Optional.of(ObmDomain.builder().uuid(domainId).name("MyName").build()));
		expect(domainConfigurationDao.get(domainId))
			.andReturn(null);
		
		expectedException
			.expect(DomainConfigurationException.class)
			.hasMessage("The IMAP Archive configuration is not defined for the domain: 'MyName'");
		
		control.replay();
		imapArchiveProcessing.archive(new TestArchiveDomainTaskFactory(logger, loggerAppenders).create(domainId, ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77")));
		control.verify();
	}
	
	@Test
	public void archiveShouldThrowWhenDomainConfigurationDisable() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		expect(domainClient.getById(domainId))
			.andReturn(Optional.of(ObmDomain.builder().uuid(domainId).name("MyName").build()));
		expect(domainConfigurationDao.get(domainId))
			.andReturn(DomainConfiguration.builder()
					.domainId(domainId)
					.enabled(false)
					.build());
		
		expectedException
			.expect(DomainConfigurationException.class)
			.hasMessage("The IMAP Archive service is disabled for the domain: 'MyName'");
		
		control.replay();
		imapArchiveProcessing.archive(new TestArchiveDomainTaskFactory(logger, loggerAppenders).create(domainId, ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77")));
		control.verify();
	}
	
	@Test
	public void previousArchiveTreatmentShouldBeAbsentWhenNone() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		expect(archiveTreatmentDao.findLastTerminated(domainId, 1))
			.andReturn(ImmutableList.<ArchiveTreatment> of());
		
		control.replay();
		Optional<ArchiveTreatment> previousArchiveTreatment = imapArchiveProcessing.previousArchiveTreatment(domainId);
		control.verify();
		assertThat(previousArchiveTreatment).isAbsent();
	}
	
	@Test
	public void previousArchiveTreatmentShouldReturnPrevious() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		expect(archiveTreatmentDao.findLastTerminated(domainId, 1))
			.andReturn(ImmutableList.<ArchiveTreatment> of(ArchiveTreatment.builder(domainId)
					.runId(ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77"))
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
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);
		
		ArchiveDomainTask archiveDomainTask = control.createMock(ArchiveDomainTask.class);
		expect(archiveDomainTask.getLogger())
			.andReturn(logger);
		expect(archiveDomainTask.getRunId())
			.andReturn(ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"));
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveDomainTask(archiveDomainTask)
				.domain(domain)
				.boundaries(Boundaries.builder()
						.lowerBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.domainConfiguration(DomainConfiguration.builder()
						.domainId(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"))
						.enabled(true)
						.schedulingConfiguration(SchedulingConfiguration.builder()
								.recurrence(ArchiveRecurrence.daily())
								.time(LocalTime.parse("13:23"))
								.build())
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
		
		ObmDomain domain = ObmDomain.builder().name("mydomain.org").build();
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);
		
		ArchiveDomainTask archiveDomainTask = control.createMock(ArchiveDomainTask.class);
		expect(archiveDomainTask.getLogger())
			.andReturn(logger);
		expect(archiveDomainTask.getRunId())
			.andReturn(ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"));
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveDomainTask(archiveDomainTask)
				.domain(domain)
				.boundaries(Boundaries.builder()
						.lowerBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.domainConfiguration(DomainConfiguration.builder()
						.domainId(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"))
						.enabled(true)
						.schedulingConfiguration(SchedulingConfiguration.builder()
								.recurrence(ArchiveRecurrence.daily())
								.time(LocalTime.parse("13:23"))
								.build())
						.excludedFolder("Excluded")
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = imapArchiveProcessing.listImapFolders(processedTask);
		control.verify();
		assertThat(listImapFolders).containsOnly(FluentIterable.from(expectedListInfos).toArray(ListInfo.class));
	}
	
	@Test
	public void listImapFoldersShouldFilterProcessedFoldersWhenContinuePreviousArchiveTreatment() throws Exception {
		List<ListInfo> expectedListInfos = ImmutableList.of(
				new ListInfo("user/usera/SPAM@mydomain.org", true, false),
				new ListInfo("user/usera/Sent@mydomain.org", true, false),
				new ListInfo("user/usera/Excluded@mydomain.org", true, false),
				new ListInfo("user/usera/Excluded/subfolder@mydomain.org", true, false));
		ListResult listResult = new ListResult(6);
		listResult.addAll(expectedListInfos);
		listResult.add(new ListInfo("user/usera@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/Drafts@mydomain.org", true, false));
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll())
			.andReturn(listResult);
		storeClient.close();
		expectLastCall();
		
		ObmDomain domain = ObmDomain.builder().name("mydomain.org").build();
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7");
		ArchiveDomainTask archiveDomainTask = control.createMock(ArchiveDomainTask.class);
		expect(archiveDomainTask.getLogger())
			.andReturn(logger);
		expect(archiveDomainTask.getRunId())
			.andReturn(runId);
		
		expect(processedFolderDao.get(runId, ImapFolder.from("user/usera@mydomain.org")))
			.andReturn(Optional.<ProcessedFolder> of(ProcessedFolder.builder()
					.runId(runId)
					.folder(ImapFolder.from("user/usera@mydomain.org"))
					.uidNext(5)
					.start(DateTime.parse("2014-05-26T08:46:00.000Z"))
					.end(DateTime.parse("2014-05-26T08:46:01.000Z"))
					.build()));
		expect(processedFolderDao.get(runId, ImapFolder.from("user/usera/Drafts@mydomain.org")))
			.andReturn(Optional.<ProcessedFolder> of(ProcessedFolder.builder()
					.runId(runId)
					.folder(ImapFolder.from("user/usera/Drafts@mydomain.org"))
					.uidNext(2)
					.start(DateTime.parse("2014-05-26T08:46:02.000Z"))
					.end(DateTime.parse("2014-05-26T08:46:04.000Z"))
					.build()));
		expect(processedFolderDao.get(runId, ImapFolder.from("user/usera/SPAM@mydomain.org")))
			.andReturn(Optional.<ProcessedFolder> absent());
		expect(processedFolderDao.get(runId, ImapFolder.from("user/usera/Sent@mydomain.org")))
			.andReturn(Optional.<ProcessedFolder> absent());
		expect(processedFolderDao.get(runId, ImapFolder.from("user/usera/Excluded@mydomain.org")))
			.andReturn(Optional.<ProcessedFolder> absent());
		expect(processedFolderDao.get(runId, ImapFolder.from("user/usera/Excluded/subfolder@mydomain.org")))
			.andReturn(Optional.<ProcessedFolder> absent());
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveDomainTask(archiveDomainTask)
				.domain(domain)
				.boundaries(Boundaries.builder()
						.lowerBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.domainConfiguration(DomainConfiguration.builder()
						.domainId(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"))
						.enabled(true)
						.schedulingConfiguration(SchedulingConfiguration.builder()
								.recurrence(ArchiveRecurrence.daily())
								.time(LocalTime.parse("13:23"))
								.build())
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> of(ArchiveTreatment.builder(ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6"))
						.runId(runId)
						.scheduledAt(DateTime.parse("2014-08-26T08:46:00.000Z"))
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.status(ArchiveStatus.ERROR)
						.build()))
				.continuePrevious(true)
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
		listResult.add(new ListInfo("user/usera/" + ArchiveMailbox.ARCHIVE_MAIN_FOLDER + "/Excluded@mydomain.org", true, false));
		listResult.add(new ListInfo("user/usera/" + ArchiveMailbox.ARCHIVE_MAIN_FOLDER + "/Excluded/subfolder@mydomain.org", true, false));
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		storeClient.login(false);
		expectLastCall();
		expect(storeClient.listAll())
			.andReturn(listResult);
		storeClient.close();
		expectLastCall();
		
		ObmDomain domain = ObmDomain.builder().name("mydomain.org").build();
		expect(storeClientFactory.create(domain.getName()))
			.andReturn(storeClient);
		
		ArchiveDomainTask archiveDomainTask = control.createMock(ArchiveDomainTask.class);
		expect(archiveDomainTask.getLogger())
			.andReturn(logger);
		expect(archiveDomainTask.getRunId())
			.andReturn(ArchiveTreatmentRunId.from("259ef5d1-9dfd-4fdb-84b0-09d33deba1b7"));
		
		control.replay();
		ProcessedTask processedTask = ProcessedTask.builder()
				.archiveDomainTask(archiveDomainTask)
				.domain(domain)
				.boundaries(Boundaries.builder()
						.lowerBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
						.higherBoundary(DateTime.parse("2014-07-26T08:46:00.000Z"))
						.build())
				.domainConfiguration(DomainConfiguration.builder()
						.domainId(ObmDomainUuid.of("e953d0ab-7053-4f84-b83a-abfe479d3888"))
						.enabled(true)
						.schedulingConfiguration(SchedulingConfiguration.builder()
								.recurrence(ArchiveRecurrence.daily())
								.time(LocalTime.parse("13:23"))
								.build())
						.build())
				.previousArchiveTreatment(Optional.<ArchiveTreatment> absent())
				.build();
		
		ImmutableList<ListInfo> listImapFolders = imapArchiveProcessing.listImapFolders(processedTask);
		control.verify();
		assertThat(listImapFolders).containsOnly(FluentIterable.from(expectedListInfos).toArray(ListInfo.class));
	}
	
	@Test
	public void searchMailUidsWhenNoPreviousArchiveTreatment() throws Exception {
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
		FluentIterable<Long> searchMailUids = imapArchiveProcessing.searchMailUids(Mailbox.from("mailbox", logger, storeClient), boundaries, Optional.<ArchiveTreatment> absent());
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
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77");
		ArchiveTreatment archiveTreatment = ArchiveTreatment.builder(ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6"))
				.runId(runId)
				.scheduledAt(DateTime.parse("2014-08-26T08:46:00.000Z"))
				.higherBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		String mailbox = "mailbox";
		ImapFolder imapFolder = ImapFolder.from(mailbox);
		expect(processedFolderDao.get(runId, imapFolder))
			.andReturn(Optional.fromNullable(ProcessedFolder.builder()
					.runId(ArchiveTreatmentRunId.from("1d7b24df-cdb1-42f2-b3e1-2c1238b2c071"))
					.folder(imapFolder)
					.uidNext(2)
					.start(DateTime.parse("2014-05-26T08:46:00.000Z"))
					.end(DateTime.parse("2014-05-26T08:46:01.000Z"))
					.build()));
		
		control.replay();
		FluentIterable<Long> searchMailUids = imapArchiveProcessing.searchMailUids(Mailbox.from("mailbox", logger, storeClient), boundaries, Optional.fromNullable(archiveTreatment));
		control.verify();
		assertThat(searchMailUids).containsOnly(5l, 6l, 7l, 8l, 9l, 10l, 11l, 12l);
	}
	
	@Test
	public void searchMailUidsShouldReturnFilteredUidsWhenPreviousUidnextIsInRange() throws Exception {
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
		
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77");
		ArchiveTreatment archiveTreatment = ArchiveTreatment.builder(ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6"))
				.runId(runId)
				.scheduledAt(DateTime.parse("2014-08-26T08:46:00.000Z"))
				.higherBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		String mailbox = "mailbox";
		ImapFolder imapFolder = ImapFolder.from(mailbox);
		expect(processedFolderDao.get(runId, imapFolder))
			.andReturn(Optional.fromNullable(ProcessedFolder.builder()
					.runId(ArchiveTreatmentRunId.from("1d7b24df-cdb1-42f2-b3e1-2c1238b2c071"))
					.folder(imapFolder)
					.uidNext(6)
					.start(DateTime.parse("2014-05-26T08:46:00.000Z"))
					.end(DateTime.parse("2014-05-26T08:46:01.000Z"))
					.build()));
		
		control.replay();
		FluentIterable<Long> searchMailUids = imapArchiveProcessing.searchMailUids(Mailbox.from("mailbox", logger, storeClient), boundaries, Optional.fromNullable(archiveTreatment));
		control.verify();
		assertThat(searchMailUids).containsOnly(6l, 7l, 8l, 9l, 10l, 11l, 12l);
	}
	
	@Test
	public void previousUidnextShouldReturnAbsentWhenNoPreviousArchiveTreatment() throws Exception {
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		control.replay();
		Optional<Long> previousUidnext = imapArchiveProcessing.previousUidnext(Mailbox.from("mailbox", logger, storeClient), Optional.<ArchiveTreatment> absent());
		control.verify();
		assertThat(previousUidnext).isAbsent();
	}
	
	@Test
	public void previousUidnextShouldReturnAbsentWhenNoPreviousProcessedFolder() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77");
		ArchiveTreatment archiveTreatment = ArchiveTreatment.builder(ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6"))
				.runId(runId)
				.scheduledAt(DateTime.parse("2014-08-26T08:46:00.000Z"))
				.higherBoundary(DateTime.parse("2014-06-26T08:46:00.000Z"))
				.status(ArchiveStatus.SUCCESS)
				.build();
		
		String mailbox = "mailbox";
		expect(processedFolderDao.get(runId, ImapFolder.from(mailbox)))
			.andReturn(Optional.<ProcessedFolder> absent());
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		control.replay();
		Optional<Long> previousUidnext = imapArchiveProcessing.previousUidnext(Mailbox.from("mailbox", logger, storeClient), Optional.fromNullable(archiveTreatment));
		control.verify();
		assertThat(previousUidnext).isAbsent();
	}
	
	@Test
	public void previousUidnextShouldReturnPreviousUidnext() throws Exception {
		ArchiveTreatmentRunId runId = ArchiveTreatmentRunId.from("ae7e9726-4d00-4259-a89e-2dbdb7b65a77");
		ArchiveTreatment archiveTreatment = ArchiveTreatment.builder(ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6"))
				.runId(runId)
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
					.uidNext(expectedUidnext)
					.start(DateTime.parse("2014-05-26T08:46:00.000Z"))
					.end(DateTime.parse("2014-05-26T08:46:01.000Z"))
					.build()));
		
		StoreClient storeClient = control.createMock(StoreClient.class);
		
		control.replay();
		Optional<Long> previousUidnext = imapArchiveProcessing.previousUidnext(Mailbox.from(mailbox, logger, storeClient), Optional.fromNullable(archiveTreatment));
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
			.status(ArchiveStatus.ERROR)
			.scheduledAt(DateTime.now())
			.higherBoundary(previousHigherBoundary)
			.build());
		
		control.replay();
		boolean continuePrevious = imapArchiveProcessing.continuePrevious(previousArchiveTreatment, previousHigherBoundary);
		control.verify();
		assertThat(continuePrevious).isTrue();
	}
}
