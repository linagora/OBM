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
import org.joda.time.DateTimeZone;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import fr.aliacom.obm.common.domain.ObmDomainUuid;

public class ArchiveTreatment {

	public static Builder<ArchiveTreatment> builder(ObmDomainUuid domainUuid) {
		return new Builder<ArchiveTreatment>(domainUuid);
	}
	
	public static class Builder<T extends ArchiveTreatment> {
		
		protected final ObmDomainUuid domainUuid;
		protected ArchiveTreatmentRunId runId;
		protected ArchiveStatus status;
		protected Boolean recurrent;
		protected DateTime scheduledTime;
		protected DateTime startTime;
		protected DateTime endTime;
		protected DateTime higherBoundary;

		protected Builder(ObmDomainUuid domainUuid) {
			Preconditions.checkNotNull(domainUuid);
			this.domainUuid = domainUuid;
		}
		
		public Builder<T> runId(String runId) {
			return runId(ArchiveTreatmentRunId.from(runId));
		}
		
		public Builder<T> runId(ArchiveTreatmentRunId runId) {
			Preconditions.checkNotNull(runId);
			this.runId = runId;
			return this;
		}
		
		public Builder<T> scheduledAt(DateTime scheduledTime) {
			Preconditions.checkNotNull(scheduledTime);
			this.scheduledTime = scheduledTime;
			return this;
		}
		
		public Builder<T> startedAt(DateTime startTime) {
			this.startTime = startTime;
			return this;
		}
		
		public Builder<T> terminatedAt(DateTime endTime) {
			this.endTime = endTime;
			return this;
		}
		
		public Builder<T> higherBoundary(DateTime higherBoundary) {
			Preconditions.checkNotNull(higherBoundary);
			this.higherBoundary = higherBoundary;
			return this;
		}
		
		public Builder<T> status(ArchiveStatus status) {
			Preconditions.checkNotNull(status);
			this.status = status;
			return this;
		}

		public Builder<T> recurrent(boolean recurrent) {
			this.recurrent = recurrent;
			return this;
		}
		
		@SuppressWarnings("unchecked")
		public T build() {
			Preconditions.checkState(runId != null);
			Preconditions.checkState(scheduledTime != null);
			Preconditions.checkState(higherBoundary != null);
			Preconditions.checkState(status != null);
			Preconditions.checkState(recurrent != null);
			return (T) new ArchiveTreatment(runId, domainUuid, status, scheduledTime, startTime, endTime, higherBoundary, recurrent);
		}
	}
	
	public static final DateTime FAILED_AT_UNKOWN_DATE = new DateTime(0, DateTimeZone.UTC);
	public static final DateTime NO_DATE = null;
	
	protected final ArchiveTreatmentRunId runId;
	protected final ObmDomainUuid domainUuid;
	protected final ArchiveStatus archiveStatus;
	protected final DateTime scheduledTime;
	protected final DateTime startTime;
	protected final DateTime endTime;
	protected final DateTime higherBoundary;
	protected final boolean recurrent;

	protected ArchiveTreatment(ArchiveTreatmentRunId runId, ObmDomainUuid  domainUuid, ArchiveStatus archiveStatus, 
			DateTime scheduledTime, DateTime startTime, DateTime endTime, DateTime higherBoundary, boolean recurrent) {
		this.runId = runId;
		this.domainUuid = domainUuid;
		this.archiveStatus = archiveStatus;
		this.scheduledTime = scheduledTime;
		this.startTime = startTime;
		this.endTime = endTime;
		this.higherBoundary = higherBoundary;
		this.recurrent = recurrent;
	}

	public ArchiveTreatmentRunId getRunId() {
		return runId;
	}

	public ObmDomainUuid getDomainUuid() {
		return domainUuid;
	}

	public ArchiveStatus getArchiveStatus() {
		return archiveStatus;
	}

	public DateTime getScheduledTime() {
		return scheduledTime;
	}

	public DateTime getStartTime() {
		return startTime;
	}

	public DateTime getEndTime() {
		return endTime;
	}

	public DateTime getHigherBoundary() {
		return higherBoundary;
	}

	public boolean isRecurrent() {
		return recurrent;
	}

	public ArchiveTerminatedTreatment asSuccess(DateTime endTime) {
		return asTerminatedBuilder(endTime).status(ArchiveStatus.SUCCESS).build();
	}

	public ArchiveTerminatedTreatment asError(DateTime endTime) {
		return asTerminatedBuilder(endTime).status(ArchiveStatus.ERROR).build();
	}
	
	private ArchiveTerminatedTreatment.Builder<ArchiveTerminatedTreatment> asTerminatedBuilder(DateTime endTime) {
		return ArchiveTerminatedTreatment
				.forDomain(domainUuid)
				.runId(runId)
				.recurrent(recurrent)
				.scheduledAt(scheduledTime)
				.startedAt(startTime)
				.higherBoundary(higherBoundary)
				.terminatedAt(endTime);
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(runId, domainUuid, archiveStatus,
				startTime, endTime, higherBoundary, recurrent);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof ArchiveTreatment) {
			ArchiveTreatment that = (ArchiveTreatment) object;
			return Objects.equal(this.runId, that.runId)
				&& Objects.equal(this.domainUuid, that.domainUuid)
				&& Objects.equal(this.recurrent, that.recurrent)
				&& Objects.equal(this.archiveStatus, that.archiveStatus)
				&& Objects.equal(this.startTime, that.startTime)
				&& Objects.equal(this.endTime, that.endTime)
				&& Objects.equal(this.higherBoundary, that.higherBoundary);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("runId", runId)
			.add("domainUuid", domainUuid)
			.add("recurrent", recurrent)
			.add("archiveStatus", archiveStatus)
			.add("startTime", startTime)
			.add("endTime", endTime)
			.add("higherBoundary", higherBoundary)
			.toString();
	}
}
