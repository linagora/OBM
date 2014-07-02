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

import java.io.File;
import java.nio.file.Paths;
import java.util.Comparator;

import org.apache.commons.io.output.DeferredFileOutputStream;
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.services.ArchiveService;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linagora.scheduling.Task;

import fr.aliacom.obm.common.domain.ObmDomain;

public class ArchiveDomainTask implements Task {

	private final static String LOG_PATH = "/var/log/obm-imap-archive";
	private final static String LOG_EXTENSION = ".log";
	private final static int BYTES_IN_MEMORY = 10240;
	
	public static Comparator<ArchiveDomainTask> comparator() {
		return new Comparator<ArchiveDomainTask>() {

			@Override
			public int compare(ArchiveDomainTask o1, ArchiveDomainTask o2) {
				return DateTimeComparator.getInstance().compare(o1.getWhen(), o2.getWhen());
			}
		};
	}
	
	@Singleton
	public static class Factory {
		
		private final ArchiveService archiveService;

		@Inject
		@VisibleForTesting Factory(ArchiveService archiveService) {
			this.archiveService = archiveService;
		}
		
		public ArchiveDomainTask create(ObmDomain domain, DateTime when, ArchiveTreatmentRunId runId) {
			return new ArchiveDomainTask(archiveService, domain, when, runId);
		}
	}
	
	private final ArchiveService archiveService;
	private final ObmDomain domain;
	private final DateTime when;
	private final ArchiveTreatmentRunId runId;
	private final DeferredFileOutputStream deferredFileOutputStream;

	protected ArchiveDomainTask(ArchiveService archiveService, ObmDomain domain, DateTime when, ArchiveTreatmentRunId runId) {
		this.archiveService = archiveService;
		this.domain = domain;
		this.when = when;
		this.runId = runId;
		this.deferredFileOutputStream = new DeferredFileOutputStream(BYTES_IN_MEMORY, 
				new File(Paths.get(LOG_PATH, runId.getRunId().toString() + LOG_EXTENSION).toUri()));
		

	}
	
	@Override
	public void run() {
		archiveService.archive(domain, runId, deferredFileOutputStream);
	}

	@Override
	public String taskName() {
		return domain.getUuid().get();
	}
	
	public ObmDomain getDomain() {
		return domain;
	}

	public DateTime getWhen() {
		return when;
	}

	public ArchiveTreatmentRunId getRunId() {
		return runId;
	}

	public DeferredFileOutputStream getDeferredFileOutputStream() {
		return deferredFileOutputStream;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(domain, when, runId);
	}

	@Override
	public final boolean equals(Object object){
		if (object instanceof ArchiveDomainTask) {
			ArchiveDomainTask that = (ArchiveDomainTask) object;
			return Objects.equal(this.domain, that.domain)
				&& Objects.equal(this.when, that.when)
				&& Objects.equal(this.runId, that.runId);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("domain", domain)
			.add("when", when)
			.add("runId", runId)
			.toString();
	}
	
}
