package org.obm.push.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

public class Encoder {

	
	protected Encoder(){
	}
	
	protected void s(Element p, String name, String val) {
		if (val != null && val.length() > 0) {
			DOMUtils.createElementAndText(p, name, val);
		}
	}

	protected void s(Element p, String name, Integer val) {
		if (val != null) {
			DOMUtils.createElementAndText(p, name, val.toString());
		}
	}

	protected void s(Element p, String name, Date val, SimpleDateFormat sdf) {
		if (val != null) {
			DOMUtils.createElementAndText(p, name, sdf.format(val));
		}
	}

	protected void s(Element p, String name, Boolean val) {
		if (val == null) {
			val = false;
		}
		DOMUtils.createElementAndText(p, name, val ? "1" : "0");
	}
	
}
