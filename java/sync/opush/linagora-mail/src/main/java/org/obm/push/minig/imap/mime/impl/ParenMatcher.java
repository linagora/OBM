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

public class ParenMatcher {

	private static char charAt(byte[] bytes, int i) {
		return (char) bytes[i];
	}

	private static int indexOf(byte[] bytes, char c, int pos) {
		int idx = pos;
		while (charAt(bytes, idx) != c) {
			idx++;
		}
		return idx;
	}

	private static byte[] substring(byte[] bytes, int start, int end) {
		byte[] ret = new byte[end - start];
		System.arraycopy(bytes, start, ret, 0, ret.length);
		return ret;
	}

	public static final int closingParenIndex(byte[] bs, int parsePosition) {
		int open = 1;
		int currentPosition = parsePosition + 1;
		while (currentPosition < bs.length && open != 0) {
			char c = charAt(bs, currentPosition);
			if (c == '"') {
				currentPosition = indexOf(bs, '"', currentPosition + 1) + 1;
			} else if (c == '{') {
				int size = currentPosition + 1;
				while (Character.isDigit(charAt(bs, size))) {
					size++;
				}
				int bytes = Integer.parseInt(new String(substring(bs,
						currentPosition + 1, size)));
				// 2 times for '}' added by another minig crap
				if (charAt(bs, size) == '}') {
					size++;
				}
				if (charAt(bs, size) == '}') {
					size++;
				}
				int atomStart = size;
				currentPosition = atomStart + bytes;
			} else {
				if (c == '(') {
					open++;
				} else if (c == ')') {
					open--;
				}
				currentPosition++;
			}
		}
		if (open == 0) {
			return currentPosition - 1;
		}
		throw new IllegalArgumentException("No matching bracket found");
	}

}
