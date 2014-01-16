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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParenListParser {
	
	protected byte[] lastReadToken;
	protected TokenType lastTokenType;

	public enum TokenType {
		STRING, NIL, LIST, DIGIT, ATOM
	}

	protected static final Logger logger = LoggerFactory
			.getLogger(ParenListParser.class);
	public ParenListParser() {
		super();
	}

	private char charAt(byte[] bytes, int i) {
		return (char) bytes[i];
	}

	protected byte[] substring(byte[] bytes, int start, int end) {
		byte[] ret = new byte[end - start];
		System.arraycopy(bytes, start, ret, 0, ret.length);
		return ret;
	}

	private int indexOf(byte[] bytes, char c, int pos) {
		int idx = pos;
		while (charAt(bytes, idx) != c) {
			idx++;
		}
		return idx;
	}

	protected boolean startsWith(byte[] d, String string) {
		if (d.length < string.length()) {
			return false;
		}
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) != d[i]) {
				return false;
			}
		}
		return true;
	}

	public int consumeToken(int parsePosition, byte[] s) {
		if (parsePosition >= s.length) {
			return parsePosition;
		}
		int cur = parsePosition;
		while (Character.isSpaceChar(charAt(s, cur))) {
			cur++;
		}

		switch (charAt(s, cur)) {
		case 'N':
			lastReadToken = "NIL".getBytes();
			lastTokenType = TokenType.NIL;
			return cur + 3;
		case '"':
			int last = indexOf(s, '"', cur + 1);
			lastReadToken = substring(s, cur + 1, last);
			lastTokenType = TokenType.STRING;
			return last + 1;
		case '(':
			int close = ParenMatcher.closingParenIndex(s, cur);
			lastReadToken = substring(s, cur + 1, close);
			lastTokenType = TokenType.LIST;
			return close + 1;
		case '{':
			int size = cur + 1;
			while (charAt(s, size) != '}') {
				size++;
			}
			int bytes = Integer
					.parseInt(new String(substring(s, cur + 1, size)));
			int atomStart = size + 1;
			// +1 pattern, don't ask
			if (charAt(s, atomStart) == '}') {
				atomStart++;
			}
			byte[] out = new byte[bytes];
			System.arraycopy(s, atomStart, out, 0, bytes);
			lastReadToken = out;
			lastTokenType = TokenType.ATOM;
			return atomStart + bytes;
		default:
			// number
			int digit = cur;
			while (Character.isDigit(charAt(s, digit))) {
				digit++;
			}
			lastReadToken = substring(s, cur, digit);
			lastTokenType = TokenType.DIGIT;
			return digit + 1;
		}
	}

	public byte[] getLastReadToken() {
		return lastReadToken;
	}

	public TokenType getLastTokenType() {
		return lastTokenType;
	}

}
