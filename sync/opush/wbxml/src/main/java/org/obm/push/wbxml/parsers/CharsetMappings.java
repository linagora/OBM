package org.obm.push.wbxml.parsers;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps MIBEnum charsets to encodings
 * 
 * See <code>http://www.iana.org/assignments/character-sets</code>
 * 
 * @author tom
 *
 */
public class CharsetMappings {

	private Map<Integer, String> mibEnumToCharset;

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
