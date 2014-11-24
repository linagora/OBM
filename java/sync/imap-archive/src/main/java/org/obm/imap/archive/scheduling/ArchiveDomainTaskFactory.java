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

package org.obm.imap.archive.scheduling;

import org.joda.time.DateTime;
import org.obm.imap.archive.beans.ArchiveConfiguration;
import org.obm.imap.archive.beans.ArchiveTreatmentKind;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.beans.DomainConfiguration;
import org.obm.imap.archive.logging.LoggerAppenders;
import org.obm.imap.archive.logging.LoggerFactory;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Events.DryRunTaskStatusChanged;
import org.obm.imap.archive.scheduling.ArchiveSchedulerBus.Events.RealRunTaskStatusChanged;
import org.obm.imap.archive.services.DryRunImapArchiveProcessing;
import org.obm.imap.archive.services.ImapArchiveProcessing;

import ch.qos.logback.classic.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ArchiveDomainTaskFactory {
	
	private final ImapArchiveProcessing imapArchiveProcessing;
	private final LoggerFactory loggerFactory;
	private final DryRunImapArchiveProcessing dryRunImapArchiveProcessing;

	@Inject
	@VisibleForTesting ArchiveDomainTaskFactory(ImapArchiveProcessing imapArchiveProcessing, 
			DryRunImapArchiveProcessing dryRunImapArchiveProcessing, LoggerFactory loggerFactory) {
		this.imapArchiveProcessing = imapArchiveProcessing;
		this.dryRunImapArchiveProcessing = dryRunImapArchiveProcessing;
		this.loggerFactory = loggerFactory;
	}
	
	public ArchiveDomainTask createAsRecurrent(DomainConfiguration configuration, DateTime when, 
			DateTime higherBoundary, ArchiveTreatmentRunId runId) {
		
		ArchiveConfiguration archiveConfiguration = createConfiguration(configuration, when, higherBoundary, runId, true);
		return create(archiveConfiguration, ArchiveTreatmentKind.REAL_RUN); 
	}

	public ArchiveDomainTask create(DomainConfiguration configuration, DateTime when,
			DateTime higherBoundary, ArchiveTreatmentRunId runId,
			ArchiveTreatmentKind kind) {
		
		ArchiveConfiguration archiveConfiguration = createConfiguration(configuration, when, higherBoundary, runId, false);
		return create(archiveConfiguration, kind);
	}
	
	private ArchiveConfiguration createConfiguration(DomainConfiguration configuration,
			DateTime when, DateTime higherBoundary, ArchiveTreatmentRunId runId, boolean recurrent) {
		Logger logger = loggerFactory.create(runId);
		LoggerAppenders loggerAppenders = LoggerAppenders.from(runId, logger);
		return new ArchiveConfiguration(configuration, 
				when, higherBoundary, runId, logger, loggerAppenders, recurrent);
	}
	
	private ArchiveDomainTask create(ArchiveConfiguration configuration, 
			ArchiveTreatmentKind archiveTreatmentKind) {
		
		switch (archiveTreatmentKind) {
		case DRY_RUN:
			return new ArchiveDomainTask(dryRunImapArchiveProcessing, new DryRunTaskStatusChanged.Factory(), configuration);
		case REAL_RUN:
			return new ArchiveDomainTask(imapArchiveProcessing, new RealRunTaskStatusChanged.Factory(), configuration);
		}
		throw new IllegalArgumentException();
	}
}