package org.obm.push.bean;

import org.obm.push.backend.Sync;

public class SyncRequest {

	private final Sync sync;
	
	public SyncRequest(Sync sync) {
		this.sync = sync;
	}
	
	public Sync getSync() {
		return sync;
	}
	
}
