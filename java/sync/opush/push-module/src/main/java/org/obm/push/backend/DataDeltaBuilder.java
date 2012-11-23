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
package org.obm.push.backend;

import java.util.Date;
import java.util.List;

import org.obm.push.bean.Builder;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemChangesBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class DataDeltaBuilder implements Builder<DataDelta> {

	private ItemChangesBuilder changesBuilder;
	private ItemChangesBuilder deletionsBuilder;
	private Date syncDate;
	
	public DataDeltaBuilder addChanges(ItemChangesBuilder itemChangesBuilder) {
		Preconditions.checkState(changesBuilder == null, "a changes section already exists");
		changesBuilder = itemChangesBuilder; 
		return this;
	}
	
	public DataDeltaBuilder addDeletions(ItemChangesBuilder itemChangesBuilder) {
		Preconditions.checkState(deletionsBuilder == null, "a deletions section already exists");
		deletionsBuilder = itemChangesBuilder; 
		return this;
	}

	public DataDeltaBuilder withSyncDate(Date syncDate) {
		Preconditions.checkState(this.syncDate == null, "SyncDate already defined");
		this.syncDate = syncDate; 
		return this;
	}
	
	@Override
	public DataDelta build() {
		return new DataDelta(buildChanges(), buildDeletions(), syncDate);
	}

	private List<ItemChange> buildChanges() {
		return buildItemChange(changesBuilder);
	}
	
	private List<ItemChange> buildDeletions() {
		return buildItemChange(deletionsBuilder);
	}
	
	private List<ItemChange> buildItemChange(ItemChangesBuilder builder) {
		if (builder != null) {
			 return builder.build();
		} else {
			return ImmutableList.<ItemChange>of();
		}
	}
}
