package org.obm.push.backend;

public class MoveItem {

	private String sourceMessageId;
	private String sourceFolderId;
	private String destinationFolderId;

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

}
