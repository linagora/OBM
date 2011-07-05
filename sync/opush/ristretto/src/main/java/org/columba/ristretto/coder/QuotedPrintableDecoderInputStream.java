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
 * FilterInputStream that decodes a quoted-printable encoded characterstream.
 * <br>
 * <b>RFC(s):</b> 2045
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class QuotedPrintableDecoderInputStream extends FilterInputStream {
	
	/**
	 * Constructs a QuotedPrinatbleDecoderInputStream.
	 * 
	 * @param arg0 the quoted-printable encoded characterstream
	 */
	public QuotedPrintableDecoderInputStream(InputStream arg0) {
		super(arg0);
	}
	
	/**
	 * Read a quoted-printable pack wich is of the form
	 * =[2-digit hex-number] and decode it to one 8-bit value. 
	 * 
	 * @return number of characters decoded
	 * @throws IOException
	 */
	private int readNextOctet() throws IOException {
		int read = in.read();
		if( read == -1) return -1;
		
		if( read == '=') {
			int first = in.read();
			switch( first ) {
				case('\r') : {
					in.read(); // read '\n'
					return readNextOctet();
				}
				
				case('\n') : {
					return readNextOctet();
				}
				
				default : {
					int second = in.read();
					char[] number = { (char) first, (char) second };
					try {
						return Integer.parseInt( new String(number), 16);
					} catch (NumberFormatException e) {
						// Invalid QuotedPrintable Encoding
						return '?';
					}
				}
			}
			
		} else {
			return read;
		}
	}
	

	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		return readNextOctet();
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
