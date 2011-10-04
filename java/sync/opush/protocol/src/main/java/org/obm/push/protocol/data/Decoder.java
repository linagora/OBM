package org.obm.push.protocol.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.obm.push.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class Decoder {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());

	public String parseDOMString(Element elt, String default_value) {
		if (elt != null) {
			logger.info("parse string: " + elt.getTextContent());
			return elt.getTextContent();
		}
		return default_value;
	}

	public String parseDOMString(Element elt) {
		return parseDOMString(elt, null);
	}

	public Date parseDOMDate(Element elt) {
		if (elt != null) {
			return parseDate(elt.getTextContent());
		} else {
			return null;
		}
	}

	public Byte parseDOMByte(Element elt, Byte default_value) {
		if (elt != null) {
			return parseByte(elt.getTextContent());
		}
		return default_value;
	}

	public Byte parseDOMByte(Element elt) {
		return parseDOMByte(elt, null);
	}

	public Integer parseDOMInt(Element elt, Integer default_value) {
		if (elt != null) {
			return parseInt(elt.getTextContent());
		}
		return default_value;
	}

	public Integer parseDOMInt(Element elt) {
		return parseDOMInt(elt, null);
	}

	public Date parseDate(String str) {
		SimpleDateFormat date;
		// Doc : [MS-ASDTYPE] 2.6 Date/Time
		try {
			if (str.matches("^....-..-..T..:..:..\\....Z$")) {
				date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
				date.setTimeZone(TimeZone.getTimeZone("GMT"));
				return date.parse(str);
			} else if (str.matches("^........T......Z$")) {
				date = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
				date.setTimeZone(TimeZone.getTimeZone("GMT"));
				return date.parse(str);
			}
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public TimeZone parseDOMTimeZone(Element node) {
		return parseDOMTimeZone(node, null);
	}

	public TimeZone parseDOMTimeZone(Element node, TimeZone default_value) {
		if (node != null) {
			return parseTimeZone(node.getTextContent());
		}
		return default_value;
	}

	public TimeZone parseTimeZone(String b64) {
		return new TZDecoder().decode(b64);
	}

	public ArrayList<String> parseDOMStringCollection(Element node,
			String elementName, ArrayList<String> default_value) {
		if (node != null) {
			return new ArrayList<String>(Arrays.asList(DOMUtils.getTexts(node,
					elementName)));
		}

		return default_value;
	}

	public ArrayList<String> parseDOMStringCollection(Element node,
			String elementName) {
		return parseDOMStringCollection(node, elementName, null);
	}

	public byte parseByte(String str) {
		return Byte.parseByte(str);
	}

	public int parseInt(String str) {
		logger.info("parse Integer: " + Integer.parseInt(str));
		return Integer.parseInt(str);
	}

	public boolean parseBoolean(String str) {
		return Boolean.parseBoolean(str);
	}

	public Boolean parseDOMBoolean(Element elt, Boolean default_value) {
		if (elt != null) {
			return parseBoolean(elt.getTextContent());
		}
		return default_value;
	}

	public Boolean parseDOMBoolean(Element elt) {
		return parseDOMBoolean(elt, null);
	}

	/**
	 * Return an int else -1
	 * 
	 * @param elt
	 * @return int
	 */
	public int parseDOMNoNullInt(Element elt) {
		if (elt == null)
			return -1;

		return Integer.parseInt(elt.getTextContent());
	}

	/**
	 * Return true if 1 else false
	 * 
	 * @param elt
	 * @return
	 */
	public Boolean parseDOMInt2Boolean(Element elt) {
		if (parseDOMNoNullInt(elt) == 1)
			return Boolean.TRUE;
		else
			return Boolean.FALSE;
	}
	
	//"obm obm" <user3@dom1.fr>
	public String parseDOMEmail(Element elt) {
		if(elt == null){
			return null;
		}
		String email = elt.getTextContent();
		if(email.contains("<")){
			int id = email.indexOf("<");
			int id2 = email.indexOf(">");
			email = email.substring(id+1, id2);
		}
		return email;
	}
}
