package org.obm.push.backend;

public enum MethodAttachment {
	NormalAttachment, // 1
	EmbeddedMessage, // 5
	AttachOLE;// 6

	public String asIntString() {
		switch (this) {
		case AttachOLE:
			return "6";
		case EmbeddedMessage:
			return "5";
		case NormalAttachment:
		default:
			return "1";
		}
	}
}
