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
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;

import org.minig.imap.EncodedWord;
import org.obm.push.mail.mime.BodyParam;

import com.google.common.base.Function;


public class BodyParamParser {
	
	public static BodyParam parse(String key, String value) {
		return new BodyParamParser(key, value, new Function<String, String>() {
			@Override
			public String apply(String input) {
				return input;
			}
		}).parse();
	}
	
	private final String key;
	private final String value;
	private String decodedKey;
	private String decodedValue;
	private final Function<String, String> keyRewriter;
	
	public BodyParamParser(String key, String value, Function<String, String> keyRewriter) {
		this.key = key;
		this.value = value;
		this.keyRewriter = keyRewriter;
	}
	
	public BodyParam parse() {
		if (key.endsWith("*")) {
			decodedKey = key.substring(0, key.length() - 1);
			decodedValue = decodeAsterixEncodedValue();
		} else {
			decodedKey = key;
			decodedValue = decodeQuotedPrintable();
		}
		return new BodyParam(keyRewriter.apply(decodedKey), decodedValue);
	}
	
	
	private String decodeAsterixEncodedValue() {
		final int firstQuote = value.indexOf("'");
		final int secondQuote = value.indexOf("'", firstQuote + 1);
		final String charsetName = value.substring(0, firstQuote);
		final String text = value.substring(secondQuote + 1);
		try {
			Charset charset = Charset.forName(charsetName);
			return URLDecoder.decode(text, charset.displayName());
		} catch (UnsupportedEncodingException e) {
		} catch (IllegalCharsetNameException e) {
		} catch (IllegalArgumentException e) {
		}
		return text;
	}

	private String decodeQuotedPrintable() {
		return EncodedWord.decode(value).toString();
	}

}
