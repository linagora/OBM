package org.obm.push.protocol.bean;

import java.util.Set;

import org.obm.push.bean.SyncCollection;

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
