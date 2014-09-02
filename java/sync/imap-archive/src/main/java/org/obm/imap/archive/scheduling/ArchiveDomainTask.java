/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.imap.archive.scheduling;

import org.joda.time.DateTime;
import org.obm.imap.archive.beans.ArchiveTreatmentKind;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.logging.LoggerAppenders;
import org.obm.imap.archive.logging.LoggerFactory;
import org.obm.imap.archive.services.ArchiveService;

import ch.qos.logback.classic.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linagora.scheduling.Task;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveDomainTask implements Task {
	
	public interface Factory {
		ArchiveDomainTask create(ObmDomainUuid domain, DateTime when, DateTime higherBoundary, ArchiveTreatmentRunId runId, ArchiveTreatmentKind kind);
		ArchiveDomainTask createAsRecurrent(ObmDomainUuid domain, DateTime when, DateTime higherBoundary, ArchiveTreatmentRunId runId);
	}
	
	@Singleton
	public static class FactoryImpl implements Factory {
		
		private final ArchiveService archiveService;
		private final LoggerFactory loggerFactory;

		@Inject
		@VisibleForTesting FactoryImpl(ArchiveService archiveService, LoggerFactory loggerFactory) {
			this.archiveService = archiveService;
			this.loggerFactory = loggerFactory;
		}
		
		@Override
		public ArchiveDomainTask create(ObmDomainUuid domain, DateTime when, DateTime higherBoundary, ArchiveTreatmentRunId runId, ArchiveTreatmentKind kind) {
			return create(domain, when, higherBoundary, runId, kind, false);
		}

		@Override
		public ArchiveDomainTask createAsRecurrent(ObmDomainUuid domain, DateTime when, DateTime higherBoundary, ArchiveTreatmentRunId runId) {
			return create(domain, when, higherBoundary, runId, ArchiveTreatmentKind.REAL_RUN, true);
		}

		private ArchiveDomainTask create(ObmDomainUuid domain, DateTime when,
				DateTime higherBoundary, ArchiveTreatmentRunId runId, 
				ArchiveTreatmentKind archiveTreatmentKind, boolean recurrent) {
			
			Logger logger = loggerFactory.create(runId);
			LoggerAppenders loggerAppenders = LoggerAppenders.from(runId, logger);
			
			return new ArchiveDomainTask(archiveService, 
					domain, when, higherBoundary, runId, logger, loggerAppenders, archiveTreatmentKind, recurrent);
		}
	}
	
	private final ArchiveService archiveService;
	private final ObmDomainUuid domain;
	private final DateTime when;
	private final DateTime higherBoundary;
	private final ArchiveTreatmentRunId runId;
	private final Logger logger;
	private final LoggerAppenders loggerAppenders;
	private final ArchiveTreatmentKind archiveTreatmentKind;
	private final boolean recurrent;

	protected ArchiveDomainTask(ArchiveService archiveService, ObmDomainUuid domain,
			DateTime when, DateTime higherBoundary, ArchiveTreatmentRunId runId, Logger logger, LoggerAppenders loggerAppenders, 
			ArchiveTreatmentKind archiveTreatmentKind, boolean recurrent) {
		this.archiveService = archiveService;
		this.domain = domain;
		this.when = when;
		this.higherBoundary = higherBoundary;
		this.runId = runId;
		this.logger = logger;
		this.loggerAppenders = loggerAppenders;
		this.archiveTreatmentKind = archiveTreatmentKind;
		this.recurrent = recurrent;
	}
	
	@Override
	public void run() {
		archiveService.archive(this);
	}

	@Override
	public String taskName() {
		return domain.get();
	}
	
	public ObmDomainUuid getDomain() {
		return domain;
	}

	public DateTime getWhen() {
		return when;
	}
	
	public DateTime getHigherBoundary() {
		return higherBoundary;
	}

	public ArchiveTreatmentRunId getRunId() {
		return runId;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public LoggerAppenders getLoggerAppenders() {
		return loggerAppenders;
	}
	
	public ArchiveTreatmentKind getArchiveTreatmentKind() {
		return archiveTreatmentKind;
	}

	public boolean isRecurrent() {
		return recurrent;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(domain, when, recurrent, runId, higherBoundary, archiveTreatmentKind);
	}

	@Override
	public final boolean equals(Object object){
		if (object instanceof ArchiveDomainTask) {
			ArchiveDomainTask that = (ArchiveDomainTask) object;
			return Objects.equal(this.domain, that.domain)
				&& Objects.equal(this.when, that.when)
				&& Objects.equal(this.recurrent, that.recurrent)
				&& Objects.equal(this.higherBoundary, that.higherBoundary)
				&& Objects.equal(this.runId, that.runId)
				&& Objects.equal(this.archiveTreatmentKind, that.archiveTreatmentKind);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("domain", domain)
			.add("when", when)
			.add("recurrent", recurrent)
			.add("higherBoundary", higherBoundary)
			.add("runId", runId)
			.add("archiveTreatmentKind", archiveTreatmentKind)
			.toString();
	}
	
}
