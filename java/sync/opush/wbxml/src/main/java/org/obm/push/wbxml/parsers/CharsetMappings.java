/* ***** BEGIN LICENSE BLOCK *****
 *
 * %%
 * "Copyleft" 1999, Stefan Haustein, Oberhausen, NW, Germany. 
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.wbxml.parsers;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps MIBEnum charsets to encodings
 * 
 * See <code>http://www.iana.org/assignments/character-sets</code>
 */
public class CharsetMappings {

	private final Map<Integer, String> mibEnumToCharset;

	public CharsetMappings() {
		this.mibEnumToCharset = new HashMap<Integer, String>();

		mibEnumToCharset.put(3, "US-ASCII");
		mibEnumToCharset.put(4, "ISO-8859-1");
		mibEnumToCharset.put(106, "UTF-8");
	}

	public String getCharset(int mibEnum) {
		String ret = mibEnumToCharset.get(mibEnum);
		if (ret == null) {
			ret = "UTF-8";
		}
		return ret;
	}

}
