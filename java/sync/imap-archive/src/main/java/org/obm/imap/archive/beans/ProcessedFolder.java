/* ***** BEGIN LICENSE BLOCK *****
 *
 * Copyright (C) 2014  Linagora
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

public class ProcessedFolder {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private ArchiveTreatmentRunId runId;
		private ImapFolder folder;
		private Long uidNext;
		private DateTime start;
		private DateTime end;
		
		private Builder() {}
		
		public Builder runId(ArchiveTreatmentRunId runId) {
			Preconditions.checkNotNull(runId);
			this.runId = runId;
			return this;
		}
		
		public Builder folder(ImapFolder folder) {
			Preconditions.checkNotNull(folder);
			this.folder = folder;
			return this;
		}
		
		public Builder uidNext(long uidNext) {
			this.uidNext = uidNext;
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
		
		public ProcessedFolder build() {
			Preconditions.checkState(runId != null);
			Preconditions.checkState(folder != null);
			Preconditions.checkState(uidNext != null);
			Preconditions.checkState(start != null);
			Preconditions.checkState(end != null);
			
			return new ProcessedFolder(runId, folder, uidNext, start, end);
		}
	}

	private final ArchiveTreatmentRunId runId;
	private final ImapFolder folder;
	private final long uidNext;
	private final DateTime start;
	private final DateTime end;

	private ProcessedFolder(ArchiveTreatmentRunId runId, ImapFolder folder, long uidNext, DateTime start, DateTime end) {
		this.runId = runId;
		this.folder = folder;
		this.uidNext = uidNext;
		this.start = start;
		this.end = end;
	}
	
	public ArchiveTreatmentRunId getRunId() {
		return runId;
	}

	public ImapFolder getFolder() {
		return folder;
	}

	public long getUidNext() {
		return uidNext;
	}

	public DateTime getStart() {
		return start;
	}

	public DateTime getEnd() {
		return end;
	}


	@Override
	public int hashCode(){
		return Objects.hashCode(runId, folder, uidNext, start, end);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof ProcessedFolder) {
			ProcessedFolder that = (ProcessedFolder) object;
			return Objects.equal(this.runId, that.runId)
				&& Objects.equal(this.folder, that.folder)
				&& Objects.equal(this.uidNext, that.uidNext)
				&& Objects.equal(this.start, that.start)
				&& Objects.equal(this.end, that.end);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("runId", runId)
			.add("folder", folder)
			.add("uidNext", uidNext)
			.add("start", start)
			.add("end", end)
			.toString();
	}
}
