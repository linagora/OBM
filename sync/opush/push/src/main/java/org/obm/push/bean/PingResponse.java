package org.obm.push.bean;

import java.util.Set;

import org.obm.push.impl.PingStatus;

public class PingResponse {

	private final Set<SyncCollection> syncCollections;
	private final PingStatus pingStatus;
	
	public PingResponse(Set<SyncCollection> syncCollections, PingStatus pingStatus) {
		this.syncCollections = syncCollections;
		this.pingStatus = pingStatus;
	}

	public Set<SyncCollection> getSyncCollections() {
		return syncCollections;
	}
	
	public PingStatus getPingStatus() {
		return pingStatus;
	}
}
