package org.obm.push.store;

/**
 * 
 * @author adrienp
 *
 */
public enum MSEmailBodyType {
	
	PlainText, HTML, RTF, MIME;

	public String asIntString() {
		switch (this) {
		case PlainText:
			return "1";
		case HTML:
			return "2";
		case RTF:
			return "3";
		case MIME:
			return "4";
		default:
			return "0";
		}
	}

	public static final MSEmailBodyType getValueOf(String s) {
		if ("text/rtf".equals(s)) {
			return RTF;
		} else if ("text/html".equals(s)) {
			return HTML;
		} else {
			return PlainText;
		}
	}
	
	public static final MSEmailBodyType getValueOf(Integer s) {
		if(s==null){
			return null;
		}
		
		if (s.equals(1)) {
			return PlainText;
		} else if (s.equals(2)) {
			return HTML;
		} else if (s.equals(3)) {
			return RTF;
		} else if (s.equals(4)) {
			return MIME;
		} else {
			return null;
		}
	}
}
