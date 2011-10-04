package org.obm.push.exception.activesync;




public class CollectionNotFoundException extends ActiveSyncException {

	private Integer collectionId;
	
	public CollectionNotFoundException() {
		super();
	}
	
	public CollectionNotFoundException(int collectionId) {
		super();
		this.collectionId = collectionId;
	}

	public CollectionNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public CollectionNotFoundException(String message) {
		super(message);
	}

	public CollectionNotFoundException(Throwable cause) {
		super(cause);
	}

	public Integer getCollectionId() {
		return collectionId;
	}
	
}
