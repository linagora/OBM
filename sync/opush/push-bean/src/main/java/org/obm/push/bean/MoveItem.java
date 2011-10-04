package org.obm.push.bean;

import com.google.common.base.Objects;

public class MoveItem {

	private final String sourceMessageId;
	private final String sourceFolderId;
	private final String destinationFolderId;

	public MoveItem(String srcMsgId, String srcFldId, String dstFldId) {
		this.sourceMessageId = srcMsgId;
		this.sourceFolderId = srcFldId;
		this.destinationFolderId = dstFldId;
	}
	
	public String getSourceMessageId() {
		return sourceMessageId;
	}

	public String getSourceFolderId() {
		return sourceFolderId;
	}

	public String getDestinationFolderId() {
		return destinationFolderId;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(sourceMessageId, sourceFolderId, destinationFolderId);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MoveItem) {
			MoveItem that = (MoveItem) object;
			return Objects.equal(this.sourceMessageId, that.sourceMessageId)
				&& Objects.equal(this.sourceFolderId, that.sourceFolderId)
				&& Objects.equal(this.destinationFolderId, that.destinationFolderId);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("sourceMessageId", sourceMessageId)
			.add("sourceFolderId", sourceFolderId)
			.add("destinationFolderId", destinationFolderId)
			.toString();
	}
	
}
