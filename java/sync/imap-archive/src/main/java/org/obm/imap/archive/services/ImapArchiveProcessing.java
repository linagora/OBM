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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.obm.annotations.transactional.Transactional;
import org.obm.imap.archive.beans.ArchiveConfiguration;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentKind;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.HigherBoundary;
import org.obm.imap.archive.beans.ImapFolder;
import org.obm.imap.archive.beans.Limit;
import org.obm.imap.archive.beans.MappedMessageSets;
import org.obm.imap.archive.beans.ProcessedFolder;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.beans.ScopeUser;
import org.obm.imap.archive.beans.Year;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationService;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.dao.ProcessedFolderDao;
import org.obm.imap.archive.exception.ImapAnnotationException;
import org.obm.imap.archive.exception.ImapArchiveProcessingException;
import org.obm.imap.archive.exception.ImapCreateException;
import org.obm.imap.archive.exception.ImapQuotaException;
import org.obm.imap.archive.exception.ImapSelectException;
import org.obm.imap.archive.exception.ImapSetAclException;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.imap.archive.mailbox.ArchiveMailbox;
import org.obm.imap.archive.mailbox.CreatableMailbox;
import org.obm.imap.archive.mailbox.Mailbox;
import org.obm.imap.archive.mailbox.MailboxImpl;
import org.obm.imap.archive.mailbox.MailboxPaths;
import org.obm.imap.archive.mailbox.TemporaryMailbox;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.UserNotFoundException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.Flag;
import org.obm.push.mail.bean.FlagsList;
import org.obm.push.mail.bean.InternalDate;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.mail.imap.IMAPException;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.system.ObmSystemUser;

@Singleton
public class ImapArchiveProcessing {
	
	@VisibleForTesting static final String GLOBAL_VIRT = "global.virt";
	protected static final String USERS_REFERENCE_NAME = "*user";
	protected static final String INBOX_MAILBOX_NAME = "/%";
	protected static final String ALL_MAILBOXES_NAME = "*";
	protected static final Flag IMAP_ARCHIVE_FLAG = Flag.from("ImapArchive");
	protected static final String ALL_USER_FOLDERS_BUT_INBOX = USERS_REFERENCE_NAME + "/%s/";

	private final DateTimeProvider dateTimeProvider;
	private final SchedulingDatesService schedulingDatesService;
	protected final StoreClientFactory storeClientFactory;
	protected final ArchiveTreatmentDao archiveTreatmentDao;
	protected final ProcessedFolderDao processedFolderDao;
	protected final ImapArchiveConfigurationService imapArchiveConfigurationService;

	
	@Inject
	@VisibleForTesting ImapArchiveProcessing(DateTimeProvider dateTimeProvider,
			SchedulingDatesService schedulingDatesService,
			StoreClientFactory storeClientFactory,
			ArchiveTreatmentDao archiveTreatmentDao,
			ProcessedFolderDao processedFolderDao,
			ImapArchiveConfigurationService imapArchiveConfigurationService) {
		
		this.dateTimeProvider = dateTimeProvider;
		this.schedulingDatesService = schedulingDatesService;
		this.storeClientFactory = storeClientFactory;
		this.archiveTreatmentDao = archiveTreatmentDao;
		this.processedFolderDao = processedFolderDao;
		this.imapArchiveConfigurationService = imapArchiveConfigurationService;
	}
	
	public void archive(ArchiveConfiguration configuration) {
		Logger logger = configuration.getLogger();
		try {
			Optional<ArchiveTreatment> previousArchiveTreatment = previousArchiveTreatment(configuration.getDomainId());
			HigherBoundary higherBoundary = calculateHigherBoundary(dateTimeProvider.now(), configuration.getConfiguration().getRepeatKind(), previousArchiveTreatment, logger);
			ProcessedTask.Builder processedTaskBuilder = ProcessedTask.builder()
					.archiveConfiguration(configuration)
					.previousArchiveTreatment(previousArchiveTreatment)
					.higherBoundary(higherBoundary)
					.continuePrevious(continuePrevious(previousArchiveTreatment, higherBoundary.getHigherBoundary()));
			
			if (!run(processedTaskBuilder.build())) {
				throw new ImapArchiveProcessingException();
			}
			
			logger.info("End of IMAP Archive for domain {}", configuration.getDomain().getName());
		} catch (Exception e) {
			logger.error("Error on archive treatment: ", e);
			Throwables.propagate(e);
		}
	}

	protected void logStart(Logger logger, ObmDomain domain) {
		logger.info("Starting IMAP Archive in {} for domain {}", ArchiveTreatmentKind.REAL_RUN, domain.getName());
	}
	
	@VisibleForTesting Optional<ArchiveTreatment> previousArchiveTreatment(ObmDomainUuid domainId) throws DaoException {
		return FluentIterable.from(archiveTreatmentDao.findLastTerminated(domainId, Limit.from(1))).first();
	}
	
	@VisibleForTesting HigherBoundary calculateHigherBoundary(DateTime start, RepeatKind repeatKind, Optional<ArchiveTreatment> previousArchiveTreatment, Logger archiveLogger) {
		HigherBoundary.Builder builder = HigherBoundary.builder();
		if (previousArchiveTreatment.isPresent()) {
			ArchiveTreatment lastArchiveTreatment = previousArchiveTreatment.get();
			if (lastArchiveTreatment.getArchiveStatus() == ArchiveStatus.ERROR) {
				builder.higherBoundary(lastArchiveTreatment.getHigherBoundary());
			} else {
				builder.higherBoundary(schedulingDatesService.higherBoundary(start, repeatKind));
			}
		} else {
			builder.higherBoundary(schedulingDatesService.higherBoundary(start, repeatKind));
		}
		HigherBoundary higherBoundary = builder.build();
		archiveLogger.debug("HigherBoundary: upto {}", higherBoundary.getHigherBoundary().toString("YYYY-MM-dd"));
		return higherBoundary;
	}

	private boolean run(ProcessedTask processedTask) throws Exception {
		boolean isSuccess = true;
		Logger logger = processedTask.getLogger();

		logStart(logger, processedTask.getDomain());
		for (ListInfo inboxListInfo : listUsers(processedTask)) {
			Optional<String> maybeUserName = inboxListInfo.getUserName();

			if (!maybeUserName.isPresent()) {
				continue;
			}

			String user = maybeUserName.get();

			try {
				if (!processUser(inboxListInfo, user, processedTask)) {
					isSuccess = false;
				}
			} catch (UserNotFoundException e) {
				logger.warn("User {} not found in OBM database", user);
			}
		}

		return isSuccess;
	}
	
	private boolean processUser(ListInfo inboxListInfo, String user, ProcessedTask processedTask) throws Exception {
		boolean isSuccess = true;
		Logger logger = processedTask.getLogger();
		for (ListInfo listInfo : listImapFolders(inboxListInfo, user, processedTask)) {
			try {
				processMailbox(MailboxImpl.from(listInfo.getName(), logger, storeClientFactory.createOnUserBackend(user, processedTask.getDomain())), 
						processedTask);
			} catch (Exception e) {
				logger.error("Error on archive treatment: ", e);
				isSuccess = false;
			}
		}
		return isSuccess;
	}

	protected void processMailbox(Mailbox mailbox, ProcessedTask processedTask) throws Exception {
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
	
	@VisibleForTesting ImmutableList<ListInfo> listUsers(final ProcessedTask processedTask) throws Exception {
		ObmDomain domain = processedTask.getDomain();
		try (StoreClient storeClient = storeClientFactory.create(domain.getName())) {
			storeClient.login(false);
			
			return FluentIterable.from(storeClient.listAll(USERS_REFERENCE_NAME, INBOX_MAILBOX_NAME))
					.transform(appendDomainWhenNone(domain))
					.filter(filterDomain(domain, processedTask.getLogger()))
					.filter(filterScopeUsers(processedTask))
					.toList();
		}
	}

	@VisibleForTesting ImmutableList<ListInfo> listImapFolders(ListInfo inboxListInfo, final String user, final ProcessedTask processedTask) throws Exception {
		ObmDomain domain = processedTask.getDomain();
		try (StoreClient storeClient = storeClientFactory.createOnUserBackend(user, domain)) {
			storeClient.login(false);

			return FluentIterable.from(
					Iterables.concat(
						Collections.singleton(inboxListInfo),
						storeClient.listAll(String.format(ALL_USER_FOLDERS_BUT_INBOX, user), ALL_MAILBOXES_NAME)))
					.transform(appendDomainWhenNone(domain))
					.filter(filterExcludedFolder(processedTask))
					.filter(filterFolders(processedTask, processedTask.getDomainConfiguration().getArchiveMainFolder(), TemporaryMailbox.TEMPORARY_FOLDER))
					.filter(filterNoSelected(processedTask))
					.toList();
		}
	}

	protected Function<ListInfo, ListInfo> appendDomainWhenNone(ObmDomain domain) {
		final String domainName = domain.getName();
		return new Function<ListInfo, ListInfo>() {

			@Override
			public ListInfo apply(ListInfo listInfo) {
				if (!hasDomain(listInfo)) {
					return appendDomainToListInfo(listInfo, domainName);
				}
				return listInfo;
			}

			private boolean hasDomain(ListInfo listInfo) {
				return listInfo.getName().contains(MailboxPaths.AT);
			}

			private ListInfo appendDomainToListInfo(ListInfo listInfo, String domainName) {
				return new ListInfo(
						new StringBuilder()
							.append(listInfo.getName())
							.append(MailboxPaths.AT)
							.append(domainName)
							.toString(), 
						listInfo.isSelectable(), listInfo.canCreateSubfolder());
			}
		};
	}

	protected Predicate<ListInfo> filterDomain(ObmDomain domain, final Logger logger) {
		final DomainName domainName = new DomainName(domain.getName());
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				try {
					MailboxPaths mailboxPaths = MailboxPaths.from(listInfo.getName());
					if (mailboxPaths.belongsTo(domainName)) {
						return true;
					}
				} catch (MailboxFormatException e) {
					logger.error(String.format("The mailbox %s can't be parsed", listInfo.getName()));
				}
				return false;
			}
		};
	}

	private Predicate<ListInfo> filterExcludedFolder(final ProcessedTask processedTask) {
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				if (listInfo.getName().contains("/" + processedTask.getDomainConfiguration().getExcludedFolder() + "/") || 
						listInfo.getName().contains("/" + processedTask.getDomainConfiguration().getExcludedFolder() + "@")) {
					return false;
				}
				return true;
			}
		};
	}

	private Predicate<? super ListInfo> filterScopeUsers(ProcessedTask processedTask) {
		final List<String> scopeUserLogins = scopeUserLogins(processedTask.getDomainConfiguration());
		final boolean scopeIncludes = processedTask.getDomainConfiguration().isScopeUsersIncludes();
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				for (String scopeUserLogin : scopeUserLogins) {
					if (listInfo.getName().startsWith("user/" + scopeUserLogin + "/") || 
							listInfo.getName().startsWith("user/" + scopeUserLogin + "@")) {
						return scopeIncludes;
					}
				}
				return !scopeIncludes;
			}
		};
	}

	private List<String> scopeUserLogins(DomainConfiguration domainConfiguration) {
		return FluentIterable.from(domainConfiguration.getScopeUsers())
				.transform(new Function<ScopeUser, String>() {

					@Override
					public String apply(ScopeUser scopeUser) {
						return scopeUser.getLogin();
					}
				}).toList();
	}

	private Predicate<ListInfo> filterFolders(ProcessedTask processedTask, final String...folders) {
		final Logger logger = processedTask.getLogger();
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				try {
					MailboxPaths mailboxPaths = MailboxPaths.from(listInfo.getName());
					for (String folder : folders) {
						if (mailboxPaths.getSubPaths().startsWith(folder + MailboxPaths.IMAP_FOLDER_SEPARATOR)) {
							return false;
						}
					}
				} catch (MailboxFormatException e) {
					logger.error(String.format("The mailbox %s can't be parsed", listInfo.getName()));
					return false;
				}
				return true;
			}
		};
	}

	private Predicate<ListInfo> filterNoSelected(ProcessedTask processedTask) {
		final Logger logger = processedTask.getLogger();
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				if (!listInfo.isSelectable()) {
					logger.info(String.format("The mailbox %s can't be selected (\\Noselect flag)", listInfo.getName()));
					return false;
				}
				return true;
			}
		};
	}


	@VisibleForTesting MessageSet searchMailUids(Mailbox mailbox, HigherBoundary higherBoundary) {
		return mailbox.uidSearch(SearchQuery.builder()
					.beforeExclusive(higherBoundary.getHigherBoundary().toDate())
					.includeDeleted(false)
					.unmatchingFlag(IMAP_ARCHIVE_FLAG)
					.build());
	}

	@VisibleForTesting boolean continuePrevious(Optional<ArchiveTreatment> previousArchiveTreatment, DateTime higherBoundary) {
		return previousArchiveTreatment.isPresent() && previousArchiveTreatment.get().getArchiveStatus() != ArchiveStatus.SUCCESS
				&& previousArchiveTreatment.get().getHigherBoundary().equals(higherBoundary);
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
	
	@VisibleForTesting static class ProcessedTask {

		public static Builder builder() {
			return new Builder();
		}
		
		public static class Builder {
			
			private ArchiveConfiguration archiveConfiguration;
			private HigherBoundary higherBoundary;
			private Optional<ArchiveTreatment> previousArchiveTreatment;
			private boolean continuePrevious;
			
			private Builder() {}

			public Builder archiveConfiguration(ArchiveConfiguration archiveConfiguration) {
				this.archiveConfiguration = archiveConfiguration;
				return this;
			}
			
			public Builder higherBoundary(HigherBoundary higherBoundary) {
				this.higherBoundary = higherBoundary;
				return this;
			}
			
			public Builder previousArchiveTreatment(Optional<ArchiveTreatment> previousArchiveTreatment) {
				this.previousArchiveTreatment = previousArchiveTreatment;
				return this;
			}
			
			public Builder continuePrevious(boolean continuePrevious) {
				this.continuePrevious = continuePrevious;
				return this;
			}
			
			public ProcessedTask build() {
				Preconditions.checkState(archiveConfiguration != null);
				Preconditions.checkState(higherBoundary != null);
				Preconditions.checkState(previousArchiveTreatment != null);
				return new ProcessedTask(archiveConfiguration.getLogger(), archiveConfiguration.getRunId(),
					archiveConfiguration.getDomain(), higherBoundary, 
					archiveConfiguration.getConfiguration(), previousArchiveTreatment, continuePrevious);
			}
		}
		
		private final Logger logger;
		private final ArchiveTreatmentRunId runId;
		private final ObmDomain domain;
		private final HigherBoundary higherBoundary;
		private final DomainConfiguration domainConfiguration;
		private final Optional<ArchiveTreatment> previousArchiveTreatment;
		private final boolean continuePrevious;
		
		private ProcessedTask(Logger logger, ArchiveTreatmentRunId runId, ObmDomain domain, 
				HigherBoundary higherBoundary, DomainConfiguration domainConfiguration, 
				Optional<ArchiveTreatment> previousArchiveTreatment, boolean continuePrevious) {
			
			this.logger = logger;
			this.runId = runId;
			this.domain = domain;
			this.higherBoundary = higherBoundary;
			this.domainConfiguration = domainConfiguration;
			this.previousArchiveTreatment = previousArchiveTreatment;
			this.continuePrevious = continuePrevious;
		}
		
		public Logger getLogger() {
			return logger;
		}
		
		public ArchiveTreatmentRunId getRunId() {
			return runId;
		}
		
		public ObmDomain getDomain() {
			return domain;
		}

		public HigherBoundary getHigherBoundary() {
			return higherBoundary;
		}

		public DomainConfiguration getDomainConfiguration() {
			return domainConfiguration;
		}

		public Optional<ArchiveTreatment> getPreviousArchiveTreatment() {
			return previousArchiveTreatment;
		}
		
		public boolean continuePrevious() {
			return continuePrevious;
		}
	}
}
