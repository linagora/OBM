package org.obm.imap.archive.scheduling;

import org.joda.time.DateTime;
import org.obm.imap.archive.beans.ArchiveTreatmentKind;
import org.obm.imap.archive.beans.ArchiveTreatmentRunId;
import org.obm.imap.archive.logging.LoggerAppenders;
import org.obm.imap.archive.services.ArchiveService;

import ch.qos.logback.classic.Logger;

import com.google.common.base.Objects;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveDomainTask extends AbstractArchiveDomainTask {

	public ArchiveDomainTask(ArchiveService archiveService, ObmDomainUuid domain,
			DateTime when, DateTime higherBoundary, ArchiveTreatmentRunId runId,
			Logger logger, LoggerAppenders loggerAppenders, boolean recurrent) {
		
		super(archiveService, domain, when, higherBoundary, runId, logger, 
				loggerAppenders, recurrent);
	}
	
	@Override
	public void run() {
		archiveService.archive(this);
	}

	@Override
	public ArchiveTreatmentKind getArchiveTreatmentKind() {
		return ArchiveTreatmentKind.REAL_RUN;
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(super.hashCode(), getArchiveTreatmentKind());
	}

	@Override
	public boolean equals(Object object){
		if (object instanceof ArchiveDomainTask) {
			ArchiveDomainTask that = (ArchiveDomainTask) object;
			return super.equals(that);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("domain", domain)
			.add("when", when)
			.add("higherBoundary", higherBoundary)
			.add("runId", runId)
			.add("recurrent", recurrent)
			.toString();
	}
	
}
