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
import java.io.OutputStream;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.base.Charsets;

/**
 * Converts XML to WBXML using a sax content handler
 */
public class WbxmlEncoder {

	private static final Logger logger = LoggerFactory
			.getLogger(WbxmlEncoder.class);
	private SAXParser parser;
	private Map<String, Integer> stringTable;
	private ByteArrayOutputStream buf;
	private final String defaultNamespace;

	/**
	 * The constructor creates an internal document handler. The given parser is
	 * used
	 */
	public WbxmlEncoder(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			parser = factory.newSAXParser();
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	void setStringTable(Map<String, Integer> table) {
		this.stringTable = table;
	}

	/**
	 * converts the XML data from the given SAX InputSource and writes the
	 * result to the given OutputStream
	 */
	public void convert(InputSource in, OutputStream out) throws SAXException,
			IOException {

		buf = new ByteArrayOutputStream();

		// perform conv.
		parser.parse(in, new EncoderHandler(this, buf, defaultNamespace));

		// ok, write header

		out.write(0x03); // version
		out.write(0x01); // unknown or missing public identifier
		out.write(0x6a); // UTF-8
		out.write(0x00); // no string table

		// write buf

		buf.writeTo(out);

		// ready!

		out.flush();
	}

	// internal methods

	void writeInt(OutputStream out, int i) throws IOException {
		byte[] buf = new byte[5];
		int idx = 0;

		do {
			buf[idx++] = (byte) (i & 0x7f);
			i = i >> 7;
		} while (i != 0);

		while (idx > 1) {
			out.write(buf[--idx] | 0x80);
		}
		out.write(buf[0]);
	}

	void writeStrI(OutputStream out, String s) throws IOException {
		out.write(Wbxml.STR_I);
		out.write(s.getBytes(Charsets.UTF_8));
		out.write(0);
	}

	public void switchPage(Integer integer) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("switching to page 0x" + Integer.toHexString(integer));
		}
		writeInt(buf, 0x00);
		writeInt(buf, integer);
	}

	public void writeElement(String name) throws IOException {
		Integer mapping = stringTable.get(name);
		if (mapping == null) {
			logger.warn("no mapping for '" + name + "'");
			throw new IOException("no mapping for '" + name + "'");
		}
		writeInt(buf, stringTable.get(name) + 64);
	}

	public void writeEmptyElement(String name) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("write empty tag " + name);
		}
		Integer mapping = stringTable.get(name);
		if (mapping == null) {
			logger.warn("no mapping for '" + name + "'");
			throw new IOException("no mapping for '" + name + "'");
		}
		writeInt(buf, stringTable.get(name));
	}
}
