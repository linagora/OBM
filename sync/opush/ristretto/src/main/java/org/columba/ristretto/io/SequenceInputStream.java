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
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * An InputStream that concats InputStreams.
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
public class SequenceInputStream extends InputStream {

	List<InputStream> streams;
	int index;

	/**
	 * Construct the SequenceInputStream.
	 * 
	 * @param e
	 *            a list of InputStreams to concat
	 */
	public SequenceInputStream(List<InputStream> e) {
		streams = e;
	}

	/**
	 * Construct the SequenceInputStream.
	 * 
	 * @param s1
	 * @param s2
	 */
	public SequenceInputStream(InputStream s1, InputStream s2) {
		streams = Arrays.asList(new InputStream[] { s1, s2 });
	}

	public void addStream(InputStream in) {
		streams.add(in);
	}

	/**
	 * @see java.io.InputStream#read()
	 */
	public int read() throws IOException {
		int read = ((InputStream) streams.get(index)).read();
		if (read == -1 && index < streams.size() - 1) {
			index++;
			read = ((InputStream) streams.get(index)).read();
		}

		return read;
	}

	/**
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		int read = ((InputStream) streams.get(index)).read(b, off, len);

		while (read != len && index < streams.size() - 1) {
			if (read == -1)
				read = 0;
			index++;
			int nextread = streams.get(index).read(b, off
					+ read, len - read);

			if (nextread != -1)
				read += nextread;
		}

		return read;
	}

	/**
	 * @see java.io.InputStream#available()
	 */
	public int available() throws IOException {
		int size = 0;
		for (int i = 0; i < streams.size(); i++) {
			size += streams.get(i).available();
		}
		return size;
	}

	/**
	 * @see java.io.InputStream#close()
	 */
	public void close() throws IOException {
		for (InputStream is : streams) {
			is.close();
		}
	}

}
