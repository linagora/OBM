/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Ristretto Mail API.
 *
 * The Initial Developers of the Original Code are
 * Timo Stich and Frederik Dietz.
 * Portions created by the Initial Developers are Copyright (C) 2004
 * All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.columba.ristretto.coder;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of QuotedPrintable en- and decoding methods.
 * QuotedPrintable encoding is used to transform a text that contains
 * non-US-ASCII characters into a US-ASCII compatible text while
 * maintaining human readability as far as possible.
 * <br>
 * <b>RFC(s):</b> 2045
 */
public class QuotedPrintable {

    /** JDK 1.4+ logging framework logger, used for logging. */
    private static final Logger LOG = Logger.getLogger("org.columba.ristretto.coder");


	private static final char[] hexTable =
		{
			'0',
			'1',
			'2',
			'3',
			'4',
			'5',
			'6',
			'7',
			'8',
			'9',
			'A',
			'B',
			'C',
			'D',
			'E',
			'F' };

	private static final Pattern packPattern =
		Pattern.compile("=([0-9a-fA-F\r][0-9a-fA-F\n])");

	/**
	 * Decodes a QuotedPrintable encoded text 
	 * to its original form using the given Charset.
	 * 
	* @param input The QuotedPrintable encoded text in form of a {@link CharSequence}
	 * @param charset The {@link Charset} of the decoded text
	 * @return {@link StringBuffer} using the given charset
	 */
	public static CharSequence decode(CharSequence input, Charset charset) {
		ByteBuffer buffer = ByteBuffer.allocate(input.length());
		Matcher matcher = packPattern.matcher(input);
		int lastMatchEnd = 0;
		String group;

		// Process found packs

		while (matcher.find()) {
			try {
				// Add the bytes from the part that isn't encoded to the buffer
				buffer.put(input.subSequence(lastMatchEnd, matcher.start()).toString().getBytes("US-ASCII"));
			} catch (UnsupportedEncodingException e) {
				// Never happens, because US-ASCII is always shipped
			    LOG.severe(e.getMessage());
			}
			group = matcher.group(1);
			if (!group.equals("\r\n")) {
				buffer.put((byte) Integer.parseInt(group, 16));
			}
			lastMatchEnd = matcher.end();
		}

		// Append the rest of the input string
		try {
			// Add the bytes from the part that isn't encoded to the buffer
			buffer.put(input.subSequence(lastMatchEnd, input.length()).toString().getBytes("US-ASCII"));
		} catch (UnsupportedEncodingException e) {
			// Never happens, because US-ASCII is always shipped
		    LOG.severe(e.getMessage());
		}
		buffer.limit(buffer.position());
		buffer.rewind();
		
		return charset.decode(buffer);
	}

	/**
 * Encodes a CharSequence with QuotedPrintable Coding. 
 * <br>
 * <b>Note:</b> The input is transformed to canonical form before encoding
 * 
 * @param input	The input text in form of a {@link CharSequence}
 * @param charset	The charactercoding that the input uses (e.g. ISO-8859-1)
 * @return a US-ASCII compatible {@link StringBuffer}
 */
public static StringBuffer encode(CharSequence input, Charset charset) {
		StringBuffer result = new StringBuffer(input.length());
		CharBuffer current = CharBuffer.allocate(1);
		ByteBuffer decoded;
		int lineLength = 0;

		for (int i = 0; i < input.length(); i++) {
			current.rewind();
			current.put(0, input.charAt(i));
			decoded = charset.encode(current);

			// chars must be encoded when not :
			// 33 <= c <=60; 62 <= c <= 126 (literal characters)
			// c = {9,32} but not at the end of a line or if they are char at linePos 74
			// because a soft linebreak will follow (whitespaces)
			// c = {\r,\n} (linebreak)
			while( decoded.remaining() != 0) {
			byte next = decoded.get();
			
			if (((next == '\t' || next == ' ')
				&& !(lineLength == 74 || input.charAt(i + 1) == '\r' || input.charAt(i + 1) == '\n'))
				|| (next >= 33
					&& next != 61
					&& next <= 126)) {
				result.append((char) next);
				lineLength++;
			} else if (next == '\r') {
				result.append("\r\n");
				i++;
				lineLength = 0;
			} else {
				if (lineLength > 71) {
					result.append("=\r\n");
					lineLength = 0;
				}
				result.append('=');
				result.append(toHexString(next));
				lineLength += 3;
			}

			if (lineLength > 74) {
				result.append("=\r\n");
				lineLength = 0;
			}
		}
		}

		return result;
	}

	/**
	 * Converts a byte-value into a hex number
	 * 
	 * @param in
	 * @return the hex number
	 */
	private static char[] toHexString(byte in) {
		char[] result = new char[2];
		int value;
		if (in < 0) {
			value = 0x080 | (0x07f & in);
		} else {
			value = in;
		}

		int hi = value / 16;
		int lo = value % 16;

		result[0] = hexTable[hi];
		result[1] = hexTable[lo];

		return result;
	}

}
