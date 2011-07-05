package org.obm.push.impl;

public enum GetItemEstimateStatus {
	OK, // 1 Success.
	INVALID_COLLECTION, // 2 A collection was invalid or one of the specified
						// collection IDs was invalid.
	NEED_SYNC, // 3 Synchronization state has not been primed yet. The Sync
				// command MUST be performed first.
	INVALID_SYNC_KEY; // 4 The specified synchronization key was invalid
	
	public String asXmlValue() {
		switch (this) {
		case INVALID_COLLECTION:
			return "2";
		case NEED_SYNC:
			return "3";
		case INVALID_SYNC_KEY:
			return "4";
		case OK:
		default:
			return "1";
		}
	}

}
