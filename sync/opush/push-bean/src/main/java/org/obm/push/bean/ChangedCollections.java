package org.obm.push.bean;

import java.util.Date;
import java.util.Set;

import com.google.common.base.Objects;

public class ChangedCollections {

	private Date lastSync;
	private Set<SyncCollection> changes;
	
	public ChangedCollections(Date lastSync, Set<SyncCollection> changed) {
		this.lastSync = lastSync;
		this.changes = changed;
	}

	
	public Date getLastSync() {
		return lastSync;
	}

	public boolean hasChanges() {
		return !changes.isEmpty();
	}
	
	public Set<SyncCollection> getChanges() {
		return changes;
	}
	
	@Override
	public String toString() {
		return Objects
				.toStringHelper(getClass())
				.addValue(changes)
				.toString();
	}
	
}
