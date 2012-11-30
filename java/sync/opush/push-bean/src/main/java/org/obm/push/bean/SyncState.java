/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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

/**
 * Stores the last sync date for a given sync key & collection
 */
public abstract class SyncState implements Serializable {

	private Date syncDate;
	private boolean syncFiltred;
	private SyncKey syncKey;
	private int id;

	protected SyncState(Date syncDate, boolean syncFiltred, SyncKey syncKey, int id) {
		this.syncDate = syncDate;
		this.syncFiltred = syncFiltred;
		this.syncKey = syncKey;
		this.id = id;
	}
	
	protected SyncState(SyncKey syncKey) {
		this(syncKey, null);
	}

	protected SyncState(SyncKey syncKey, Date syncDate) {
		this.syncDate = Objects.firstNonNull(syncDate, DateUtils.getEpochPlusOneSecondCalendar().getTime());
		this.syncFiltred = false;
		this.syncKey = syncKey;
	}

	public Date getSyncDate() {
		return syncDate;
	}

	public void setSyncDate(Date syncDate) {
		this.syncDate = syncDate;
	}

	public SyncKey getSyncKey() {
		return syncKey;
	}

	public void setSyncKey(SyncKey syncKey) {
		this.syncKey = syncKey;
	}

	public Boolean isSyncFiltred() {
		return syncFiltred;
	}

	public void setSyncFiltred(boolean syncFiltred) {
		this.syncFiltred = syncFiltred;
	}

	public void updateLastWindowStartDate(FilterType filterType) {
		if (filterType != null) {
			Date filteredDate = filterType.getFilteredDateTodayAtMidnight();
			if (getSyncDate() != null && filteredDate.after(getSyncDate())) {
				setSyncDate(filteredDate);
				setSyncFiltred(true);
			}
		}
	}

	public int getId() {
		return this.id;
	}
	
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(syncDate, syncFiltred, syncKey, id);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SyncState) {
			SyncState that = (SyncState) object;
			return Objects.equal(this.syncDate, that.syncDate)
				&& Objects.equal(this.syncFiltred, that.syncFiltred)
				&& Objects.equal(this.syncKey, that.syncKey)
				&& Objects.equal(this.id, that.id);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("syncDate", syncDate)
			.add("syncFiltred", syncFiltred)
			.add("syncKey", syncKey)
			.add("id", id)
			.toString();
	}
	
}
