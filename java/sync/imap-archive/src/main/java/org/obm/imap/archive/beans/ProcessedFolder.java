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


package org.obm.imap.archive.beans;

import org.joda.time.DateTime;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ProcessedFolder {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private final static ArchiveStatus DEFAULT_STATUS = ArchiveStatus.ERROR;
		
		private ArchiveTreatmentRunId runId;
		private ImapFolder folder;
		private Long lastUid;
		private DateTime start;
		private DateTime end;
		private ArchiveStatus status;
		
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
		
		public Builder lastUid(long lastUid) {
			this.lastUid = lastUid;
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
		
		public Builder status(ArchiveStatus status) {
			Preconditions.checkNotNull(status);
			this.status = status;
			return this;
		}
		
		public ProcessedFolder build() {
			Preconditions.checkState(runId != null);
			Preconditions.checkState(folder != null);
			Preconditions.checkState(lastUid != null);
			Preconditions.checkState(start != null);
			Preconditions.checkState(end != null);
			
			return new ProcessedFolder(runId, folder, lastUid, start, end, Objects.firstNonNull(status, DEFAULT_STATUS));
		}
	}

	private final ArchiveTreatmentRunId runId;
	private final ImapFolder folder;
	private final long lastUid;
	private final DateTime start;
	private final DateTime end;
	private final ArchiveStatus status;

	private ProcessedFolder(ArchiveTreatmentRunId runId, ImapFolder folder, long lastUid, DateTime start, DateTime end, ArchiveStatus status) {
		this.runId = runId;
		this.folder = folder;
		this.lastUid = lastUid;
		this.start = start;
		this.end = end;
		this.status = status;
	}
	
	public ArchiveTreatmentRunId getRunId() {
		return runId;
	}

	public ImapFolder getFolder() {
		return folder;
	}

	public long getLastUid() {
		return lastUid;
	}

	public DateTime getStart() {
		return start;
	}

	public DateTime getEnd() {
		return end;
	}

	public ArchiveStatus getStatus() {
		return status;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(runId, folder, lastUid, start, end, status);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof ProcessedFolder) {
			ProcessedFolder that = (ProcessedFolder) object;
			return Objects.equal(this.runId, that.runId)
				&& Objects.equal(this.folder, that.folder)
				&& Objects.equal(this.lastUid, that.lastUid)
				&& Objects.equal(this.start, that.start)
				&& Objects.equal(this.end, that.end)
				&& Objects.equal(this.status, that.status);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("runId", runId)
			.add("folder", folder)
			.add("lastUid", lastUid)
			.add("start", start)
			.add("end", end)
			.add("status", status)
			.toString();
	}
}
