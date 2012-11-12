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
package org.obm.push.minig.imap.impl;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.commons.codec.binary.Base64;

/**
 * Copied from ristretto code
 * 
 * @author tstich
 * 
 */
public class MailboxNameUTF7Converter {

	private final static int PRINTABLE = 0;
	private final static int BASE64 = 1;

	/**
	 * Encode the mailbox name in the IMAP UTF-7 style charset.
	 * 
	 * @param mailboxName
	 * @return the IMAP UTF-7 representation
	 */
	public static String encode(String mailboxName) {
		int dataBits = 0;
		int mode = PRINTABLE;
		// Allocate a bytebuffer that must be at max
		// twice the length of characters in the string
		// because of utf-16 encoding and add 2 more
		// bytes in order to have a length that can be
		// devided by 3. This is necessary to avoid the
		// padding in base64 which is forbidden in
		// modified UTF-7 encoding.
		ByteBuffer buffer = ByteBuffer.allocate(mailboxName.length() * 4 + 4);

		StringBuilder result = new StringBuilder();
		char c;

		for (int i = 0; i < mailboxName.length(); i++) {
			c = mailboxName.charAt(i);

			if (isPrintable(c)) {
				if (mode != PRINTABLE && buffer.position() > 0) {
					// pad buffer with zero
					// logger.info("vor put oben " + buffer.capacity() +
					// ":" + buffer.remaining());
					if (buffer.position() % 3 != 0) {
						buffer.put(new byte[3 - (buffer.position() % 3)]);
					}
					// logger.info("nach put oben " + buffer.capacity() +
					// ":" + buffer.remaining());
					buffer.limit(buffer.position());
					// logger.info("nach limit " + buffer.capacity() +
					// ":" + buffer.remaining());

					// encode in base64
					String encoded = Base64.encodeBase64String(buffer.array());
					buffer.rewind();

					// do the modifications
					String rawEncoded = encoded.replace('/', ',');

					// cut the end to a valid base64 character
					// base64: 6 bits per character

					int encodedBits = rawEncoded.length() * 6;
					int superfluentChars = (encodedBits - dataBits) / 6;

					if (superfluentChars > 0) {
						rawEncoded = rawEncoded.substring(0, rawEncoded
								.length()
								- superfluentChars);
						dataBits = 0;
					}

					result.append(rawEncoded);

					// switch back to PRINTABLE mode
					mode = PRINTABLE;
					result.append('-');
					// logger.info("result so far :" + result);
				}

				// write the character
				if (c == '&') {
					result.append("&-");
				} else {
					result.append(c);
				}
			} else {
				if (mode != BASE64) {
					result.append('&');
					mode = BASE64;
				}

				try {
					if (buffer.remaining() < 2) {
						buffer.limit(buffer.limit() + (2 - buffer.remaining()));
					}
					// logger.info("vor put " + buffer.capacity() + ":" +
					// buffer.remaining());
					byte[] utfBytes = mailboxName.substring(i, i + 1).getBytes(
							"UTF-16");
					if (utfBytes[0] != -2) {
						buffer.put(utfBytes[0]);
						buffer.put(utfBytes[1]);

						dataBits += 16;
					}
					if (buffer.remaining() == 0) {
						buffer.limit(buffer.limit() + 1);
					}
					buffer.put(utfBytes[2]);
					// logger.info("nach put " + buffer.capacity() + ":"
					// + buffer.remaining());
					if (buffer.remaining() == 0) {
						buffer.limit(buffer.limit() + 1);
					}
					buffer.put(utfBytes[3]);

					dataBits += 16;
				} catch (UnsupportedEncodingException e) {
					// will never happen
				}
			}
		}

		if (mode != PRINTABLE) {
			// pad buffer with zeros
			if (buffer.remaining() == 0) {
				buffer.limit(buffer.limit() + 3);
			}
			if (buffer.position() % 3 != 0) {
				buffer.put(new byte[3 - (buffer.position() % 3)]);
			}
			buffer.limit(buffer.position());

			// encode in base64
			String encoded = Base64.encodeBase64String(buffer.array());
			buffer.rewind();

			// do the modifications
			String rawEncoded = encoded.replace('/', ',');

			// cut the end to a valid base64 character
			// base64: 6 bits per character

			int encodedBits = rawEncoded.length() * 6;
			int superfluentChars = (encodedBits - dataBits) / 6;

			if (superfluentChars > 0) {
				rawEncoded = rawEncoded.substring(0, rawEncoded.length()
						- superfluentChars);
			}

			result.append(rawEncoded);

			// switch back to PRINTABLE mode
			mode = PRINTABLE;
			result.append('-');
		}

		String returnString = result.toString();
		// logger.info("schlu�endlich :" + returnString);
		return returnString;
	}

	/**
	 * Decode the IMAP UTF-7 mailbox name to a Java String.
	 * 
	 * @param mailboxName
	 * @return the Java representation of the mailbox names
	 */
	public static String decode(String mailboxName) {
		int lastEnd = 0;
		int nextAnd = mailboxName.indexOf('&');

		// if no & is in the name no decoding must be done
		if (nextAnd == -1)
			return mailboxName;

		StringBuilder result = new StringBuilder(mailboxName.length());

		while (nextAnd != -1) {
			// add all the printable characters until this position
			result.append(mailboxName.substring(lastEnd, nextAnd));

			// can be either a switch to utf-7 or &-
			if (mailboxName.charAt(nextAnd + 1) == '-') {
				result.append('&');
				lastEnd = nextAnd + 2;
			} else {
				// find end of base64 code
				lastEnd = mailboxName.indexOf('-', nextAnd);
				int expanded = 0;

				// pad with A's for modified base64
				StringBuilder rawEncoded = new StringBuilder(mailboxName
						.substring(nextAnd + 1, lastEnd).replace(',', '/'));
				while (rawEncoded.length() % 4 != 0) {
					rawEncoded.append('A');
					expanded++;
				}

				// decode the transformed modified-base64
				ByteBuffer decoded = ByteBuffer.wrap(Base64.decodeBase64(rawEncoded.toString()));
				decoded.limit(decoded.limit() - expanded);
				if (decoded.limit() % 2 != 0) {
					// delete the end zeros
					decoded.limit(decoded.limit() - (decoded.limit() % 2));
				}

				result.append(decoded.asCharBuffer());

				lastEnd++;
			}

			nextAnd = mailboxName.indexOf('&', lastEnd);
		}

		result.append(mailboxName.substring(lastEnd));
		String returnString = result.toString();
		return returnString;
	}

	private static boolean isPrintable(char c) {
		return (c >= 0x020 && c <= 0x7e);
	}

}
