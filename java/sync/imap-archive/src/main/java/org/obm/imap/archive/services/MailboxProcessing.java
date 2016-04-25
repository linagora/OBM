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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.obm.annotations.transactional.Transactional;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.HigherBoundary;
import org.obm.imap.archive.beans.ImapFolder;
import org.obm.imap.archive.beans.MappedMessageSets;
import org.obm.imap.archive.beans.ProcessedFolder;
import org.obm.imap.archive.beans.Year;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationService;
import org.obm.imap.archive.dao.ProcessedFolderDao;
import org.obm.imap.archive.exception.ImapAnnotationException;
import org.obm.imap.archive.exception.ImapCreateException;
import org.obm.imap.archive.exception.ImapQuotaException;
import org.obm.imap.archive.exception.ImapSelectException;
import org.obm.imap.archive.exception.ImapSetAclException;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.imap.archive.mailbox.ArchiveMailbox;
import org.obm.imap.archive.mailbox.CreatableMailbox;
import org.obm.imap.archive.mailbox.Mailbox;
import org.obm.imap.archive.mailbox.TemporaryMailbox;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.InternalDate;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.mail.imap.IMAPException;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.system.ObmSystemUser;

@Singleton
public class MailboxProcessing {
	
	protected static final Flag IMAP_ARCHIVE_FLAG = Flag.from("ImapArchive");

	private final DateTimeProvider dateTimeProvider;
	protected final ProcessedFolderDao processedFolderDao;
	protected final ImapArchiveConfigurationService imapArchiveConfigurationService;

	
	@Inject
	@VisibleForTesting MailboxProcessing(DateTimeProvider dateTimeProvider,
			ProcessedFolderDao processedFolderDao,
			ImapArchiveConfigurationService imapArchiveConfigurationService) {
		
		this.dateTimeProvider = dateTimeProvider;
		this.processedFolderDao = processedFolderDao;
		this.imapArchiveConfigurationService = imapArchiveConfigurationService;
	}

	public void processMailbox(Mailbox mailbox, ProcessedTask processedTask) throws Exception {
		ProcessedFolder.Builder processedFolder = ProcessedFolder.builder()
				.runId(processedTask.getRunId())
				.folder(ImapFolder.from(mailbox.getName()))
				.start(dateTimeProvider.now());
		
		try (StoreClient storeClient = mailbox.getStoreClient()) {
			storeClient.login(false);
			
			Logger logger = processedTask.getLogger();
			
			grantRightsWhenNotSelectable(mailbox);
			
			MessageSet mailUids = searchMailUids(mailbox, processedTask.getHigherBoundary());
			if (!mailUids.isEmpty()) {
				logger.info("Processing: {}", mailbox.getName());
				logger.info("{} mails will be archived, from UID {} to {}", mailUids.size(), mailUids.first().get(), mailUids.max());
				
				processingImapCopy(mailbox, mailUids, processedTask);
				processingImapMove(mailbox, mailUids, processedTask);
			}
			processedFolder.status(ArchiveStatus.SUCCESS);
		} finally {
			folderProcessed(processedFolder);
		}
	}

	protected void processingImapMove(Mailbox mailbox, MessageSet mailUids, ProcessedTask processedTask) 
			throws ImapSelectException, MailboxNotFoundException {
		
		Logger logger = processedTask.getLogger();
		
		if (!processedTask.domainConfiguration.isMoveEnabled()) {
			logger.debug("Moving archived emails is disabled");
			return;
		}

		logger.debug("Moving archived emails is enabled");
		mailbox.select();
		logger.debug("Set deleted flag on %s", mailUids.toString());
		mailbox.getStoreClient().uidStore(mailUids, new FlagsList(ImmutableList.of(Flag.DELETED)), true);
	}
	
	protected void processingImapCopy(Mailbox mailbox, MessageSet mailUids, ProcessedTask processedTask) 
			throws IMAPException, MailboxFormatException, MailboxNotFoundException {
		
		DomainName domainName = new DomainName(processedTask.getDomain().getName());
		Logger logger = processedTask.getLogger();
		
		TemporaryMailbox temporaryMailbox = TemporaryMailbox.builder()
				.from(mailbox)
				.domainName(domainName)
				.cyrusPartitionSuffix(imapArchiveConfigurationService.getCyrusPartitionSuffix())
				.build();
		try {
			MessageSet copiedMessageSet = copyToTemporary(mailbox, temporaryMailbox, logger, mailUids);
			MappedMessageSets mappedMessageSets = MappedMessageSets.builder().origin(mailUids).destination(copiedMessageSet).build();
			
			batchCopyFromTemporaryToArchive(mailbox, temporaryMailbox, mappedMessageSets, processedTask);
	
		} finally {
			try {
				temporaryMailbox.delete();
			} catch (Exception e) {
				logger.warn(String.format("Wasn't able to delete temporary mailbox %s", temporaryMailbox.getName()), e);
			}
		}
	}

	private MessageSet copyToTemporary(Mailbox mailbox, TemporaryMailbox temporaryMailbox, Logger logger, MessageSet messageSet) throws IMAPException, MailboxNotFoundException {
		createFolder(temporaryMailbox, logger);
		
		mailbox.select();
		return mailbox.uidCopy(messageSet, temporaryMailbox);
	}

	private void batchCopyFromTemporaryToArchive(Mailbox mailbox, TemporaryMailbox temporaryMailbox, MappedMessageSets mappedMessageSets, ProcessedTask processedTask) 
			throws MailboxNotFoundException, IMAPException, MailboxFormatException {

		for (MessageSet partitionMessageSet : mappedMessageSets.getOrigin().partition(imapArchiveConfigurationService.getProcessingBatchSize())) {
			Map<Year, MessageSet> mappedByYear = mapByYear(mailbox, partitionMessageSet);
			for (Map.Entry<Year, MessageSet> entry : mappedByYear.entrySet()) {
				ArchiveMailbox archiveMailbox = createArchiveMailbox(mailbox, entry.getKey(), processedTask);
				
				MessageSet originUids = entry.getValue();
				MessageSet yearMessageSet = mappedMessageSets.getDestinationUidFor(originUids);
				copyTemporaryMessagesToArchive(temporaryMailbox, yearMessageSet, archiveMailbox, processedTask.getLogger());
				if (!originUids.isEmpty() && !processedTask.getDomainConfiguration().isMoveEnabled()) {
					addArchiveFlag(mailbox, originUids);
				}
			}
		}
	}

	private void copyTemporaryMessagesToArchive(TemporaryMailbox temporaryMailbox, 
			MessageSet partitionMessageSet, ArchiveMailbox archiveMailbox, Logger logger) 
					throws MailboxNotFoundException, ImapSelectException, IMAPException {
		
		if (partitionMessageSet.isEmpty()) {
			logger.warn("Empty messageSet for mailbox: {}", archiveMailbox.getName());
			return;
		}
		temporaryMailbox.select();
		MessageSet copiedMessageSet = temporaryMailbox.uidCopy(partitionMessageSet, archiveMailbox);
		addSeenFlags(archiveMailbox, copiedMessageSet);
	}

	private Map<Year, MessageSet> mapByYear(Mailbox mailbox, MessageSet messageSet) throws ImapSelectException, MailboxNotFoundException {
		if (messageSet.isEmpty()) {
			return ImmutableMap.of();
		}
		
		mailbox.select();
		Year year = guessRangeYear(mailbox, messageSet);
		MessageSet otherYears = searchOutOfYearMessages(mailbox, messageSet, year);
		
		HashMap<Year, MessageSet> messageSetsByYear = Maps.newHashMap(
			Maps.transformValues(
				Multimaps
					.index(mailbox.fetchInternalDate(otherYears), new YearFromInternalDate())
					.asMap(),
					new CreateMessageSet<>()));
		messageSetsByYear.put(year, messageSet.remove(otherYears));
		return messageSetsByYear;
	}

	private static class CreateMessageSet<T extends Collection<InternalDate>> implements Function<T, MessageSet> {
		@Override
		public MessageSet apply(T input) {
			MessageSet.Builder messageSetBuilder = MessageSet.builder();
			for (InternalDate internalDate: input) {
				messageSetBuilder.add(internalDate.getUid());
			}
			return messageSetBuilder.build();
		}
	}
	
	private static class YearFromInternalDate implements Function<InternalDate, Year> {
		@Override
		public Year apply(InternalDate input) {
			return Year.from(input);
		}
	}
	
	private MessageSet searchOutOfYearMessages(Mailbox mailbox, MessageSet messageSet, Year year) {
		return mailbox.uidSearch(SearchQuery.builder()
				.between(true)
				.beforeExclusive(year.toDate())
				.afterInclusive(year.next().toDate())
				.messageSet(messageSet)
				.build());
	}

	private Year guessRangeYear(Mailbox mailbox, MessageSet messageSet) {
		return Year
				.from(
					FluentIterable.from(
							mailbox.fetchInternalDate(
									MessageSet.singleton(messageSet.first().get())))
				.first().get());
	}

	private ArchiveMailbox createArchiveMailbox(Mailbox mailbox, Year year, ProcessedTask processedTask) 
			throws MailboxFormatException, MailboxNotFoundException, ImapSelectException, ImapSetAclException, ImapCreateException, ImapQuotaException, ImapAnnotationException {
		
		DomainName domainName = new DomainName(processedTask.getDomain().getName());
		Logger logger = processedTask.getLogger();

		ArchiveMailbox archiveMailbox = ArchiveMailbox.builder()
				.from(mailbox) 
				.year(year)
				.domainName(domainName)
				.archiveMainFolder(processedTask.getDomainConfiguration().getArchiveMainFolder())
				.cyrusPartitionSuffix(imapArchiveConfigurationService.getCyrusPartitionSuffix())
				.build();
		createFolder(archiveMailbox, logger);
		return archiveMailbox;
	}

	private void addSeenFlags(ArchiveMailbox archiveMailbox, MessageSet messageSet) throws MailboxNotFoundException, IMAPException {
		if (!messageSet.isEmpty()) {
			archiveMailbox.select();
			archiveMailbox.uidStoreSeen(messageSet);
		}
	}

	private void addArchiveFlag(Mailbox mailbox, MessageSet messageSet) throws ImapSelectException, MailboxNotFoundException {
		if (!messageSet.isEmpty()) {
			mailbox.select();
			mailbox.uidStore(messageSet, IMAP_ARCHIVE_FLAG, true);
		}
	}


	private void grantRightsWhenNotSelectable(Mailbox mailbox) 
			throws MailboxNotFoundException, ImapSelectException, ImapSetAclException {
		
		try {
			mailbox.select();
		} catch (ImapSelectException e) {
			mailbox.grantAllRightsTo(ObmSystemUser.CYRUS);
			mailbox.select();
		}
	}

	protected void createFolder(CreatableMailbox creatableMailbox, Logger logger) 
			throws MailboxNotFoundException, ImapSelectException, ImapSetAclException, ImapCreateException, ImapQuotaException, ImapAnnotationException {
		
		try {
			creatableMailbox.select();
		} catch (ImapSelectException e) {
			createMailbox(creatableMailbox, logger);
		}
	}
	
	@VisibleForTesting MessageSet searchMailUids(Mailbox mailbox, HigherBoundary higherBoundary) {
		return mailbox.uidSearch(SearchQuery.builder()
					.beforeExclusive(higherBoundary.getHigherBoundary().toDate())
					.includeDeleted(false)
					.unmatchingFlag(IMAP_ARCHIVE_FLAG)
					.build());
	}

	private void createMailbox(CreatableMailbox creatableMailbox, Logger logger) 
			throws MailboxNotFoundException, ImapSetAclException, ImapCreateException, ImapSelectException, ImapQuotaException, ImapAnnotationException {
		
		String archiveMailboxName = creatableMailbox.getName();
		logger.debug("Creating {} mailbox", archiveMailboxName);
		
		creatableMailbox.create();
		creatableMailbox.grantAllRightsTo(ObmSystemUser.CYRUS);
		creatableMailbox.grantReadRightsTo(creatableMailbox.getUserAtDomain());
		creatableMailbox.setMaxQuota(imapArchiveConfigurationService.getQuotaMaxSize());
		creatableMailbox.setSharedSeenAnnotation();
		creatableMailbox.select();
	}

	@Transactional
	protected void folderProcessed(ProcessedFolder.Builder processedFolder) throws DaoException {
		processedFolderDao.insert(processedFolder.end(dateTimeProvider.now()).build());
	}
}
