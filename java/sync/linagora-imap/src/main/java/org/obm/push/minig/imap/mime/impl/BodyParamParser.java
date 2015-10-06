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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obm.push.mail.EncodedWord;
import org.obm.push.mail.mime.BodyParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

public class BodyParamParser {
	
	public static BodyParam parse(String key, String value) {
		return new BodyParamParser(key, value).parse();
	}

	private static final Pattern ASTERIX_NUMBER_ASTERIX_END_PATTERN = Pattern.compile("(.*)\\*(\\d+)\\*$");
	private static final Optional<Integer> ASTERIX_ONLY_INDEX = Optional.of(1);
	
	private final String key;
	private final String value;
	
	private BodyParamParser(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	public BodyParam parse() {
		if (key.endsWith("*")) { 
			Matcher matcher = ASTERIX_NUMBER_ASTERIX_END_PATTERN.matcher(key);
			ValueWithCharsetDecoder decoder = new ValueWithCharsetDecoder(value);
			if (matcher.matches()) {
				String decodedKey = matcher.group(1).toLowerCase().trim();
				Optional<Integer> groupIndex = Optional.of(Integer.valueOf(matcher.group(2)));
				return new BodyParam(decodedKey, decoder.text, decoder.charset, groupIndex);
			}
			String decodedKey = key.substring(0, key.length() - 1);
			return new BodyParam(decodedKey, decoder.text, decoder.charset, ASTERIX_ONLY_INDEX);
		} else {
			return new BodyParam(key, decodeQuotedPrintable());
		}
	}

	private String decodeQuotedPrintable() {
		return EncodedWord.decode(value).toString();
	}

	private static class ValueWithCharsetDecoder {
		
		private static final Logger logger = LoggerFactory.getLogger(ValueWithCharsetDecoder.class);
		
		final Optional<String> charset;
		final String text;
		
		public ValueWithCharsetDecoder(String value) {
			int firstQuote = value.indexOf("'");
			int secondQuote = value.indexOf("'", firstQuote + 1);

			charset = parseCharset(value, firstQuote);
			text = value.substring(secondQuote + 1);
		}

		private Optional<String> parseCharset(String value, int firstQuote) {
			try {
				String charsetName = value.substring(0, firstQuote);
				return Optional.of(charsetName);
			} catch (StringIndexOutOfBoundsException e) {
				logger.warn("The charset is not present or cannot be parsed for: " + value);
				return Optional.absent();
			}
		}
	}
}
