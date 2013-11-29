/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.protocol.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.obm.push.protocol.bean.ASTimeZone;
import org.obm.push.utils.DOMUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Decoder {
	
	private final Base64ASTimeZoneDecoder base64asTimeZoneDecoder;
	private final ASTimeZoneConverter asTimeZoneConverter;
	
	@Inject
	@VisibleForTesting Decoder(Base64ASTimeZoneDecoder base64asTimeZoneDecoder, ASTimeZoneConverter asTimeZoneConverter) {
		this.base64asTimeZoneDecoder = base64asTimeZoneDecoder;
		this.asTimeZoneConverter = asTimeZoneConverter;
	}
	
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
			byte[] nodeInBase64 = node.getTextContent().getBytes(Charsets.UTF_8);
			ASTimeZone asTimeZone = base64asTimeZoneDecoder.decode(nodeInBase64);
			return asTimeZoneConverter.convert(asTimeZone);
		}
		return default_value;
	}

	public TimeZone parseTimeZone(String b64) {
		return new TZDecoder().decode(b64);
	}

	public List<String> parseDOMStringCollection(Element node,
			String elementName, List<String> default_value) {
		if (node != null) {
			return new ArrayList<String>(Arrays.asList(DOMUtils.getTexts(node,
					elementName)));
		}

		return default_value;
	}

	public List<String> parseDOMStringCollection(Element node,
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
