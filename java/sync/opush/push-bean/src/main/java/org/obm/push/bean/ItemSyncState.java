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
package org.obm.push.bean;

import java.io.Serializable;
import java.util.Date;

import org.obm.push.utils.DateUtils;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class ItemSyncState implements Serializable {

	private final static Date INITIAL_DATE = DateUtils.getEpochPlusOneSecondCalendar().getTime();
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private Date syncDate;
		private boolean syncFiltered;
		private SyncKey syncKey;
		private int id;
		
		private Builder() {}
		
		public Builder syncDate(Date syncDate) {
			this.syncDate = syncDate;
			return this;
		}
		
		public Builder syncFiltered(boolean syncFiltered) {
			this.syncFiltered = syncFiltered;
			return this;
		}
		
		public Builder syncKey(SyncKey syncKey) {
			this.syncKey = syncKey;
			return this;
		}
		
		public Builder id(int id) {
			this.id = id;
			return this;
		}
		
		public ItemSyncState build() {
			Preconditions.checkArgument(syncKey != null, "syncKey can't be null or empty");
			Preconditions.checkArgument(syncDate != null, "syncDate can't be null or empty");
			return new ItemSyncState(syncDate, syncFiltered, syncKey, id);
		}
	}
	
	private static final long serialVersionUID = 133407493947001047L;
	
	private final Date syncDate;
	private final boolean syncFiltered;
	private final SyncKey syncKey;
	private final int id;
	
	private ItemSyncState(Date syncDate, boolean syncFiltered, SyncKey syncKey, int id) {
		this.syncDate = syncDate;
		this.syncFiltered = syncFiltered;
		this.syncKey = syncKey;
		this.id = id;
	}

	public Date getSyncDate() {
		return syncDate;
	}

	public boolean isSyncFiltered() {
		return syncFiltered;
	}

	public SyncKey getSyncKey() {
		return syncKey;
	}

	public int getId() {
		return id;
	}
	
	public boolean isInitial() {
		return INITIAL_DATE.equals(syncDate);
	}
	
	public Date getFilteredSyncDate(FilterType filterType) {
		if (filterType != null) {
			Date filteredDate = filterType.getFilteredDateTodayAtMidnight();
			if (getSyncDate() != null && filteredDate.after(getSyncDate())) {
				return filteredDate;
			}
		}
		return getSyncDate();
	}	

	@Override
	public final int hashCode(){
		return Objects.hashCode(syncDate, syncFiltered, syncKey, id);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof ItemSyncState) {
			ItemSyncState that = (ItemSyncState) object;
			return Objects.equal(this.syncDate, that.syncDate)
				&& Objects.equal(this.syncFiltered, that.syncFiltered)
				&& Objects.equal(this.syncKey, that.syncKey)
				&& Objects.equal(this.id, that.id);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("syncDate", syncDate)
			.add("syncFiltered", syncFiltered)
			.add("syncKey", syncKey)
			.add("id", id)
			.toString();
	}
}
