package org.obm.sync.push.client;

import java.util.LinkedList;
import java.util.List;

/**
 * <Collection> <SyncKey>f0e0ec53-40a6-432a-bfee-b8c1d391478c</SyncKey>
 * <CollectionId>179</CollectionId> <Status>1</Status> </Collection>
 * 
 * @author adrienp
 * 
 */
public class Collection {

	private String syncKey;
	private String collectionId;
	private SyncStatus status;
	private List<Add> adds = new LinkedList<Add>();

	public String getSyncKey() {
		return syncKey;
	}

	public void setSyncKey(String syncKey) {
		this.syncKey = syncKey;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}

	public SyncStatus getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = SyncStatus.getSyncStatus(status);
	}

	public List<Add> getAdds() {
		return adds;
	}

	public void addAdd(Add applicationData) {
		adds.add(applicationData);
	}

}
