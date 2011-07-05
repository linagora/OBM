package org.obm.push.impl;

public enum MoveItemsStatus {
	
	INVALID_SOURCE_COLLECTION_ID, //1 Invalid source collection ID.
	INVALID_DESTINATION_COLLECTION_ID, //2 Invalid destination collection ID.
	SUCCESS, // 3 Success
	SAME_SOURCE_AND_DESTINATION_COLLECTION_ID, //4 Source and destination collection IDs are the same.
	SERVER_ERROR, // 5 A failure occurred during the MoveItem operation.
	ITEM_ALREADY_EXISTS_AT_DESTINATION, //6 An item with that name already exists at the destination.
	SOURCE_OR_DESTINATION_LOCKED;//7 Source or destination item was locked.
	
	
	public String asXmlValue() {
		switch (this) {
		case INVALID_SOURCE_COLLECTION_ID:
			return "1";
		case INVALID_DESTINATION_COLLECTION_ID:
			return "2";
		case SAME_SOURCE_AND_DESTINATION_COLLECTION_ID:
			return "4";
		case SERVER_ERROR:
			return "5";
		case ITEM_ALREADY_EXISTS_AT_DESTINATION:
			return "6";
		case SOURCE_OR_DESTINATION_LOCKED:
			return "7";
		case SUCCESS:
		default:
			return "3";
		}
	}
}
