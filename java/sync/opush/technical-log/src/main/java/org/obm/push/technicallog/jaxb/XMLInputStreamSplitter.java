/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.technicallog.jaxb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

public class XMLInputStreamSplitter implements Iterable<String> {
	
	private final static char OPENING_TAG_SYMBOL = '<';
	private final static char CLOSING_TAG_SYMBOL = '>';
	private final static char ENDING_TAG_SYMBOL = '/';
	private final static char SPACE_CHARACTER = ' ';
	
	private final BufferedReader bufferedReader;
	private StringBuffer buffer;
	private boolean hasEnteredXML;
	
	public XMLInputStreamSplitter(InputStream inputStream) {
		Preconditions.checkNotNull(inputStream);
		bufferedReader = new BufferedReader(new InputStreamReader(inputStream, Charsets.UTF_8));
	}
	
	public String readNextXML() throws IOException, EndOfStreamException  {
		initialize();
		String rootTagName = readNextRootTagName();
		String endingRootTagName = endingRootTagName(rootTagName);
		return readUntilEndingRootTag(endingRootTagName);
	}

	private void initialize() {
		hasEnteredXML = false;
		buffer = new StringBuffer();
	}
	
	private String readNextRootTagName() throws IOException, EndOfStreamException {
		readUntilOpeningTagSymbol();
		return readTagName();
	}
	
	private String endingRootTagName(String rootTag) {
		return "" + OPENING_TAG_SYMBOL + ENDING_TAG_SYMBOL + rootTag + CLOSING_TAG_SYMBOL;
	}

	private String readUntilEndingRootTag(String endingRootTag)
		throws IOException, EndOfStreamException {
		while (!buffer.toString().contains(endingRootTag)) {
			read();
		}
		return buffer.toString();
	}

	private void readUntilOpeningTagSymbol() throws IOException, EndOfStreamException {
		while (read() != OPENING_TAG_SYMBOL) {}
	}

	private String readTagName() throws IOException, EndOfStreamException {
		char read;
		StringBuffer root = new StringBuffer();
		while ((read = read()) != CLOSING_TAG_SYMBOL) {
			
			if (read == SPACE_CHARACTER || read == ENDING_TAG_SYMBOL) {
				break;
			}
			root.append(read);
		}
		return root.toString();
	}
	
	private char read() throws IOException, EndOfStreamException {
		int value = bufferedReader.read();
		if (value == -1) {
			throw new EndOfStreamException();
		}
		
		char currentChar = (char) value;
		appendToBuffer(currentChar);
		return currentChar;
	}

	private void appendToBuffer(char currentChar) {
		if (!hasEnteredXML) {
			checkOpeningTag(currentChar);
		}
		if (hasEnteredXML) {
			buffer.append(currentChar);
		}
	}

	private void checkOpeningTag(char currentChar) {
		if (currentChar == OPENING_TAG_SYMBOL) {
			hasEnteredXML = true;
		}
	}
	
	@Override
	public Iterator<String> iterator() {
		return new XMLSplitterIterator(this);
	}
}
