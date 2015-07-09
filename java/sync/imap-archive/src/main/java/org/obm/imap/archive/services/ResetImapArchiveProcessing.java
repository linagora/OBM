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

import org.obm.annotations.transactional.Transactional;
import org.obm.imap.archive.beans.ArchiveConfiguration;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.configuration.ImapArchiveConfigurationService;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.dao.ProcessedFolderDao;
import org.obm.imap.archive.exception.ImapSelectException;
import org.obm.imap.archive.exception.MailboxFormatException;
import org.obm.imap.archive.exception.TestingModeRequiredException;
import org.obm.imap.archive.mailbox.DeletableMailbox;
import org.obm.imap.archive.mailbox.Mailbox;
import org.obm.imap.archive.mailbox.MailboxImpl;
import org.obm.imap.archive.mailbox.MailboxPaths;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.obm.push.exception.MailboxNotFoundException;
import org.obm.push.mail.bean.ListInfo;
import org.obm.push.mail.bean.MessageSet;
import org.obm.push.minig.imap.StoreClient;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class ResetImapArchiveProcessing extends ImapArchiveProcessing {
	
	private static final long UID_MIN = 0;
	private static final long UID_MAX = Long.MAX_VALUE;
	@VisibleForTesting static final MessageSet ALL = MessageSet.builder().add(Range.openClosed(UID_MIN, UID_MAX)).build();
	
	private final boolean testingMode;

	@Inject
	@VisibleForTesting ResetImapArchiveProcessing(DateTimeProvider dateTimeProvider,
			SchedulingDatesService schedulingDatesService,
			StoreClientFactory storeClientFactory,
			ArchiveTreatmentDao archiveTreatmentDao,
			ProcessedFolderDao processedFolderDao,
			ImapArchiveConfigurationService imapArchiveConfigurationService,
			@Named("testingMode") Boolean testingMode) {
		super(dateTimeProvider, schedulingDatesService, storeClientFactory, archiveTreatmentDao, processedFolderDao, imapArchiveConfigurationService);
		this.testingMode = testingMode;
	}

	@Override
	@Transactional
	public void archive(ArchiveConfiguration configuration) {
		Logger logger = configuration.getLogger();
		ObmDomain domain = configuration.getDomain();
		try {
			logger.info("Starting IMAP Archive reset for domain {}", domain.getName());
			checkTestingMode();
			
			resetDaos(domain);
			resetArchiveMailboxes(domain, configuration.getLogger(), configuration.getConfiguration());
			resetArchiveFlag(domain, configuration.getLogger(), configuration.getConfiguration());
		} catch (Exception e) {
			logger.error("Error on archive treatment: ", e);
			Throwables.propagate(e);
		} finally {
			logger.info("End of IMAP Archive reset for domain {}", domain.getName());
		}
	}

	private void checkTestingMode() {
		if (!testingMode) {
			throw new TestingModeRequiredException();
		}
	}

	private void resetDaos(ObmDomain domain) throws DaoException {
		archiveTreatmentDao.deleteAll(domain.getUuid());
	}

	private void resetArchiveMailboxes(ObmDomain domain, Logger logger, DomainConfiguration domainConfiguration) throws Exception {
		String domainName = domain.getName();
		for (ListInfo listInfo : listArchiveFolders(domain, logger, domainConfiguration)) {
			DeletableMailbox mailbox = DeletableMailbox.from(listInfo.getName(), logger, storeClientFactory.create(domainName));
			logger.info("Deleting: {}", mailbox.getName());
			delete(mailbox);
		}
	}

	private void delete(DeletableMailbox mailbox) throws Exception {
		try (StoreClient storeClient = mailbox.getStoreClient()) {
			storeClient.login(false);
			mailbox.delete();
		}
	}

	@VisibleForTesting ImmutableList<ListInfo> listArchiveFolders(ObmDomain domain, Logger logger, DomainConfiguration domainConfiguration) throws Exception {
		try (StoreClient storeClient = storeClientFactory.create(domain.getName())) {
			storeClient.login(false);
			
			return FluentIterable.from(storeClient.listAll(ImapArchiveProcessing.USERS_REFERENCE_NAME, ImapArchiveProcessing.ALL_MAILBOXES_NAME))
					.transform(appendDomainWhenNone(domain))
					.filter(filterDomain(domain, logger))
					.filter(filterArchiveFolder(logger, domainConfiguration))
					.toList();
		}
	}

	private Predicate<ListInfo> filterArchiveFolder(final Logger logger, final DomainConfiguration domainConfiguration) {
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				try {
					MailboxPaths mailboxPaths = MailboxPaths.from(listInfo.getName());
					if (!mailboxPaths.getSubPaths().startsWith(domainConfiguration.getArchiveMainFolder() + MailboxPaths.IMAP_FOLDER_SEPARATOR)) {
						return false;
					}
				} catch (MailboxFormatException e) {
					logger.error(String.format("The mailbox %s can't be parsed", listInfo.getName()));
					return false;
				}
				return true;
			}
		};
	}

	private void resetArchiveFlag(ObmDomain domain, Logger logger, DomainConfiguration domainConfiguration) throws Exception {
		String domainName = domain.getName();
		for (ListInfo listInfo : listStandardFolders(domain, logger, domainConfiguration)) {
			MailboxImpl mailbox = MailboxImpl.from(listInfo.getName(), logger, storeClientFactory.create(domainName));
			logger.info("Removing ImapArchive flag: {}", mailbox.getName());
			removeArchiveFlag(mailbox);
		}
	}

	private void removeArchiveFlag(Mailbox mailbox) throws ImapSelectException, MailboxNotFoundException {
		mailbox.select();
		mailbox.uidStore(ALL, IMAP_ARCHIVE_FLAG, false);
	}

	@VisibleForTesting ImmutableList<ListInfo> listStandardFolders(ObmDomain domain, Logger logger, DomainConfiguration domainConfiguration) throws Exception {
		try (StoreClient storeClient = storeClientFactory.create(domain.getName())) {
			storeClient.login(false);
			
			return FluentIterable.from(storeClient.listAll(ImapArchiveProcessing.USERS_REFERENCE_NAME, ImapArchiveProcessing.ALL_MAILBOXES_NAME))
					.transform(appendDomainWhenNone(domain))
					.filter(filterDomain(domain, logger))
					.filter(filterOutArchiveFolder(logger, domainConfiguration))
					.toList();
		}
	}

	private Predicate<ListInfo> filterOutArchiveFolder(final Logger logger, final DomainConfiguration domainConfiguration) {
		return new Predicate<ListInfo>() {

			@Override
			public boolean apply(ListInfo listInfo) {
				try {
					MailboxPaths mailboxPaths = MailboxPaths.from(listInfo.getName());
					if (mailboxPaths.getSubPaths().startsWith(domainConfiguration.getArchiveMainFolder() + MailboxPaths.IMAP_FOLDER_SEPARATOR)) {
						return false;
					}
				} catch (MailboxFormatException e) {
					logger.error(String.format("The mailbox %s can't be parsed", listInfo.getName()));
					return false;
				}
				return true;
			}
		};
	}
}
