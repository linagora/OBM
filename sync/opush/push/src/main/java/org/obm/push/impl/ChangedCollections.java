package org.obm.push.impl;

import java.util.Date;
import java.util.Set;

import org.obm.push.store.SyncCollection;

import com.google.common.base.Objects;

public class ChangedCollections {

	private Date lastSync;
	private Set<SyncCollection> changed;
	
	public ChangedCollections(Date lastSync, Set<SyncCollection> changed) {
		this.lastSync = lastSync;
		this.changed = changed;
	}

	
	public Date getLastSync() {
		return lastSync;
	}

	public Set<SyncCollection> getChanged() {
		return changed;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(getClass())
			.add("lastSync", lastSync)
			.add("changes", changed)
			.toString();
	}
	
}
