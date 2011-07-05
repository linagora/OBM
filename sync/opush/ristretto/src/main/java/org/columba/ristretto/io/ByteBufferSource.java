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
package org.columba.ristretto.io;

import java.io.IOException;

/**
 * Source that wraps a byte array.
 * 
 * @author tstich
 *
 */
public class ByteBufferSource implements Source {

	private byte[] source;
	
	private int start,end;
	private int pos;	

	/**
	 * Constructs the ByteBufferSource. 
	 * 
	 * @param source
	 */
	public ByteBufferSource( byte[] source ) {
		this.source = source;
		
		pos = 0;
		start = 0;
		end = source.length; 
	}

	/**
	 * @see org.columba.ristretto.io.Source#fromActualPosition()
	 */
	public Source fromActualPosition() {
		ByteBufferSource subsource = new ByteBufferSource(source);
		subsource.start = this.start + pos;
		subsource.end = end;
		subsource.pos = 0;
		return subsource;
	}

	/**
	 * @see org.columba.ristretto.io.Source#getPosition()
	 */
	public int getPosition() {
		return pos;
	}

	/**
	 * @see org.columba.ristretto.io.Source#seek(int)
	 */
	public void seek(int position) throws IOException {
		pos = position;
	}

	/**
	 * @see org.columba.ristretto.io.Source#next()
	 */
	public char next() throws IOException {
	    // this is a trick to avoid interpreting
	    // the byte as a signed value
	    byte value = source[ start + (pos++) ];
	    int trueValue = (value & 0x080) + (value & 0x07F);
	    
	    return (char) trueValue; 
	}

	/**
	 * @see org.columba.ristretto.io.Source#isEOF()
	 */
	public boolean isEOF() {
		return pos == end;
	}

	/**
	 * @see java.lang.CharSequence#length()
	 */
	public int length() {
		return end - start;
	}

	/**
	 * @see java.lang.CharSequence#charAt(int)
	 */
	public char charAt(int pos) {
	    try {
            seek(pos);
            return next();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
	}

	/**
	 * @see org.columba.ristretto.io.Source#subSource(int, int)
	 */
	public Source subSource( int start, int end ) {
		ByteBufferSource subsource = new ByteBufferSource(source);
		subsource.start = this.start + start;
		subsource.end = this.start + end;
		subsource.pos = 0;
		return subsource;
	}
	
	/**
	 * @see java.lang.CharSequence#subSequence(int, int)
	 */
	public CharSequence subSequence(int start, int end) {
		return subSource( start, end);
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder buffer = new StringBuilder(length());
		for( int i=0; i < length(); i++) {
			buffer.append(charAt(i));
		}
		return buffer.toString();
	}

	/**
	 * @see org.columba.ristretto.io.Source#close()
	 */
	public void close() throws IOException {
		source = null;
	}

	/**
	 * @see org.columba.ristretto.io.Source#deepClose()
	 */
	public void deepClose() throws IOException {
		close();
	}
}
