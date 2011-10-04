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
package org.minig.imap.impl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Implementation of base64 en- and decoding methods.
 * Base64 coding is used to efficiently represent a ByteBuffer as
 * an US-ASCII compatible String.
 * <br>
 * <b>Note:</b> The size of the coded input will increase by about 33%. 
 * 
 * <br>
 * <b>RFC(s):</b> 2045
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class Base64 {
	private static final char[] etable = { 
		 65, 66, 67, 68, 69, 70, 71, 72, 73, 74,
		 75, 76, 77, 78, 79, 80, 81, 82, 83, 84,
		 85, 86, 87, 88, 89, 90, 97, 98, 99,100,
		101,102,103,104,105,106,107,108,109,110,
		111,112,113,114,115,116,117,118,119,120,
		121,122, 48, 49, 50, 51, 52, 53, 54, 55,
		 56, 57, 43, 47 };        

	
	// US-ASCII to Base64 Table

	private static byte[] dtable =
		{
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			000,
			62,
			000,
			000,
			000,
			63,
			52,
			53,
		// + / 0..
		54, 55, 56, 57, 58, 59, 60, 61, 000, 000, // ..9
		000, 0, 000, 000, 000, 0, 1, 2, 3, 4, // = A..
		5, 6, 7, 8, 9, 10, 11, 12, 13, 14, // ..
		15, 16, 17, 18, 19, 20, 21, 22, 23, 24, //
		25, 000, 000, 000, 000, 000, 000, 26, 27, 28, // ..Z a..
		29, 30, 31, 32, 33, 34, 35, 36, 37, 38, // ..
		39, 40, 41, 42, 43, 44, 45, 46, 47, 48, // ..
		49, 50, 51, 000, 000, 000, 000, 000 }; // ..z

	/**
	 * Takes a base64 encoded String and decodes it into a ByteBuffer.
	 * If you need to have a byte array use decodeToArray.
	 * 
	 * @param input a base64 String
	 * @return the decoded ByteBuffer
	 */
	public static ByteBuffer decode(CharSequence input) {
		// Create OutBuffer with maximal possible size
		int size = (int) (input.length() * .75);
		
		byte[] outBytes = new byte[size];
		byte[] pack = new byte[4];
		int outPos = 0;
		int packSize = 0;
		int pads = 0;
		char current;

		// Decode the input
		for (int i = 0; i < input.length(); i++) {
			current = input.charAt(i);
			if (current != '\r' && current != '\n') {
				pack[packSize++] = (byte) current;
				if (current == '=') {
					pads++;
				}
			}
			if (packSize == 4) {
				outBytes[outPos++] =
					(byte) ((dtable[pack[0]] << 2) | (dtable[pack[1]] >> 4));
				if (pads < 2)
					outBytes[outPos++] =
						(byte) ((dtable[pack[1]] << 4) | (dtable[pack[2]] >> 2));
				if (pads < 1)
					outBytes[outPos++] =
						(byte) ((dtable[pack[2]] << 6) | (dtable[pack[3]]));
				packSize = 0;
				if( pads != 0) break;
			}
		}

		// Shorten the outBytes to the exact size if nessecary
		return ByteBuffer.wrap(outBytes,0,outPos);
	}
	
	/**
	 * Takes a base64 encoded String and decodes it into a byte[].
	 * 
	 * @param input
	 * @return the base64 encoded input
	 */
	public static byte[] decodeToArray(CharSequence input) {
		ByteBuffer buffer = Base64.decode(input);
		if( buffer.limit() == buffer.capacity()) return buffer.array();
		
		// Cut of trailing zeros
		byte[] result = new byte[buffer.limit()];
		System.arraycopy(buffer.array(),0,result,0,buffer.limit());
		return result;
	}
	
	/**
	 * Encodes a ByteBuffer in base64 code.
	 * If wrap is true, the output will be wrapped
	 * after 76 characters.
	 * 
	 * @param input the raw bytes
	 * @param wrap wraps the output after 76 characters if true
	 * @return the base64 representation of the input
	 */
	public static StringBuilder encode( ByteBuffer input, boolean wrap )  {
		int lastPackSize = input.limit() % 3;
		int estimatedEncodedSize = ((int) (input.limit() * 1.333 + .5)) +2;
		StringBuilder result = new StringBuilder(estimatedEncodedSize + (estimatedEncodedSize / 76) * 2);
		int packsPerLine = 0;
		int i;

		//Encode
		for( i=0; i<(input.limit() - lastPackSize); i+=3) {
			result.append( etable[(byte)(0x03F & (input.get(i)>>2))] );
			result.append( etable[(byte)((0x03F & (input.get(i)<<4)) | (0x00F & (input.get(i+1)>>4)))]);
			result.append( etable[(byte)((0x03F & (input.get(i+1)<<2)) | (0x003 & (input.get(i+2)>>6)))]);
			result.append( etable[(byte)(0x03F & input.get(i+2))]);
			
			// No more than 76 chars/line
			packsPerLine++;
			if( packsPerLine == 25 && wrap) {
				result.append("\r\n");
				packsPerLine = 0;
			}
		}
		
		// Handle pads if necessary
		if( lastPackSize == 2 ) {
			result.append( etable[(byte)(0x03F & (input.get(i)>>2))] );
			result.append( etable[(byte)((0x03F & (input.get(i)<<4)) | (0x00F & (input.get(i+1)>>4)))]);
			result.append( etable[(byte)(0x03F & (input.get(i+1)<<2))] );
			result.append( '=');
		}
		
		if( lastPackSize == 1 ) {
			result.append( etable[(byte)(0x03F & (input.get(i)>>2))] );
			result.append( etable[(byte)(0x03F & (input.get(i)<<4))]);
			result.append( '=' );
			result.append( '=');
		}
		
		return result;
	}

	/**
	 * Takes a String that is encoded in US-ASCII and converts it to base64.
	 * <br>
	 * Convenience method that uses {@link #encode(ByteBuffer)} after
	 * getting the bytes from the string.
	 * 
	 * @param input the us-ascii input string
	 * @return StringBuilder with the encoded input  
	 */
	public static StringBuilder encode( String input ) {
		Charset charset = Charset.forName("US-ASCII");
		ByteBuffer bytes = charset.encode(input);
		return encode( bytes);
	}
	
	/**
	 * Encodes a ByteBuffer in base64.
	 * Wraps the result String after 76 characters.
	 * 
	 * @see #encode(ByteBuffer, boolean)
	 * 
	 * @param buffer
	 * @return StringBuilder with the encoded input  
	 */
	public static StringBuilder encode( ByteBuffer buffer ) {
	    return encode( buffer, true );
	}

}
