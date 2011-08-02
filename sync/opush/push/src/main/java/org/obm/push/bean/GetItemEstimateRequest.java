package org.obm.push.bean;

import java.util.Collection;

public class GetItemEstimateRequest {

	private final Collection<SyncCollection> syncCollections;

	public GetItemEstimateRequest(Collection<SyncCollection> syncCollections) {
		this.syncCollections = syncCollections;
	}

	public Collection<SyncCollection> getSyncCollections() {
		return syncCollections;
	}
	
}
