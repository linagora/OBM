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
package org.obm.push;

public class Base64 {

	static private final int BASELENGTH = 255;
	static private final int LOOKUPLENGTH = 64;
	static private final int FOURBYTE = 4;
	static private final char PAD = '=';
	static final private byte[] base64Alphabet = new byte[BASELENGTH];
	static final private char[] lookUpBase64Alphabet = new char[LOOKUPLENGTH];

	static {

		for (int i = 0; i < BASELENGTH; i++) {
			base64Alphabet[i] = -1;
		}
		for (int i = 'Z'; i >= 'A'; i--) {
			base64Alphabet[i] = (byte) (i - 'A');
		}
		for (int i = 'z'; i >= 'a'; i--) {
			base64Alphabet[i] = (byte) (i - 'a' + 26);
		}

		for (int i = '9'; i >= '0'; i--) {
			base64Alphabet[i] = (byte) (i - '0' + 52);
		}

		base64Alphabet['+'] = 62;
		base64Alphabet['/'] = 63;

		for (int i = 0; i <= 25; i++)
			lookUpBase64Alphabet[i] = (char) ('A' + i);

		for (int i = 26, j = 0; i <= 51; i++, j++)
			lookUpBase64Alphabet[i] = (char) ('a' + j);

		for (int i = 52, j = 0; i <= 61; i++, j++)
			lookUpBase64Alphabet[i] = (char) ('0' + j);
		lookUpBase64Alphabet[62] = '+';
		lookUpBase64Alphabet[63] = '/';

	}

	private static final boolean isWhiteSpace(byte octect) {
		return (octect == 0x20 || octect == 0xd || octect == 0xa || octect == 0x9);
	}

	private static final boolean isPad(byte octect) {
		return (octect == PAD);
	}

	private static int removeWhiteSpace(byte[] data) {
		if (data == null)
			return 0;

		// count characters that's not whitespace
		int newSize = 0;
		int len = data.length;
		for (int i = 0; i < len; i++) {
			byte dataS = data[i];
			if (!isWhiteSpace(dataS))
				data[newSize++] = dataS;
		}
		return newSize;
	}

	public final static byte[] decode(byte[] base64Data)
			throws Exception {
		// remove white spaces
		int len = removeWhiteSpace(base64Data);

		if (len % FOURBYTE != 0) {
			throw new Exception("decoding.divisible.four");
			// should be divisible by four
		}

		int numberQuadruple = (len / FOURBYTE);

		if (numberQuadruple == 0)
			return new byte[0];

		byte decodedData[] = null;
		byte b1 = 0, b2 = 0, b3 = 0, b4 = 0;

		int i = 0;
		int encodedIndex = 0;
		int dataIndex = 0;

		// decodedData = new byte[ (numberQuadruple)*3];
		dataIndex = (numberQuadruple - 1) * 4;
		encodedIndex = (numberQuadruple - 1) * 3;
		// first last bits.
		b1 = base64Alphabet[base64Data[dataIndex++]];
		b2 = base64Alphabet[base64Data[dataIndex++]];
		if ((b1 == -1) || (b2 == -1)) {
			// if found "no data" just return null
			throw new Exception("decoding.general");
		}

		byte d3, d4;
		b3 = base64Alphabet[d3 = base64Data[dataIndex++]];
		b4 = base64Alphabet[d4 = base64Data[dataIndex++]];
		if ((b3 == -1) || (b4 == -1)) {
			// Check if they are PAD characters
			if (isPad(d3) && isPad(d4)) { // Two PAD e.g. 3c[Pad][Pad]
				if ((b2 & 0xf) != 0)// last 4 bits should be zero
					throw new Exception("decoding.general");
				decodedData = new byte[encodedIndex + 1];
				decodedData[encodedIndex] = (byte) (b1 << 2 | b2 >> 4);
			} else if (!isPad(d3) && isPad(d4)) { // One PAD e.g. 3cQ[Pad]
				if ((b3 & 0x3) != 0)// last 2 bits should be zero
					throw new Exception("decoding.general");
				decodedData = new byte[encodedIndex + 2];
				decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
				decodedData[encodedIndex] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
			} else {
				throw new Exception("decoding.general");// an error like
				// "3c[Pad]r", "3cdX",
				// "3cXd", "3cXX" where
				// X is non data
			}
		} else {
			// No PAD e.g 3cQl
			decodedData = new byte[encodedIndex + 3];
			decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
			decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
			decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);
		}
		encodedIndex = 0;
		dataIndex = 0;
		// the begin
		for (i = numberQuadruple - 1; i > 0; i--) {
			b1 = base64Alphabet[base64Data[dataIndex++]];
			b2 = base64Alphabet[base64Data[dataIndex++]];
			b3 = base64Alphabet[base64Data[dataIndex++]];
			b4 = base64Alphabet[base64Data[dataIndex++]];

			if ((b1 == -1) || (b2 == -1) || (b3 == -1) || (b4 == -1)) {
				throw new Exception("decoding.general");// if found "no data"
				// just return null
			}

			decodedData[encodedIndex++] = (byte) (b1 << 2 | b2 >> 4);
			decodedData[encodedIndex++] = (byte) (((b2 & 0xf) << 4) | ((b3 >> 2) & 0xf));
			decodedData[encodedIndex++] = (byte) (b3 << 6 | b4);
		}
		return decodedData;
	}
}
