package org.obm.push.backend;

import java.util.ArrayList;
import java.util.List;

import org.obm.push.ItemChange;

public class DataDelta {
	
	private List<ItemChange> changes;
	private List<ItemChange> deletions;

	public DataDelta(List<ItemChange> changes, List<ItemChange> deletions) {
		this.changes = new ArrayList<ItemChange>(changes);
		this.deletions = new ArrayList<ItemChange>(deletions);
	}

	public List<ItemChange> getChanges() {
		return changes;
	}
	
	public List<ItemChange> getDeletions() {
		return deletions;
	}
	
	

}
