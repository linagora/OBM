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
		return currentPosition - 1;
	}

}
