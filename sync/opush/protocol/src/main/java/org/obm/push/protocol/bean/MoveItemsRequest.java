package org.obm.push.protocol.bean;

import java.util.List;

import org.obm.push.bean.MoveItem;

public class MoveItemsRequest {

	private final List<MoveItem> moveItems;

	public MoveItemsRequest(List<MoveItem> moveItems) {
		this.moveItems = moveItems;
	}
	
	public List<MoveItem> getMoveItems() {
		return moveItems;
	}

}
