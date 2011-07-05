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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * FilterInputStream that decodes a base64 encoded stream.
 * 
 * <br>
 * <b>RFC(s):</b> 2045
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class Base64DecoderInputStream extends FilterInputStream {

	// US-ASCII to Base64 Table

	private static byte[] table =
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

	private int[] outBytes;
	private byte[] inBytes;
	private int pos;
	private int available;

	/**
	 * Constructs a Base64DecoderInputStream.
	 * 
	 * @param in the base64 encoded inputstream
	 */
	public Base64DecoderInputStream(InputStream in) {
		super(new CRLFFilterInputStream(in));
		outBytes = new int[3];
		inBytes = new byte[4];
	}

	/**
	 * Read 4 characters = 1 base64 pack and decode it into
	 * the ouBytes buffer.
	 * 
	 * @return number of characters read
	 * @throws IOException
	 */
	private int readNextPack() throws IOException {
		int[] lookedUp = new int[4];

		//int block;
		int read = in.read(inBytes);

		if (read != 4)
			return -1;

		lookedUp[0] = table[inBytes[0]];
		lookedUp[1] = table[inBytes[1]];
		lookedUp[2] = table[inBytes[2]];
		lookedUp[3] = table[inBytes[3]];

		outBytes[0] = 0x0ff & ((lookedUp[0] << 2) | (lookedUp[1] >> 4));
		outBytes[1] = 0x0ff & ((lookedUp[1] << 4) | (lookedUp[2] >> 2));
		outBytes[2] = 0x0ff & ((lookedUp[2] << 6) | (lookedUp[3]));


		// check if this pack is padded
		if (inBytes[2] == 61) {
			return 1;
		} 
		if (inBytes[3] == 61) {
			return 2;
		}
				
		return 3;
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {		
		// Do we need to read the next pack from the inputstream?
		if( pos == available ) {
			available = readNextPack();
			pos = 0;
		}
		
		// Are we at the end of the input 
		if( available == -1) return -1;
		
		// return next byte
		return outBytes[pos++];
	}

	/**
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		int next;
		for( int i=0; i<arg2; i++) {
			next = read();
			if( next == -1 ) {
				if( i == 0 ) {
					return -1;
				} else {
					return i;
				}
			}
			arg0[arg1+i] = (byte) next;
		}
		return arg2;
	}
	
}