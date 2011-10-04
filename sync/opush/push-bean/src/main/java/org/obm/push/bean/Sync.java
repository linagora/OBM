package org.obm.push.bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


import com.google.common.collect.ImmutableSet;
import com.google.common.base.Objects;

public class Sync {
	
	private final Map<Integer, SyncCollection> collections;
	private Integer wait;
	
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
	
	public Set<SyncCollection> getCollections() {
		return ImmutableSet.copyOf(collections.values());
	}
	
	public SyncCollection getCollection(Integer collectionId) {
		return collections.get(collectionId);
	}
	
	public void addCollection(SyncCollection collec) {
		collections.put(collec.getCollectionId(), collec);
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(collections, wait);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Sync) {
			Sync that = (Sync) object;
			return Objects.equal(this.collections, that.collections)
				&& Objects.equal(this.wait, that.wait);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("collections", collections)
			.add("wait", wait)
			.toString();
	}
	
}
