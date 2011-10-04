package org.obm.sync.push.client;

public enum ProtocolVersion {

	V121, V120, V25;

	@Override
	public String toString() {
		switch (this) {
		case V120:
			return "12.0";
		case V25:
			return "2.5";
		default:
		case V121:
			return "12.1";
		}
	}

}
