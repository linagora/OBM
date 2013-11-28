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
import java.util.Map;
import java.util.Stack;

import org.obm.push.wbxml.TagsTables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.CharMatcher;

class EncoderHandler extends DefaultHandler {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	private final WbxmlEncoder we;
	private final ByteArrayOutputStream buf;
	private final String defaultNamespace;
	private String currentXmlns;
	private StringBuilder currentCharacter;

	private final Stack<String> stackedStarts;
	private final Stack<Boolean> currentNodeHasSubnodes;
	
	public EncoderHandler(WbxmlEncoder we, ByteArrayOutputStream buf,
			String defaultNamespace) throws IOException {
		this.stackedStarts = new Stack<String>();
		this.currentNodeHasSubnodes = new Stack<Boolean>();
		this.defaultNamespace = defaultNamespace;
		this.we = we;
		this.buf = buf;
		try {
			switchToNs(defaultNamespace);
		} catch (SAXException e) {
		}
		currentXmlns = defaultNamespace;
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attr) throws SAXException {

		subnodeStartElement();
		if (!stackedStarts.isEmpty()) {
			flushNormal();
		}
		dropIgnorableWhitespace();
		try {
			String newNs = null;
			if (!qName.contains(":")) {
				newNs = defaultNamespace;
			} else {
				newNs = qName.substring(0, qName.indexOf(":"));
				qName = qName.substring(qName.indexOf(":") + 1);
			}

			if (!newNs.equals(currentXmlns)) {
				switchToNs(newNs);
			}
			currentXmlns = newNs;

//			we.writeElement(qName);
			queueStart(qName);
		} catch (IOException e) {
			throw new SAXException(e);
		}
	}

	private void subnodeStartElement() {
		if (!currentNodeHasSubnodes.isEmpty()) {
			currentNodeHasSubnodes.pop();
			currentNodeHasSubnodes.add(true);
		}
		currentNodeHasSubnodes.add(false);
	}

	private void dropIgnorableWhitespace() throws SAXException {
		if (currentCharacter != null) {
			if (!containsIgnorableWhitespaces(currentCharacter.toString())) {
				throw new SAXException("unexpected characters between to opening tags");
			}
			currentCharacter = null;
		}
	}

	private boolean containsIgnorableWhitespaces(String input) {
		return CharMatcher.WHITESPACE.matchesAllOf(input);
	}

	private void queueStart(String qName) {
		stackedStarts.add(qName);
	}

	private void flushNormal() throws SAXException {
		String e = stackedStarts.pop();
		try {
			we.writeElement(e);
		} catch (IOException e1) {
			throw new SAXException(e);
		}
	}

	private void flushEmptyElem() throws SAXException {
		String e = stackedStarts.pop();
		try {
			we.writeEmptyElement(e);
		} catch (IOException e1) {
			throw new SAXException(e, e1);
		}
	}

	private void switchToNs(String newNs) throws IOException, SAXException {
		if (!stackedStarts.isEmpty()) {
			flushNormal();
		}
		Map<String, Integer> table = TagsTables.getElementMappings(newNs);
		we.setStringTable(table);
		we.switchPage(TagsTables.NAMESPACES_IDS.get(newNs));
	}
	
	@Override
	public void characters(char[] chars, int start, int len)
			throws SAXException {
		if (!stackedStarts.isEmpty()) {
			flushNormal();
		}
		String s = new String(chars, start, len);
		appendCharacter(s);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		subnodeEndElement();
		if (!stackedStarts.isEmpty()) {
			flushEmptyElem();
		} else {
			flushCharacter();
			buf.write(Wbxml.END);
		}
	}
	
	private void subnodeEndElement() throws SAXException {
		boolean currentElementHasChildren = currentNodeHasSubnodes.pop();
		if (currentElementHasChildren) {
			dropIgnorableWhitespace();
		}
	}

	private void appendCharacter(String characters){
		
		if(this.currentCharacter == null){
			this.currentCharacter = new StringBuilder();
		}
		currentCharacter.append(characters);
	}
	
	private void flushCharacter(){
		if(this.currentCharacter != null){
			try {
				we.writeStrI(buf, currentCharacter.toString());
				currentCharacter = null;
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		if (!stackedStarts.isEmpty()) {
			flushNormal();
		}
	}

}