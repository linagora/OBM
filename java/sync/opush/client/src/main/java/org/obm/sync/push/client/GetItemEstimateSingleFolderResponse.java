package org.obm.sync.push.client;


public class GetItemEstimateSingleFolderResponse implements IEasReponse {

	private final int collectionId;
	private final int estimate;
	private final int status;

	public GetItemEstimateSingleFolderResponse(int collectionId, int estimate, int status) {
		this.collectionId = collectionId;
		this.estimate = estimate;
		this.status = status;
	}
	
	@Override
	public String getReturnedSyncKey() {
		throw new IllegalAccessError("GetItemEstimate's response has no SyncKey");
	}

	public int getCollectionId() {
		return collectionId;
	}

	public int getEstimate() {
		return estimate;
	}

	public int getStatus() {
		return status;
	}
}
