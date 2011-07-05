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
 * FilterInputStream that converts CR or LF lineendings to CRLF.
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class CanonizeFilterInputStream extends FilterInputStream {
	
	private static final int NOOP = 0;
	private static final int CRFOUND = 1;
	private static final int PRINTLF = 2;
	private static final int PRINTBUFFER = 3;
	
	
	private int mode = 0;
	private int buffer;

	/**
	 * Constructs a CanonizeFilterInputStream.
	 * 
	 * @param in inputstream to canonize
	 */
	public CanonizeFilterInputStream(InputStream in) {
		super(in);
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		// First handle things that must be inserted in order
		// to canonize
			
		// A LF must be returned in order to complete the begin CR
		if( mode == PRINTLF ) {
			mode = NOOP;
			return '\n';
		}
		
		// Print what was in the buffer
		if( mode == PRINTBUFFER ) {
			mode = NOOP;
			return buffer;
		}
		
		// Nothing must be inserted, so we can read another byte
		// from the inputstream;
		int read = in.read();
		
		// if we read a CR next char must be a LF so go in CRFOUND mode
		if( read == '\r' ) {
			mode = CRFOUND;
			return read;
		}		

		// if we are in CRFOUND mode but the next char wasn't a LF
		// save the read byte in the buffer and insert a LF
		// Go to PRINTBUFFER mode so the read byte is read next.
		if( read != '\n' && mode == CRFOUND ) {
			mode = PRINTBUFFER;
			buffer = read;
			return '\n';
		} 
				
		// if we read a LF without being in CRFOUND mode we have to
		// insert a CR and ensure that the next read byte is a LF.
		if( read == '\n' && mode != CRFOUND ) {
			mode = PRINTLF;
			return '\r';
		}

		// Everything is okay, return the read byte and go to NOOP
		mode = NOOP;
		return read; 		
	}


	/**
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		int read;
		for( int i=0; i<len ; i++) {
			read = in.read();
			if( read == -1) {
				if( i == 0 ) return -1;
				else return i;
			}
			b[off+i] = (byte) read;
		}
		
		return len;
	}

}
