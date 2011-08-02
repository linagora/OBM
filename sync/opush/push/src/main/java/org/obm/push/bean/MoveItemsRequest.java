package org.obm.push.bean;

import java.util.List;

import org.obm.push.backend.MoveItem;

public class MoveItemsRequest {

	private final List<MoveItem> moveItems;

	public MoveItemsRequest(List<MoveItem> moveItems) {
		this.moveItems = moveItems;
	}
	
	public List<MoveItem> getMoveItems() {
		return moveItems;
	}

}
