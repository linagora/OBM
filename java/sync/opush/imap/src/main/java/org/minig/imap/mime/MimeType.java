package org.minig.imap.mime;

public class MimeType {

	private final String type;
	private final String subtype;

	public MimeType(String type, String subtype) {
		this.type = type;
		this.subtype = subtype;
	}
	
	public String getType() {
		return type;
	}
	
	public String getSubtype() {
		return subtype;
	}
	
	@Override
	public String toString() {
		return "MimeType(" + type + "/" + subtype + ")";
	}
}
