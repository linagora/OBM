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
 * FilterInputStream that encodes a bytestream into a base64 encoded charstream.
 * 
 * <br>
 * <b>RFC(s):</b> 2045
 *
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class Base64EncoderInputStream extends  FilterInputStream {


	private static final byte[] table = { 
		 65, 66, 67, 68, 69, 70, 71, 72, 73, 74,
		 75, 76, 77, 78, 79, 80, 81, 82, 83, 84,
		 85, 86, 87, 88, 89, 90, 97, 98, 99,100,
		101,102,103,104,105,106,107,108,109,110,
		111,112,113,114,115,116,117,118,119,120,
		121,122, 48, 49, 50, 51, 52, 53, 54, 55,
		 56, 57, 43, 47 };        

	private byte[] outBytes;
	private byte[] inBytes;
	private int blockCount;
	private int pos;
	private int available;

	/**
	 * Constructs a Base64EncoderInputStream.
	 * 
	 * @param in the input bytestream
	 */
    public Base64EncoderInputStream( InputStream in )
    {
    	super(in);
		outBytes = new byte[4];
		inBytes = new byte[3];
    }


	
	/**
	 * Reads 3 bytes from the inputstream and decodes it
	 * into 4 characters encoded in base64.
	 * 
	 * @return number of bytes read
	 * @throws IOException
	 */
	private int encodeNextPack() throws IOException {		
		if( blockCount == 24) {
			outBytes[0] = '\r';
			outBytes[1] = '\n';
			blockCount = 0;
			return 2;
		}

		int read = in.read( inBytes );
		
		if( read == 3 ) {
            outBytes[0] = table[(byte)(0x03F & (inBytes[0]>>2))];
            outBytes[1] = table[(byte)((0x03F & (inBytes[0]<<4)) | (0x00F & (inBytes[1]>>4)))];            
            outBytes[2] = table[(byte)((0x03F & (inBytes[1]<<2)) | (0x003 & (inBytes[2]>>6)))];            
            outBytes[3] = table[(byte)(0x03F & inBytes[2])];

            blockCount++;
		} else if (read > 0){
		
            outBytes[0] = table[(byte)(0x03F & (inBytes[0]>>2))];
            
            if( read == 2 ) {
	            outBytes[1] = table[(byte)((0x03F & (inBytes[0]<<4)) | (0x00F & (inBytes[1]>>4)))];            
    	        outBytes[2] = table[(byte)(0x03F & (inBytes[1]<<2) )];
        	    outBytes[3] = '=';        	    
            } else {
	            outBytes[1] = table[(byte)(0x03F & (inBytes[0]<<4))];
    	        outBytes[2] = '=';
        	    outBytes[3] = '=';
            }
		} else {
			return -1;
		}
		
		return 4;
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		if( pos == available ) {
			available = encodeNextPack();
			pos = 0;
		}
		
		if( available == -1) return -1;
		
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
	

	/**
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		return (int) (in.available() * 1.33);  
	}

}
