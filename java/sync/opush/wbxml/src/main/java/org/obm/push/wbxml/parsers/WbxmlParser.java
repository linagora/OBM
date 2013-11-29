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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.google.common.base.Joiner;

/**
 * A SAX-based parser for the WAP Binary XML Content Format (WBXML).
 * <p>
 * This parser does <b>not</b> implement org.xml.sax.Parser in order to avoid
 * some overhead that would probably overfloat the KVM. However, the most
 * important interface, DocumentHandler, is supported.
 * <p>
 * For full org.sax.Parser compatibility please use this class in conjunciton
 * with the wrapper class <tt>SaxWrapper</tt>.
 * <p>
 * Still Todo:
 * <ul>
 * <li>implement Processing Instructions</li>
 * <li>implement support for more than one codepages</li>
 * </ul>
 */

public class WbxmlParser {

	private static final Logger logger = LoggerFactory.getLogger(WbxmlParser.class);

	private InputStream in;
	private ContentHandler dh;
	private WbxmlExtensionHandler eh;

	private String[] attrStartTable;
	private String[] attrValueTable;
	private String[] tagTable;
	private final Map<Integer, String[]> tagsTables;
	private final Map<Integer, String[]> attrStarTables;
	private final Map<Integer, String[]> attrValueTables;
	private char[] stringTable;

	private int publicIdentifierId;
	private final Vector<String> stack = new Vector<String>();

	private String docCharset;

	public WbxmlParser() {
		this.tagsTables = new HashMap<Integer, String[]>();
		this.attrStarTables = new HashMap<Integer, String[]>();
		this.attrValueTables = new HashMap<Integer, String[]>();
		switchPage(0);
	}

	/**
	 * Sets the tag table for a given page. The first string in the array
	 * defines tag 5, the second tag 6 etc. Currently, only page 0 is supported
	 */
	public void setTagTable(int page, String[] tagTable) {
		tagsTables.put(page, tagTable);
	}

	/**
	 * Sets the attribute start Table for a given page. The first string in the
	 * array defines attribute 5, the second attribute 6 etc. Please use the
	 * character '=' (without quote!) as delimiter between the attribute name
	 * and the (start of the) value
	 */
	public void setAttrStartTable(int page, String[] attrStartTable) {
		attrStarTables.put(page, attrStartTable);
	}

	/**
	 * Sets the attribute value Table for a given page. The first string in the
	 * array defines attribute value 0x85, the second attribute value 0x86 etc.
	 */
	public void setAttrValueTable(int page, String[] attrStartTable) {
		attrValueTables.put(page, attrStartTable);
	}

	/** sets ContentHandler. See SAX documentation */
	public void setDocumentHandler(ContentHandler dh) {
		this.dh = dh;
	}

	/** registers a handler for Wap XML extensions */

	public void setWbxmlExtensionHandler(WbxmlExtensionHandler eh) {
		this.eh = eh;
	}

	public void parse(InputStream in) throws SAXException, IOException {

		char entityBuf[] = new char[1];

		this.in = in;

		readByte(); // skip version
		publicIdentifierId = readInt();

		if (publicIdentifierId == 0)
			readInt();

		int charset = readInt();
		docCharset = new CharsetMappings().getCharset(charset);
		if(logger.isDebugEnabled()){
			logger.debug("document charset is " + docCharset);
		}
		int strTabSize = readInt();
		stringTable = new char[strTabSize];

		for (int i = 0; i < strTabSize; i++)
			stringTable[i] = (char) readByte();

		// ok, now the real thing....

		dh.startDocument();

		while (true) {
			int id = in.read();
			if (id == -1)
				break;

			switch (id) {
			case Wbxml.SWITCH_PAGE:
				int page = readByte();
				switchPage(page);
				break;

			case Wbxml.END:
				dh.endElement(null, null, stack.lastElement());
				stack.setSize(stack.size() - 1);
				break;

			case Wbxml.ENTITY:
				entityBuf[0] = (char) readInt();
				dh.characters(entityBuf, 0, 1);
				break;

			case Wbxml.STR_I: {
				String s = readStrI();
				dh.characters(s.toCharArray(), 0, s.length());
				break;
			}

			case Wbxml.EXT_I_0:
			case Wbxml.EXT_I_1:
			case Wbxml.EXT_I_2:
			case Wbxml.EXT_T_0:
			case Wbxml.EXT_T_1:
			case Wbxml.EXT_T_2:
			case Wbxml.EXT_0:
			case Wbxml.EXT_1:
			case Wbxml.EXT_2:
			case Wbxml.OPAQUE:
				handleExtensions(id);
				break;

			case Wbxml.PI:
				throw new SAXException("PI curr. not supp.");
				// readPI;
				// break;

			case Wbxml.STR_T: {
				int pos = readInt();
				int end = pos;
				while (stringTable[end] != 0)
					end++;
				dh.characters(stringTable, pos, end - pos);
				break;
			}

			default:
				readElement(id);
			}
		}
		if (stack.size() != 0)
			throw new SAXException("unclosed elements: " + stack);

		dh.endDocument();
	}

	public void switchPage(int page) {
		tagTable = tagsTables.get(page);
		attrStartTable = attrStarTables.get(page);
		attrValueTable = attrValueTables.get(page);
		logger.debug("switching to page 0x" + page);
		if (tagTable == null) {
			logger.debug("tagsTable not found for page " + page);
		}
	}

	// -------------- internal methods start here --------------------

	void handleExtensions(int id) throws SAXException, IOException {

		if (eh == null)
			throw new SAXException("No WapExtensionHandler registered!");

		switch (id) {
		case Wbxml.EXT_I_0:
		case Wbxml.EXT_I_1:
		case Wbxml.EXT_I_2:
			eh.ext_i(id - Wbxml.EXT_I_0, readStrI());
			break;

		case Wbxml.EXT_T_0:
		case Wbxml.EXT_T_1:
		case Wbxml.EXT_T_2:
			eh.ext_t(id - Wbxml.EXT_T_0, readInt());
			break;

		case Wbxml.EXT_0:
		case Wbxml.EXT_1:
		case Wbxml.EXT_2:
			eh.ext(id - Wbxml.EXT_0);
			break;

		case Wbxml.OPAQUE: {
			int len = readInt();
			byte[] buf = new byte[len];
			for (int i = 0; i < len; i++)
				buf[i] = (byte) readByte();

			eh.opaque(buf);
		} // case OPAQUE
		} // SWITCH
	}

	public Attributes readAttr() throws SAXException, IOException {

		AttributesImpl result = new AttributesImpl();

		int id = readByte();

		while (id != 1) {

			String name = resolveId(attrStartTable, id);
			StringBuffer value;

			int cut = name.indexOf('=');

			if (cut == -1)
				value = new StringBuffer();
			else {
				value = new StringBuffer(name.substring(cut + 1));
				name = name.substring(0, cut);
			}

			id = readByte();
			while (id > 128 || id == Wbxml.ENTITY || id == Wbxml.STR_I
					|| id == Wbxml.STR_T
					|| (id >= Wbxml.EXT_I_0 && id <= Wbxml.EXT_I_2)
					|| (id >= Wbxml.EXT_T_0 && id <= Wbxml.EXT_T_2)) {

				switch (id) {
				case Wbxml.ENTITY:
					value.append((char) readInt());
					break;

				case Wbxml.STR_I:
					value.append(readStrI());
					break;

				case Wbxml.EXT_I_0:
				case Wbxml.EXT_I_1:
				case Wbxml.EXT_I_2:
				case Wbxml.EXT_T_0:
				case Wbxml.EXT_T_1:
				case Wbxml.EXT_T_2:
				case Wbxml.EXT_0:
				case Wbxml.EXT_1:
				case Wbxml.EXT_2:
				case Wbxml.OPAQUE:
					handleExtensions(id);
					break;

				case Wbxml.STR_T:
					value.append(readStrT());
					break;

				default:
					value.append(resolveId(attrValueTable, id));
				}

				id = readByte();
			}

			result.addAttribute(null, null, name, null, value.toString());
		}

		return result;
	}

	private String resolveId(String[] tab, int id) throws SAXException,
			IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("resolve(0x" + Integer.toHexString(id & 0x07f) + ")");
		}
		int idx = (id & 0x07f) - 5;
		if (idx == -1) {
			return readStrT();
		}
		if (idx < 0 || tab == null || idx >= tab.length || tab[idx] == null) {
			throw new SAXException("id " + id + " idx "+idx+" undef. tab: " + (tab != null ? Joiner.on(",").join(tab) : "null"));
		}

		String ret = tab[idx];
		if (logger.isDebugEnabled()) {
			logger.debug("resolved as '" + ret + "'");
		}

		return ret;
	}

	void readElement(int id) throws IOException, SAXException {
		String tag = resolveId(tagTable, id & 0x03f);

		// ok, now let's care about attrs etc

		dh.startElement(null, null, tag, ((id & 128) != 0) ? readAttr()
				: new AttributesImpl());

		if ((id & 64) != 0) {
			stack.addElement(tag);
		} else {
			dh.endElement(null, null, tag);
		}
	}

	int readByte() throws IOException, SAXException {
		int i = in.read();
		if (i == -1)
			throw new SAXException("Unexpected EOF");
		return i;
	}

	int readInt() throws SAXException, IOException {
		int result = 0;
		int i;

		do {
			i = readByte();
			result = (result << 7) | (i & 0x7f);
		} while ((i & 0x80) != 0);

		return result;
	}

	String readStrI() throws IOException, SAXException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		while (true) {
			int i = in.read();
			if (i == -1)
				throw new SAXException("Unexpected EOF");
			if (i == 0)
				return new String(out.toByteArray(), docCharset);
			out.write(i);
		}
	}

	String readStrT() throws IOException, SAXException {
		int pos = readInt();
		int end = pos;

		while (stringTable[end] != 0)
			end++;

		return new String(stringTable, pos, end - pos);
	}
}
