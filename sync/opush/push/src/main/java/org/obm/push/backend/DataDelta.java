package org.obm.push.backend;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.obm.push.bean.ItemChange;

public class DataDelta {
	
	private final List<ItemChange> changes;
	private final List<ItemChange> deletions;
	private final Date syncDate;

	public DataDelta(List<ItemChange> changes, List<ItemChange> deletions, Date syncDate) {
		this.syncDate = syncDate;
		this.changes = new ArrayList<ItemChange>(changes);
		this.deletions = new ArrayList<ItemChange>(deletions);
	}

	public List<ItemChange> getChanges() {
		return changes;
	}
	
	public List<ItemChange> getDeletions() {
		return deletions;
	}
	
	public Date getSyncDate() {
		return syncDate;
	}

}
