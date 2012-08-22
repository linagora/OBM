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

import java.util.Date;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.base.Objects;

public class HierarchyItemsChanges {

	public static class Builder {
		private List<ItemChange> changes;
		private List<ItemChange> deletions;
		private Date lastSync;

		public Builder changes(List<ItemChange> changes) {
			this.changes = changes;
			return this;
		}
		
		public Builder deletions(List<ItemChange> deletions) {
			this.deletions = deletions;
			return this;
		}
		
		public Builder lastSync(Date lastSync) {
			this.lastSync = lastSync;
			return this;
		}
		
		public HierarchyItemsChanges build() {
			if (changes == null) {
				changes = ImmutableList.of();
			}
			if (deletions == null) {
				deletions = ImmutableList.of();
			}
			return new HierarchyItemsChanges(changes, deletions, lastSync);
		}
	}
	
	private final List<ItemChange> changes;
	private final List<ItemChange> deletions;
	private final Date lastSync;

	private HierarchyItemsChanges(List<ItemChange> changes, List<ItemChange> deletions, Date lastSync) {
		this.changes = changes;
		this.deletions = deletions;
		this.lastSync = lastSync;
	}
	
	public List<ItemChange> getChangedItems() {
		return changes;
	}
	
	public List<ItemChange> getDeletedItems() {
		return deletions;
	}

	public Date getLastSync() {
		return lastSync;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(changes, deletions, lastSync);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof HierarchyItemsChanges) {
			HierarchyItemsChanges that = (HierarchyItemsChanges) object;
			return Objects.equal(this.changes, that.changes)
				&& Objects.equal(this.deletions, that.deletions)
				&& Objects.equal(this.lastSync, that.lastSync);
		}
		return false;
	}
}
