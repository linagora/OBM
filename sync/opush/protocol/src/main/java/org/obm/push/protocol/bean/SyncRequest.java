package org.obm.push.protocol.bean;

import org.obm.push.bean.Sync;

public class SyncRequest {

	private final Sync sync;
	
	public SyncRequest(Sync sync) {
		this.sync = sync;
	}
	
	public Sync getSync() {
		return sync;
	}
	
}
