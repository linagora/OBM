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
package org.obm.push.minig.imap.mime.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.IllegalCharsetNameException;

import org.obm.push.mail.EncodedWord;
import org.obm.push.mail.mime.BodyParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

public class BodyParamParser {
	
	public static BodyParam parse(String key, String value) {
		return new BodyParamParser(key, value).parse();
	}
	
	private static final Logger logger = LoggerFactory.getLogger(BodyParamParser.class);
	
	private final String key;
	private final String value;
	
	private BodyParamParser(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public BodyParam parse() {
		if (key.endsWith("*")) {
			String decodedKey = key.substring(0, key.length() - 1);
			return new BodyParam(decodedKey, decodeAsterixEncodedValue());
		} else {
			return new BodyParam(key, decodeQuotedPrintable());
		}
	}
	
	
	private String decodeAsterixEncodedValue() {
		try {
			return decodeUrlEncodedValueWithCharset();
		} catch (StringIndexOutOfBoundsException e) {
			return decodeUrlEncodedValue(value, Charsets.UTF_8.name());
		}
	}

	private String decodeUrlEncodedValueWithCharset() {
		int firstQuote = value.indexOf("'");
		int secondQuote = value.indexOf("'", firstQuote + 1);
		String charsetName = value.substring(0, firstQuote);
		String text = value.substring(secondQuote + 1);
		return decodeUrlEncodedValue(text, charsetName);
	}

	private String decodeUrlEncodedValue(String text, String charset) {
		try {
			return URLDecoder.decode(text, charset);
		} catch (UnsupportedEncodingException | IllegalCharsetNameException e) {
			logger.error("Unsupported charset, returning raw value", e);
			return text;
		}
	}

	private String decodeQuotedPrintable() {
		return EncodedWord.decode(value).toString();
	}

}
