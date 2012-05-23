package org.obm.sync.push.client;

import com.google.common.base.Objects;


public final class GetItemEstimateSingleFolderResponse implements IEasReponse {

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

	@Override
	public int hashCode(){
		return Objects.hashCode(collectionId, estimate, status);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof GetItemEstimateSingleFolderResponse) {
			GetItemEstimateSingleFolderResponse that = (GetItemEstimateSingleFolderResponse) object;
			return Objects.equal(this.collectionId, that.collectionId)
				&& Objects.equal(this.estimate, that.estimate)
				&& Objects.equal(this.status, that.status);
		}
		return false;
	}
	
	
}
