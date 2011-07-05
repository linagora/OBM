package org.obm.push.data.email;

public enum Type {

	PLAIN_TEXT, // 1
	HTML, // 2
	RTF, // 3
	MIME; // 4

	@Override
	public String toString() {
		switch (this) {
		case HTML:
			return "2";
		case RTF:
			return "3";
		case MIME:
			return "4";

		default:
		case PLAIN_TEXT:
			return "1";
		}
	}

	public static Type fromInt(int i) {
		switch (i) {
		case 2:
			return HTML;
		case 3:
			return RTF;
		case 4:
			return MIME;

		case 1:
		default:
			return PLAIN_TEXT;
		}
	}

}
