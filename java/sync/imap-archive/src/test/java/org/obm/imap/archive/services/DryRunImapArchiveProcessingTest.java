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
import org.obm.imap.archive.beans.ArchiveConfiguration;
import org.obm.imap.archive.beans.ArchiveRecurrence;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.Limit;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.beans.SchedulingConfiguration;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.dao.ProcessedFolderDao;
import org.obm.imap.archive.logging.LoggerAppenders;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.ListResult;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.minig.imap.StoreClient;
import org.slf4j.LoggerFactory;

import pl.wkr.fluentrule.api.FluentExpectedException;
import ch.qos.logback.classic.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.system.ObmSystemUser;

public class DryRunImapArchiveProcessingTest {

	@Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
	@Rule public FluentExpectedException expectedException = FluentExpectedException.none();
	
	private IMocksControl control;
	private DateTimeProvider dateTimeProvider;
	private SchedulingDatesService schedulingDatesService;
	private StoreClientFactory storeClientFactory;
	private ArchiveTreatmentDao archiveTreatmentDao;
	private ProcessedFolderDao processedFolderDao;
	private Logger logger;
	private LoggerAppenders loggerAppenders;
	
	private DryRunImapArchiveProcessing imapArchiveProcessing;

	@Before
	public void setup() throws IOException {
		control = createControl();
		dateTimeProvider = control.createMock(DateTimeProvider.class);
		schedulingDatesService = control.createMock(SchedulingDatesService.class);
		storeClientFactory = control.createMock(StoreClientFactory.class);
		archiveTreatmentDao = control.createMock(ArchiveTreatmentDao.class);
		processedFolderDao = control.createMock(ProcessedFolderDao.class);
		logger = (Logger) LoggerFactory.getLogger(temporaryFolder.newFile().getAbsolutePath());
		loggerAppenders = control.createMock(LoggerAppenders.class);
		
		imapArchiveProcessing = new DryRunImapArchiveProcessing(dateTimeProvider, 
				schedulingDatesService, storeClientFactory, archiveTreatmentDao, processedFolderDao);
	}
	
	@Test
	public void archiveShouldWork() throws Exception {
		ObmDomainUuid domainId = ObmDomainUuid.of("fc2f915e-9df4-4560-b141-7b4c7ddecdd6");
		ObmDomain domain = ObmDomain.builder().uuid(domainId).name("mydomain.org").build();
		DomainConfiguration domainConfiguration = DomainConfiguration.builder()
				.domain(domain)
				.enabled(true)
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
		imapArchiveProcessing.archive(new ArchiveConfiguration(domainConfiguration, null, null, runId, logger, loggerAppenders, false));
		control.verify();
	}
	
	private void expectImapCommandsOnMailboxProcessing(String mailboxName, String archiveMailboxName, Range<Long> uids, DateTime higherBoundary, StoreClient storeClient) 
			throws Exception {
		
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
}
