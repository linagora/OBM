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
package org.columba.ristretto.message;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Map;

import org.columba.ristretto.io.CharSequenceSource;
import org.columba.ristretto.io.SourceInputStream;
import org.columba.ristretto.io.Streamable;

/**
 * Datastructure that stores headers. This is basically comparable to a
 * java.util.Map structure.
 * 
 * @author tstich
 * 
 */
public class Header implements Streamable, Serializable {

	private static final long serialVersionUID = 4566687882390647422L;

	private static final int MAXLINELENGTH = 78;
	private IgnoreCaseHashtable<String> header;

	private static final String[] unlimitedFields = { "received", "x-" };

	/**
	 * Constructs the Header.
	 */
	public Header() {
		header = new IgnoreCaseHashtable<String>();
	}

	/**
	 * Set the header with the specified key.
	 * 
	 * @param key
	 * @param value
	 */
	public void set(String key, Object value) {
		header.put(key, value.toString());
	}

	/**
	 * Append the value to the header with the specified key.
	 * 
	 * @param key
	 * @param value
	 */
	public void append(String key, String value) {
		String oldvalue = (String) header.get(key);
		if (oldvalue == null || !isUnlimited(key)) {
			header.put(key, value);
		} else {
			header.put(key, oldvalue + value);
		}
	}

	/**
	 * Get the header with the specified key.
	 * 
	 * @param key
	 * @return the header
	 */
	public String get(String key) {
		return (String) header.get(key);
	}

	/**
	 * 
	 * 
	 * @return the number of headers
	 */
	public int length() {
		return header.size();
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	public Object clone() {
		Header cloneHeader = new Header();
		cloneHeader.header = (IgnoreCaseHashtable<String>) this.header.clone();
		return cloneHeader;
	}

	/**
	 * @return the number of headers
	 */
	public int count() {
		return header.size();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder result = new StringBuilder();
		Enumeration<String> keys = header.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			result.append(key);
			result.append(": ");
			String val = get(key);
			if (!val.contains("\n")) {
				result.append(foldLine(val, MAXLINELENGTH - key.length() - 2));
			} else {
				result.append(val);
			}
			result.append("\r\n");
		}
		result.append("\r\n");
		return result.toString();
	}

	/**
	 * Merge the given header with this header.
	 * 
	 * @param arg
	 */
	public void merge(Header arg) {
		Enumeration<String> keys = arg.header.keys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			this.header.put(key, arg.header.get(key));
		}
	}

	private CharSequence foldLine(String line, int firstMaxLength) {
		// If line must not be broken or key was too long return line as is
		if (line.length() <= firstMaxLength || firstMaxLength <= 0) {
			return line;
		}

		// Try to find possible fold pos else return complete line
		int foldPos = line.indexOf(' ', firstMaxLength);
		if (foldPos == -1) {
			return line;
		}

		StringBuilder result = new StringBuilder(line.length() + 3);
		result.append(line.subSequence(0, foldPos));
		result.append("\r\n ");
		int lastFoldPos = foldPos;

		foldPos += MAXLINELENGTH;
		if (foldPos < line.length()) {
			foldPos = line.indexOf(' ', foldPos);
		}

		while (foldPos != -1 && foldPos < line.length()) {
			result.append(line.subSequence(lastFoldPos, foldPos));
			result.append("\r\n ");
			lastFoldPos = foldPos;

			foldPos += MAXLINELENGTH;
			if (foldPos < line.length()) {
				foldPos = line.indexOf(' ', foldPos);
			}
		}

		result.append(line.substring(lastFoldPos));

		return result;
	}

	/**
	 * @see org.columba.ristretto.io.Streamable#getInputStream()
	 */
	public InputStream getInputStream() {
		return new SourceInputStream(new CharSequenceSource(toString()));
	}

	/**
	 * Gets the keys that are stored in the header.
	 * 
	 * @return the Enumeration of keys
	 */
	public Enumeration<String> getKeys() {
		return header.keys();
	}

	private static boolean isUnlimited(String key) {
		String ignoredCase = key.toLowerCase();
		for (int i = 0; i < unlimitedFields.length; i++) {
			if (ignoredCase.startsWith(unlimitedFields[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return Returns the header.
	 */
	public Map<String, String> getHeader() {
		return header;
	}

	/**
	 * @param header
	 *            The header to set.
	 */
	public void setHeader(Map<String, String> header) {
		this.header = (IgnoreCaseHashtable<String>) header;
	}

}
