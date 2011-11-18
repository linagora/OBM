package org.obm.push.utils;

/**
 * Content types which could be used in Mime Headers.
 *
 */
public enum MimeContentType {
	
	TEXT_PLAIN("text", "plain"),
	TEXT_HTML("text", "html"),
	
	MULTIPART_ALTERNATIVE("multipart", "alternative"),
	MULTIPART_MIXED("multipart", "mixed");
	
	private final String prefix;
	private final String subType;

	private MimeContentType(String prefix, String subType) {
		this.prefix = prefix;
		this.subType = subType;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSubType() {
		return subType;
	}

	/**
	 * @return The content type as "prefix/subtype".
	 */
	public String getContentType() {
		return prefix + "/" + subType;
	}
}
