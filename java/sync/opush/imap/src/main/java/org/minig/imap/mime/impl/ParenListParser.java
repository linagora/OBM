/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.minig.imap.mime.impl;

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
			// FIXME
			if (startsWith(lastReadToken, "\"TEXT\"")) {
				lastReadToken = ("(" + new String(lastReadToken) + ")")
						.getBytes();
			}
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
