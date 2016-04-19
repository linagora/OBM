/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014-2016  Linagora
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

import java.util.Set;

import org.joda.time.DateTime;
import org.obm.imap.archive.beans.ArchiveConfiguration;
import org.obm.imap.archive.beans.ArchiveStatus;
import org.obm.imap.archive.beans.ArchiveTreatment;
import org.obm.imap.archive.beans.ArchiveTreatmentKind;
import org.obm.imap.archive.beans.HigherBoundary;
import org.obm.imap.archive.beans.Limit;
import org.obm.imap.archive.beans.RepeatKind;
import org.obm.imap.archive.dao.ArchiveTreatmentDao;
import org.obm.imap.archive.exception.ImapArchiveProcessingException;
import org.obm.provisioning.dao.exceptions.DaoException;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linagora.scheduling.DateTimeProvider;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;

@Singleton
public class ImapArchiveProcessing {

	private final DateTimeProvider dateTimeProvider;
	private final SchedulingDatesService schedulingDatesService;
	protected final StoreClientFactory storeClientFactory;
	protected final ArchiveTreatmentDao archiveTreatmentDao;
	private final Set<MailboxesProcessor> mailboxesProcessors;
	private final MailboxProcessing mailboxProcessing;

	
	@Inject
	@VisibleForTesting ImapArchiveProcessing(DateTimeProvider dateTimeProvider,
			SchedulingDatesService schedulingDatesService,
			StoreClientFactory storeClientFactory,
			ArchiveTreatmentDao archiveTreatmentDao,
			MailboxProcessing mailboxProcessing,
			Set<MailboxesProcessor> mailboxesProcessors) {
		
		this.dateTimeProvider = dateTimeProvider;
		this.schedulingDatesService = schedulingDatesService;
		this.storeClientFactory = storeClientFactory;
		this.archiveTreatmentDao = archiveTreatmentDao;
		this.mailboxProcessing = mailboxProcessing;
		this.mailboxesProcessors = mailboxesProcessors;
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
		Logger logger = processedTask.getLogger();
		logStart(logger, processedTask.getDomain());

		boolean isSuccess = true;
		for (MailboxesProcessor mailboxesProcessor : mailboxesProcessors) {
			isSuccess = isSuccess && mailboxesProcessor.processMailboxes(processedTask, logger, mailboxProcessing);
		}

		return isSuccess;
	}
	@VisibleForTesting boolean continuePrevious(Optional<ArchiveTreatment> previousArchiveTreatment, DateTime higherBoundary) {
		return previousArchiveTreatment.isPresent() && previousArchiveTreatment.get().getArchiveStatus() != ArchiveStatus.SUCCESS
				&& previousArchiveTreatment.get().getHigherBoundary().equals(higherBoundary);
	}
}
