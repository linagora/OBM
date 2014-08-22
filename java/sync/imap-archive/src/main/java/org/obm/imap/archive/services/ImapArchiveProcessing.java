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

import org.joda.time.DateTime;
import org.obm.annotations.transactional.Transactional;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.Boundaries;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.beans.ImapFolder;
import org.obm.imap.archive.beans.ProcessedFolder;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.beans.Year;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.dao.DomainConfigurationDao;
import org.obm.imap.archive.dao.ProcessedFolderDao;
import org.obm.imap.archive.exception.DomainConfigurationException;
import org.obm.imap.archive.exception.ImapArchiveProcessingException;
import org.obm.imap.archive.exception.ImapCreateException;
import org.obm.imap.archive.exception.ImapSelectException;
import org.obm.imap.archive.exception.ImapSetAclException;
import org.obm.imap.archive.scheduling.ArchiveDomainTask;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.SearchQuery;
import org.obm.push.minig.imap.StoreClient;
import org.obm.sync.base.DomainName;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.system.ObmSystemUser;

@Singleton
public class ImapArchiveProcessing {
	
	@VisibleForTesting static final String GLOBAL_VIRT = "global.virt";

	private final DateTimeProvider dateTimeProvider;
	private final SchedulingDatesService schedulingDatesService;
	private final StoreClientFactory storeClientFactory;
	private final DomainClient domainClient;
	private final ArchiveTreatmentDao archiveTreatmentDao;
	private final DomainConfigurationDao domainConfigurationDao;
	private final ProcessedFolderDao processedFolderDao;

	
	@Inject
	@VisibleForTesting ImapArchiveProcessing(DateTimeProvider dateTimeProvider,
			SchedulingDatesService schedulingDatesService,
			StoreClientFactory storeClientFactory,
			DomainClient domainClient,
			ArchiveTreatmentDao archiveTreatmentDao,
			DomainConfigurationDao domainConfigurationDao,
			ProcessedFolderDao processedFolderDao) {
		
		this.dateTimeProvider = dateTimeProvider;
		this.schedulingDatesService = schedulingDatesService;
		this.storeClientFactory = storeClientFactory;
		this.domainClient = domainClient;
		this.archiveTreatmentDao = archiveTreatmentDao;
		this.domainConfigurationDao = domainConfigurationDao;
		this.processedFolderDao = processedFolderDao;
	}
	
	@Transactional
	public void archive(ArchiveDomainTask archiveDomainTask) {
		Logger logger = archiveDomainTask.getLogger();
		try {
			Optional<ObmDomain> optionalDomain = domainClient.getById(archiveDomainTask.getDomain());
			if (!optionalDomain.isPresent()) {
				throw new DomainNotFoundException(archiveDomainTask.getDomain());
			}
			
			ObmDomain domain = optionalDomain.get();
			logger.info("Starting IMAP Archive for domain {}", domain.getName());

			DomainConfiguration domainConfiguration = getConfiguration(domain);
			Optional<ArchiveTreatment> previousArchiveTreatment = previousArchiveTreatment(domain.getUuid());
			Boundaries boundaries = calculateBoundaries(dateTimeProvider.now(), domainConfiguration.getRepeatKind(), previousArchiveTreatment, logger);
			ProcessedTask.Builder processedTaskBuilder = ProcessedTask.builder()
					.archiveDomainTask(archiveDomainTask)
					.domain(domain)
					.domainConfiguration(domainConfiguration)
					.previousArchiveTreatment(previousArchiveTreatment)
					.boundaries(boundaries)
					.continuePrevious(continuePrevious(previousArchiveTreatment, boundaries.getHigherBoundary()));
			
			if (!dryRun(processedTaskBuilder.build())) {
				throw new ImapArchiveProcessingException();
			}
			
			logger.info("End of IMAP Archive for domain {}", domain.getName());
		} catch (Exception e) {
			logger.error("Error on archive treatment: ", e);
			Throwables.propagate(e);
		}
	}

	@VisibleForTesting Optional<ArchiveTreatment> previousArchiveTreatment(ObmDomainUuid domainId) throws DaoException {
		return FluentIterable.from(archiveTreatmentDao.findLastTerminated(domainId, 1)).first();
	}

	private DomainConfiguration getConfiguration(ObmDomain domain) throws DaoException {
		DomainConfiguration domainConfiguration = domainConfigurationDao.get(domain.getUuid());
		if (domainConfiguration == null) {
			throw new DomainConfigurationException("The IMAP Archive configuration is not defined for the domain: '" + domain.getName() + "'");
		}
		if (!domainConfiguration.isEnabled()) {
			throw new DomainConfigurationException("The IMAP Archive service is disabled for the domain: '" + domain.getName() + "'");
		}
		return domainConfiguration;
	}
	
	@VisibleForTesting Boundaries calculateBoundaries(DateTime start, RepeatKind repeatKind, Optional<ArchiveTreatment> previousArchiveTreatment, Logger archiveLogger) {
		Boundaries.Builder builder = Boundaries.builder();
		if (previousArchiveTreatment.isPresent()) {
			ArchiveTreatment lastArchiveTreatment = previousArchiveTreatment.get();
			if (lastArchiveTreatment.getArchiveStatus() == ArchiveStatus.ERROR) {
				builder
					.lowerBoundary(schedulingDatesService.lowerBoundary(lastArchiveTreatment.getScheduledTime(), repeatKind))
					.higherBoundary(lastArchiveTreatment.getHigherBoundary());
			} else {
				builder
					.lowerBoundary(lastArchiveTreatment.getHigherBoundary())
					.higherBoundary(schedulingDatesService.higherBoundary(start, repeatKind));
			}
		} else {
			builder
				.firstSync()
				.higherBoundary(schedulingDatesService.higherBoundary(start, repeatKind));
		}
		Boundaries boundaries = builder.build();
		archiveLogger.debug("Boundaries: from {} to {}", boundaries.getLowerBoundary().toString("YYYY-MM-dd"), boundaries.getHigherBoundary().toString("YYYY-MM-dd"));
		return boundaries;
	}

	private boolean dryRun(ProcessedTask processedTask) throws Exception {
		boolean isSuccess = true;
		Logger logger = processedTask.getLogger();
		for (ListInfo listInfo : listImapFolders(processedTask)) {
			try {
				processMailbox(Mailbox.from(listInfo.getName(), logger, storeClientFactory.create(processedTask.getDomain().getName())), 
						processedTask);
			} catch (Exception e) {
				logger.error("Error on archive treatment: ", e);
				isSuccess = false;
			}
		}
		return isSuccess;
	}

	@Transactional
	private void processMailbox(Mailbox mailbox, ProcessedTask processedTask) throws Exception {
		try (StoreClient storeClient = mailbox.storeClient) {
			storeClient.login(false);
			
			Logger logger = processedTask.getLogger();
			logger.info("Processing: {}", mailbox.getName());
			
			grantRightsWhenNotSelectable(mailbox);
			
			ProcessedFolder.Builder processedFolder = ProcessedFolder.builder()
					.runId(processedTask.getRunId())
					.folder(ImapFolder.from(mailbox.getName()))
					.uidNext(mailbox.uidNext())
					.start(dateTimeProvider.now());
			
			FluentIterable<Long> mailUids = searchMailUids(mailbox, processedTask.getBoundaries(), processedTask.getPreviousArchiveTreatment());
			if (!mailUids.isEmpty()) {
				logger.info("{} mails will be archived, from UID {} to {}", mailUids.size(), mailUids.first().get(), mailUids.last().get());
	
				ArchiveMailbox archiveMailbox = ArchiveMailbox.from(mailbox, 
						Year.from(processedTask.getBoundaries().getHigherBoundary().year().get()), 
						new DomainName(processedTask.getDomain().getName())); 
				createFolder(archiveMailbox, logger);
				
				mailbox.select();
				logger.debug("Copying from {} mailbox to {} mailbox", mailbox, archiveMailbox);
				// uidCopy mailbox to temp
				// uidMove from temp to archive
				
				// archiveMailbox.select
				// uidStore Flag.SEEN
			}
			
			insertProcessedFolder(processedFolder);
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

	private void createFolder(ArchiveMailbox archiveMailbox, Logger logger) 
			throws MailboxNotFoundException, ImapSelectException, ImapSetAclException, ImapCreateException {
		
		try {
			archiveMailbox.select();
		} catch (ImapSelectException e) {
			createArchiveMailbox(archiveMailbox, logger);
		}
	}
	
	@VisibleForTesting ImmutableList<ListInfo> listImapFolders(final ProcessedTask processedTask) throws Exception {
		try (StoreClient storeClient = storeClientFactory.create(processedTask.getDomain().getName())) {
			storeClient.login(false);
			
			return FluentIterable.from(storeClient.listAll())
					.filter(filterExcludedFolder(processedTask))
					.filter(filterAlreadyProcessedFolder(processedTask))
					.filter(filterArchiveFolder())
					.toList();
		}
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

	private Predicate<ListInfo> filterAlreadyProcessedFolder(final ProcessedTask processedTask) {
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				if (processedTask.continuePrevious()) {
					try {
						if (hasBeenProcessedByPrevious(ImapFolder.from(listInfo.getName()), processedTask.getPreviousArchiveTreatment().get())) {
							return false;
						}
					} catch (DaoException e) {
						processedTask.getLogger().error("Error while getting ImapFolder", e);
						Throwables.propagate(e);
					}
				}
				return true;
			}

			private boolean hasBeenProcessedByPrevious(ImapFolder imapFolder, ArchiveTreatment previousArchiveTreatment) throws DaoException {
				return processedFolderDao.get(previousArchiveTreatment.getRunId(), imapFolder).isPresent();
			}
		};
	}

	private Predicate<ListInfo> filterArchiveFolder() {
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				if (listInfo.getName().contains(ArchiveMailbox.IMAP_FOLDER_SEPARATOR + ArchiveMailbox.ARCHIVE_MAIN_FOLDER + ArchiveMailbox.IMAP_FOLDER_SEPARATOR)) {
					return false;
				}
				return true;
			}
		};
	}

	@VisibleForTesting FluentIterable<Long> searchMailUids(Mailbox mailbox, Boundaries boundaries, Optional<ArchiveTreatment> previousArchiveTreatment) throws DaoException {
		final Optional<Long> previousUidnext = previousUidnext(mailbox, previousArchiveTreatment);
		
		return FluentIterable.from(
				mailbox.uidSearch(SearchQuery.builder()
					.after(boundaries.getLowerBoundary().toDate())
					.before(boundaries.getHigherBoundary().toDate())
					.build()))
				.filter(new Predicate<Long> () {

					@Override
					public boolean apply(Long input) {
						if (previousUidnext.isPresent() && input < previousUidnext.get()) {
							return false;
						}
						return true;
					}
				});
	}

	@VisibleForTesting Optional<Long> previousUidnext(Mailbox mailbox, Optional<ArchiveTreatment> previousArchiveTreatment) throws DaoException {
		if (previousArchiveTreatment.isPresent()) {
			Optional<ProcessedFolder> optionalProcessedFolder = processedFolderDao.get(previousArchiveTreatment.get().getRunId(), ImapFolder.from(mailbox.getName()));
			if (optionalProcessedFolder.isPresent()) {
				return Optional.of(optionalProcessedFolder.get().getUidNext());
			}
		}
		return Optional.absent();
	}

	@VisibleForTesting boolean continuePrevious(Optional<ArchiveTreatment> previousArchiveTreatment, DateTime higherBoundary) {
		return previousArchiveTreatment.isPresent() && previousArchiveTreatment.get().getHigherBoundary().equals(higherBoundary);
	}

	private void createArchiveMailbox(ArchiveMailbox archiveMailbox, Logger logger) 
			throws MailboxNotFoundException, ImapSetAclException, ImapCreateException, ImapSelectException {
		
		String archiveMailboxName = archiveMailbox.getName();
		logger.debug("Creating {} mailbox", archiveMailboxName);
		
		archiveMailbox.create();
		archiveMailbox.grantAllRightsTo(ObmSystemUser.CYRUS);
		archiveMailbox.grantReadRightsTo(archiveMailbox.getUserAtDomain());
		archiveMailbox.select();
	}

	@SuppressWarnings("unused")
	@VisibleForTesting void insertProcessedFolder(ProcessedFolder.Builder processedFolder) throws DaoException {
//		processedFolderDao.insert(processedFolder.end(dateTimeProvider.now()).build());
	}
	
	@VisibleForTesting static class ProcessedTask {

		public static Builder builder() {
			return new Builder();
		}
		
		public static class Builder {
			
			private ArchiveDomainTask archiveDomainTask;
			private ObmDomain domain;
			private Boundaries boundaries;
			private DomainConfiguration domainConfiguration;
			private Optional<ArchiveTreatment> previousArchiveTreatment;
			private boolean continuePrevious;
			
			private Builder() {}

			public Builder archiveDomainTask(ArchiveDomainTask archiveDomainTask) {
				this.archiveDomainTask = archiveDomainTask;
				return this;
			}
			
			public Builder domain(ObmDomain domain) {
				this.domain = domain;
				return this;
			}

			public Builder boundaries(Boundaries boundaries) {
				this.boundaries = boundaries;
				return this;
			}
			
			public Builder domainConfiguration(DomainConfiguration domainConfiguration) {
				this.domainConfiguration = domainConfiguration;
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
				Preconditions.checkState(archiveDomainTask != null);
				Preconditions.checkState(domain != null);
				Preconditions.checkState(boundaries != null);
				Preconditions.checkState(domainConfiguration != null);
				Preconditions.checkState(previousArchiveTreatment != null);
				return new ProcessedTask(archiveDomainTask.getLogger(), archiveDomainTask.getRunId(), domain, boundaries, domainConfiguration, previousArchiveTreatment, continuePrevious);
			}
		}
		
		private final Logger logger;
		private final ArchiveTreatmentRunId runId;
		private final ObmDomain domain;
		private final Boundaries boundaries;
		private final DomainConfiguration domainConfiguration;
		private final Optional<ArchiveTreatment> previousArchiveTreatment;
		private final boolean continuePrevious;
		
		private ProcessedTask(Logger logger, ArchiveTreatmentRunId runId, ObmDomain domain, Boundaries boundaries, DomainConfiguration domainConfiguration, Optional<ArchiveTreatment> previousArchiveTreatment, boolean continuePrevious) {
			this.logger = logger;
			this.runId = runId;
			this.domain = domain;
			this.boundaries = boundaries;
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

		public Boundaries getBoundaries() {
			return boundaries;
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
