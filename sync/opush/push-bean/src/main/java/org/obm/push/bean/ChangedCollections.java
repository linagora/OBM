package org.obm.push.bean;

import java.util.Date;
import java.util.Set;


import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

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
		ToStringHelper h = Objects.toStringHelper(getClass());
		h.addValue(h);
		for(SyncCollection change : changed){
			h.addValue(change);
		}
		return h.toString();

	}
	
}
