package org.obm.push.exception;

public class InvalidSyncKeyException extends Exception {

	private final Integer collectionId;

	public InvalidSyncKeyException() {
		this(null);
	}
	
	public InvalidSyncKeyException(Integer collectionId) {
		super();
		this.collectionId = collectionId;
	}
	
	public Integer getCollectionId() {
		return collectionId;
	}

}
