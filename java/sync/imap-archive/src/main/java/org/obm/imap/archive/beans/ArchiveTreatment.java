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

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveTreatment {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private ArchiveTreatmentRunId runId;
		private ObmDomainUuid domainId;
		private ArchiveStatus archiveStatus;
		private DateTime start;
		private DateTime end;
		private DateTime lowerBoundary;
		private DateTime higherBoundary;
		
		private Builder() {
		}
		
		public Builder runId(ArchiveTreatmentRunId runId) {
			Preconditions.checkNotNull(runId);
			this.runId = runId;
			return this;
		}
		
		public Builder domainId(ObmDomainUuid domainId) {
			Preconditions.checkNotNull(domainId);
			this.domainId = domainId;
			return this;
		}
		
		public Builder archiveStatus(ArchiveStatus archiveStatus) {
			Preconditions.checkNotNull(archiveStatus);
			this.archiveStatus = archiveStatus;
			return this;
		}
		
		public Builder start(DateTime start) {
			Preconditions.checkNotNull(start);
			this.start = start;
			return this;
		}
		
		public Builder end(DateTime end) {
			Preconditions.checkNotNull(end);
			this.end = end;
			return this;
		}
		
		public Builder lowerBoundary(DateTime lowerBoundary) {
			Preconditions.checkNotNull(lowerBoundary);
			this.lowerBoundary = lowerBoundary;
			return this;
		}
		
		public Builder higherBoundary(DateTime higherBoundary) {
			Preconditions.checkNotNull(higherBoundary);
			this.higherBoundary = higherBoundary;
			return this;
		}
		
		public ArchiveTreatment build() {
			Preconditions.checkState(runId != null);
			Preconditions.checkState(domainId != null);
			Preconditions.checkState(archiveStatus != null);
			Preconditions.checkState(start != null);
			
			if (archiveStatus != ArchiveStatus.RUNNING) {
				Preconditions.checkState(end != null);
			}
			
			return new ArchiveTreatment(runId, domainId, archiveStatus, start, end, lowerBoundary, higherBoundary);
		}
	}
	
	private final ArchiveTreatmentRunId runId;
	private final ObmDomainUuid domainId;
	private final ArchiveStatus archiveStatus;
	private final DateTime start;
	private final DateTime end;
	private final DateTime lowerBoundary;
	private final DateTime higherBoundary;

	private ArchiveTreatment(ArchiveTreatmentRunId runId, ObmDomainUuid  domainId, ArchiveStatus archiveStatus, DateTime start, DateTime end, DateTime lowerBoundary, DateTime higherBoundary) {
		this.runId = runId;
		this.domainId = domainId;
		this.archiveStatus = archiveStatus;
		this.start = start;
		this.end = end;
		this.lowerBoundary = lowerBoundary;
		this.higherBoundary = higherBoundary;
	}
	
	public ArchiveTreatmentRunId getRunId() {
		return runId;
	}
	
	public ObmDomainUuid getDomainId() {
		return domainId;
	}

	public ArchiveStatus getArchiveStatus() {
		return archiveStatus;
	}

	public DateTime getStart() {
		return start;
	}

	public DateTime getEnd() {
		return end;
	}

	public DateTime getLowerBoundary() {
		return lowerBoundary;
	}

	public DateTime getHigherBoundary() {
		return higherBoundary;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(runId, domainId, archiveStatus, start, end, lowerBoundary, higherBoundary);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof ArchiveTreatment) {
			ArchiveTreatment that = (ArchiveTreatment) object;
			return Objects.equal(this.runId, that.runId)
				&& Objects.equal(this.domainId, that.domainId)
				&& Objects.equal(this.archiveStatus, that.archiveStatus)
				&& Objects.equal(this.start, that.start)
				&& Objects.equal(this.end, that.end)
				&& Objects.equal(this.lowerBoundary, that.lowerBoundary)
				&& Objects.equal(this.higherBoundary, that.higherBoundary);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("runId", runId)
			.add("domainId", domainId)
			.add("archiveStatus", archiveStatus)
			.add("start", start)
			.add("end", end)
			.add("lowerBoundary", lowerBoundary)
			.add("higherBoundary", higherBoundary)
			.toString();
	}
}
