package org.minig.imap.idle;

public enum IdleTag {
	EXISTS, RECENT, FETCH, EXPUNGE, FLAGS, READWRITE, BYE, COMPLETED;

	public static IdleTag getIdleTag(String arg0) {
		if (arg0 == null) {
			return null;
		}
		return valueOf(arg0.replace("-", "").toUpperCase());
	}
}
