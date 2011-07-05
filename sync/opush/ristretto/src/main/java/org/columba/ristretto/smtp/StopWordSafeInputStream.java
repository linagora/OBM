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
package org.columba.ristretto.smtp;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * FilterInputStream that safely wraps stop words. A stop word is CR LF '.' CR
 * LF. If a message is sent that contains stop words they must be encoded so
 * that the server continues receiving. This is done by adding a '.'. (See
 * RFC2821)
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class StopWordSafeInputStream extends FilterInputStream {

	private static final int CLRF = 0;

	private static final int NORMAL = 1;

	private static final int BUFFER = 2;

	private int buffer;

	private int mode;

	/**
	 * Constructs a StopWordSafeInputStream.
	 * 
	 * @param arg0
	 */
	public StopWordSafeInputStream(InputStream arg0) {
		super(arg0);
		mode = CLRF;
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		if (mode == BUFFER) {
			mode = NORMAL;
			return buffer;
		}

		buffer = in.read();

		if (mode == CLRF && buffer == '.') {
			mode = BUFFER;

			// insert a . before
			return '.';
		}

		if (buffer == '\n') {
			mode = CLRF;
		} else {
			mode = NORMAL;
		}

		return buffer;
	}

	/**
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] arg0, int arg1, int arg2) throws IOException {
		int next;
		for (int i = 0; i < arg2; i++) {
			next = read();
			if (next == -1) {
				if (i == 0) {
					return -1;
				} else {
					return i;
				}
			}
			arg0[arg1 + i] = (byte) next;
		}
		return arg2;
	}
}
