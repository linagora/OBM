package org.obm.push.protocol.bean;

import java.util.Collection;

import org.obm.push.bean.SyncCollection;

public class GetItemEstimateRequest {

	private final Collection<SyncCollection> syncCollections;

	public GetItemEstimateRequest(Collection<SyncCollection> syncCollections) {
		this.syncCollections = syncCollections;
	}

	public Collection<SyncCollection> getSyncCollections() {
		return syncCollections;
	}
	
}
