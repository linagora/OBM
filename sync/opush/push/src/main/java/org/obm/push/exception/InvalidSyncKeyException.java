package org.obm.push.exception;

public class InvalidSyncKeyException extends Exception {

	private final Integer collectionId;
	private final String syncKey;

	public InvalidSyncKeyException(String syncKey) {
		this(null, syncKey);
	}
	
	public InvalidSyncKeyException(Integer collectionId, String syncKey) {
		super(String.format(
				"A client provided an unknown SyncKey (%s), may be expected after database migration", 
				syncKey));
		this.collectionId = collectionId;
		this.syncKey = syncKey;
	}
	
	public Integer getCollectionId() {
		return collectionId;
	}

	public Object getSyncKey() {
		return syncKey;
	}

}
