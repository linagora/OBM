package org.obm.push.bean;

import java.util.Date;
import java.util.Set;

import com.google.common.base.Objects;

public class ChangedCollections {

	private final Date lastSync;
	private final Set<SyncCollection> changes;
	
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
	public final int hashCode(){
		return Objects.hashCode(lastSync, changes);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof ChangedCollections) {
			ChangedCollections that = (ChangedCollections) object;
			return Objects.equal(this.lastSync, that.lastSync)
				&& Objects.equal(this.changes, that.changes);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.toString();
	}

}
