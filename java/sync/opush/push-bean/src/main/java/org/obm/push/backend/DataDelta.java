/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.push.backend;

import java.util.Date;
import java.util.List;

import org.obm.push.bean.SyncKey;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class DataDelta {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private List<ItemChange> changes;
		private List<ItemDeletion> deletions;
		private Date syncDate;
		private SyncKey syncKey;
		private Boolean moreAvailable;
		
		private Builder() {
			super();
		}

		public Builder changes(List<ItemChange> changes) {
			this.changes = changes;
			return this;
		}

		public Builder deletions(List<ItemDeletion> deletions) {
			this.deletions = deletions;
			return this;
		}

		public Builder syncDate(Date syncDate) {
			this.syncDate = syncDate;
			return this;
		}

		public Builder syncKey(SyncKey syncKey) {
			this.syncKey = syncKey;
			return this;
		}

		public Builder moreAvailable(boolean moreAvailable) {
			this.moreAvailable = moreAvailable;
			return this;
		}
		
		public DataDelta build() {
			Preconditions.checkNotNull(syncDate);
			Preconditions.checkNotNull(syncKey);
			
			changes = Objects.firstNonNull(changes, ImmutableList.<ItemChange>of());
			deletions = Objects.firstNonNull(deletions, ImmutableList.<ItemDeletion>of());
			moreAvailable = Objects.firstNonNull(moreAvailable, false);
			
			return new DataDelta(changes, deletions, syncDate, syncKey, moreAvailable);
		}
		
	}
	
	private final List<ItemChange> changes;
	private final List<ItemDeletion> deletions;
	private final Date syncDate;
	private final SyncKey syncKey;
	private final boolean moreAvailable;
	
	private DataDelta(List<ItemChange> changes, List<ItemDeletion> deletions, Date syncDate, SyncKey syncKey,
			boolean moreAvailable) {
		this.syncDate = syncDate;
		this.syncKey = syncKey;
		this.moreAvailable = moreAvailable;
		this.changes = ImmutableList.copyOf(changes);
		this.deletions = ImmutableList.copyOf(deletions);
	}

	public List<ItemChange> getChanges() {
		return changes;
	}
	
	public List<ItemDeletion> getDeletions() {
		return deletions;
	}
	
	public Date getSyncDate() {
		return syncDate;
	}
	
	public SyncKey getSyncKey() {
		return syncKey;
	}

	public int getItemEstimateSize() {
		int count = 0;
		if (changes != null) {
			count += changes.size();
		}
		if (deletions != null) {
			count += deletions.size();
		}
		return count;
	}

	public boolean hasMoreAvailable() {
		return moreAvailable;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(changes, deletions, syncDate, syncKey, moreAvailable);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof DataDelta) {
			DataDelta that = (DataDelta) object;
			return Objects.equal(this.changes, that.changes)
				&& Objects.equal(this.deletions, that.deletions)
				&& Objects.equal(this.syncDate, that.syncDate)
				&& Objects.equal(this.syncKey, that.syncKey)
				&& Objects.equal(this.moreAvailable, that.moreAvailable);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("changes", changes)
			.add("deletions", deletions)
			.add("syncDate", syncDate)
			.add("syncKey", syncKey)
			.add("moreAvailable", moreAvailable)
			.toString();
	}
	
	public String statistics() {
		return String.format("%d changes, %d deletions, syncdate %tc, %s syncKey, %b moreAvailable", 
					changes.size(), deletions.size(), syncDate, syncKey.getSyncKey(), moreAvailable);
	}

	public static DataDelta newEmptyDelta(Date lastSync, SyncKey syncKey) {
		return DataDelta.builder()
				.changes(ImmutableList.<ItemChange>of())
				.deletions(ImmutableList.<ItemDeletion>of())
				.syncDate(lastSync)
				.syncKey(syncKey)
				.moreAvailable(false)
				.build();
	}
}
