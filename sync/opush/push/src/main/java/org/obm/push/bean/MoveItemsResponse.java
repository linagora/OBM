package org.obm.push.bean;

import java.util.List;

import org.obm.push.impl.MoveItemsStatus;

public class MoveItemsResponse {

	public static class MoveItemsItem {
		private MoveItemsStatus itemStatus;
		private final String sourceMessageId;
		private String newDstId;

		public MoveItemsItem(MoveItemsStatus status, String sourceMessageId) {
			this.itemStatus = status;
			this.sourceMessageId = sourceMessageId;
		}
		public void setStatusForItem(MoveItemsStatus status) {
			this.itemStatus = status;
		}
		public MoveItemsStatus getItemStatus() {
			return itemStatus;
		}
		public String getSourceMessageId() {
			return sourceMessageId;
		}
		public void setDstMesgId(String newDstId) {
			this.newDstId = newDstId;
		}
		public String getNewDstId() {
			return newDstId;
		}
	}

	private final List<MoveItemsItem> moveItemsItem;
	
	public MoveItemsResponse(List<MoveItemsItem> moveItemsItem) {
		this.moveItemsItem = moveItemsItem;
	}
	
	public List<MoveItemsItem> getMoveItemsItem() {
		return moveItemsItem;
	}
	
}
