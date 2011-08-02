package org.obm.push.bean;

import java.util.Set;

public class PingRequest {

	private Long heartbeatInterval;
	private Set<SyncCollection> syncCollections;

	public void setHeartbeatInterval(Long interval) {
		this.heartbeatInterval = interval;
	}
	
	public Long getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public void setSyncCollections(Set<SyncCollection> syncCollections) {
		this.syncCollections = syncCollections;
	}
	
	public Set<SyncCollection> getSyncCollections() {
		return syncCollections;
	}

}
