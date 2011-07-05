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
import java.nio.IntBuffer;


/**
 * FilterInputStream that encodes a characterstream in quoted-printable.
 * <br>
 * <b>Note:</b> The input is transformed to canonical form before encoding.
 * (lineendings are converted to CRLF)
 * <br>
 * <b>RFC(s):</b> 2045
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class QuotedPrintableEncoderInputStream extends FilterInputStream {

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

	private IntBuffer outBytes;

	private int pos;
	private int available;
	private int lineLength;

	/**
	 * Constructs a QuotedPrintableEncoderInputStream.
	 * 
	 * @param arg0 the characterstream that will be encoded
	 */
	public QuotedPrintableEncoderInputStream(InputStream arg0) {
		super(arg0);

		outBytes = IntBuffer.allocate(10);

	}

	
	/**
	 * The next char is read from the inputstream and
	 * is encoded in quoted-printable if necessary.
	 * See RFC 2045 for details on how quoted-printable
	 * decoding works.
	 * 
	 * @return the number of output characters.
	 * @throws IOException
	 */
	private int processNextInput() throws IOException {
		outBytes.clear();
		int read = in.read();

		if (read == -1)
			return -1;

		// chars must be encoded when not :
		// 33 <= c <=60; 62 <= c <= 126 (literal characters)
		// c = {9,32} but not at the end of a line or if they are char at linePos 74
		// because a soft linebreak will follow (whitespaces)
		// c = {\r,\n} (linebreak)
		if (read == '\t' || read == ' ') {
			// if linelength == 73 always encode the WS
			if (lineLength >= 74) {
				outBytes.put('=');
				outBytes.put(toHexString(read));
				lineLength += 3;
			} else {
				// Check the next character
				int next = in.read();
				if (next != -1) {
					// if it is a CRLF encode the WS
					if (next == '\r' || next == '\n') {
						outBytes.put('=');
						outBytes.put(toHexString(read));
						lineLength += 3;
					} else {
						// else no need to encode the WS
						outBytes.put( read );
						lineLength++;
					}
					
					// set the second read character to read
					// so the following code will handle the
					// encoding of it
					read = next;
				}
			}
		}
		
		// Check if read must be encoded
		if (read >= 33 && read != 61 && read <= 126) {
			outBytes.put(read);
			lineLength++;
		} else if( read == '\r' ){
			outBytes.put('\r');
			outBytes.put('\n');
			// On \r a \n MUST follow -> we added it already
			in.read();
			lineLength = 0;						
		} else if( read == '\n' ){
			// Normalize the stream and convert \n to \r\n
			outBytes.put('\r');
			outBytes.put('\n');
			lineLength = 0;						
		} else {
			outBytes.put('=');
			outBytes.put(toHexString(read));
			lineLength += 3;
		}
		
		// Insert Softlinebreak if the linelength > 74
		if( lineLength >= 74) {
			outBytes.put('=');			
			outBytes.put('\r');
			outBytes.put('\n');
			lineLength = 0;
		}
		
		return outBytes.position();
	} 
	
	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		if( pos == available ) {
			available = processNextInput();
			pos = 0;
		}
		
		if( available == -1) return -1;
		
		return outBytes.get(pos++);
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
	

	/**
	 * Converts a byte-value into a hex number
	 * 
	 * @param in
	 * @return the hex number
	 */
	private int[] toHexString(int in) {
		int[] result = new int[2];
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
