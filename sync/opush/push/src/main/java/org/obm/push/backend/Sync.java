package org.obm.push.backend;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.obm.push.store.SyncCollection;

public class Sync {
	
	private Integer wait;
	private Map<Integer, SyncCollection> collections;
	
	public Sync() {
		super();
		this.collections = new HashMap<Integer, SyncCollection>();
	}
	
	public Integer getWaitInSecond() {
		Integer ret = 0;
		if(wait != null){
			ret = wait * 60;
		}
		return ret;
	}
	
	public void setWait(Integer wait) {
		this.wait = wait;
	}
	
	public Map<Integer, SyncCollection> getCollectionById() {
		return collections;
	}
	
	public Collection<SyncCollection> getCollections() {
		return collections.values();
	}
	
	public SyncCollection getCollection(Integer collectionId) {
		return collections.get(collectionId);
	}
	
	public void addCollection(SyncCollection collec) {
		collections.put(collec.getCollectionId(), collec);
	}

}
