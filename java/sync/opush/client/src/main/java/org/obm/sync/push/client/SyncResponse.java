package org.obm.sync.push.client;

import java.util.HashMap;
import java.util.Map;

public class SyncResponse {

	private Map<String, Collection> cl;

	public SyncResponse(Map<String, Collection> cl) {
		this.cl = new HashMap<String, Collection>(cl);
	}

	public Map<String, Collection> getCollections() {
		return cl;
	}
	
	public Collection getCollection(String key) {
		return cl.get(key);
	}
}
