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
import org.obm.imap.archive.services.ArchiveService;

import ch.qos.logback.classic.Logger;

import com.google.common.base.Objects;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class RealRunArchiveDomainTask extends ArchiveDomainTask {

	public RealRunArchiveDomainTask(ArchiveService archiveService, ObmDomainUuid domain,
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
		if (object instanceof RealRunArchiveDomainTask) {
			RealRunArchiveDomainTask that = (RealRunArchiveDomainTask) object;
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
