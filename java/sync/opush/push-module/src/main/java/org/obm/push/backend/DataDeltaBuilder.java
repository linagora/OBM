package org.obm.push.backend;

import java.util.Date;
import java.util.List;

import org.obm.push.bean.Builder;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ItemChangesBuilder;

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
