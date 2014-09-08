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
package org.obm.imap.archive.beans;

import org.joda.time.DateTime;
import org.obm.imap.archive.logging.LoggerAppenders;

import ch.qos.logback.classic.Logger;

import com.google.common.base.Objects;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveConfiguration {
	
	private final Logger logger;
	private final LoggerAppenders loggerAppenders;
	private final ObmDomainUuid domain;
	private final DateTime when;
	private final DateTime higherBoundary;
	private final ArchiveTreatmentRunId runId;
	private final boolean recurrent;
	
	public ArchiveConfiguration(ObmDomainUuid domain,
			DateTime when, DateTime higherBoundary, ArchiveTreatmentRunId runId, Logger logger, LoggerAppenders loggerAppenders, 
			boolean recurrent) {
		this.domain = domain;
		this.when = when;
		this.higherBoundary = higherBoundary;
		this.runId = runId;
		this.logger = logger;
		this.loggerAppenders = loggerAppenders;
		this.recurrent = recurrent;
	}
	
	public ObmDomainUuid getDomainId() {
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
	
	public boolean isRecurrent() {
		return recurrent;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(domain, when, recurrent, runId, higherBoundary);
	}

	@Override
	public boolean equals(Object object){
		if (object instanceof ArchiveConfiguration) {
			ArchiveConfiguration that = (ArchiveConfiguration) object;
			return Objects.equal(this.domain, that.domain)
					&& Objects.equal(this.recurrent, that.recurrent)
					&& Objects.equal(this.when, that.when)
					&& Objects.equal(this.higherBoundary, that.higherBoundary)
					&& Objects.equal(this.runId, that.runId);
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
			.toString();
	}
}