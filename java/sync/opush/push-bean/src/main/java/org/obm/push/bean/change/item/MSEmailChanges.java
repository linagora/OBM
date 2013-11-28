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
package org.obm.push.bean.change.item;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class MSEmailChanges {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private final List<ItemChange> changes;
		private final List<ItemDeletion> deletions;

		private Builder() {
			changes = Lists.newArrayList();
			deletions = Lists.newArrayList();
		}
		
		public Builder changes(Collection<ItemChange> changes) {
			Preconditions.checkNotNull(changes);
			this.changes.addAll(changes);
			return this;
		}
		
		public Builder deletions(Collection<ItemDeletion> deletions) {
			Preconditions.checkNotNull(deletions);
			this.deletions.addAll(deletions);
			return this;
		}
		
		public MSEmailChanges build() {
			return new MSEmailChanges(changes, deletions);
		}
	}
	
	private final List<ItemChange> changes;
	private final List<ItemDeletion> deletions;

	private MSEmailChanges(List<ItemChange> changes, List<ItemDeletion> deletions) {
		this.changes = changes;
		this.deletions = deletions;
	}
	
	public List<ItemChange> getItemChanges() {
		return changes;
	}
	
	public List<ItemDeletion> getItemDeletions() {
		return deletions;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(changes, deletions);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof MSEmailChanges) {
			MSEmailChanges that = (MSEmailChanges) object;
			return Objects.equal(this.changes, that.changes)
				&& Objects.equal(this.deletions, that.deletions);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("changes", changes)
			.add("deletions", deletions)
			.toString();
	}
}
