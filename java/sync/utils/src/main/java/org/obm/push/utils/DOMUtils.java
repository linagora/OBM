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
package org.obm.push.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.xalan.processor.TransformerFactoryImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.cyberneko.html.parsers.DOMParser;
import org.obm.push.utils.xml.XmlCharacterFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;


public final class DOMUtils {

	private static final Logger logger = LoggerFactory
	.getLogger(DOMUtils.class);
	
	private static TransformerFactory fac;
	private static DocumentBuilderFactory dbf;
	private static ThreadLocal<DocumentBuilder> builder = new ThreadLocal<DocumentBuilder>();

	static {
		Class<TransformerFactoryImpl> factoryClass = TransformerFactoryImpl.class;
		fac = TransformerFactory.newInstance(factoryClass.getName(), factoryClass.getClassLoader());
		dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setValidating(false);
	}

	private static DocumentBuilder builder() {
		DocumentBuilder documentBuilder = builder.get();
		if (documentBuilder == null) {
			try {
				documentBuilder = dbf.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				logger.error(e.getMessage(), e);
			}
			builder.set(documentBuilder);
		}
		return documentBuilder;
	}

	public static String getElementTextInChildren(Element root,
			String elementName) {
		NodeList list = root.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeType() == Node.TEXT_NODE) {
				continue;
			}
			Element e = (Element) list.item(i);
			if (e.getTagName().equals(elementName)) {
				return getElementText((Element) list.item(i));
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("No element named '" + elementName + "' under '" //$NON-NLS-1$ //$NON-NLS-2$
					+ root.getNodeName() + "'"); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Renvoie une élément qui doit être unique dans le document.
	 * 
	 * @param root
	 * @param elementName
	 * @return
	 */
	public static Element getUniqueElementInChildren(Element root,
			String elementName) {

		NodeList list = root.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			if (list.item(i).getNodeType() == Node.TEXT_NODE) {
				continue;
			}
			Element e = (Element) list.item(i);
			if (e.getTagName().equals(elementName)) {
				return (Element) list.item(i);
			}
		}
		return null;
	}	

	public static String getElementText(Element root, String elementName) {
		NodeList list = root.getElementsByTagName(elementName);
		if (list.getLength() == 0) {
			return null;
		}
		return getElementText((Element) list.item(0));
	}

	public static String getElementText(Element node) {
		if (isTextElement(node)) {
			Text txtElem = (Text) node.getFirstChild();
			if (txtElem != null) {
				return txtElem.getData();
			}
		}
		return null;
	}

	public static Long getElementLong(Element node) {
		if (isTextElement(node)) {
			Text txtElem = (Text) node.getFirstChild();
			if (txtElem != null) {
				return Long.valueOf(txtElem.getData());
			}
		}
		return null;
	}

	
	public static Integer getElementInteger(Element node) {
		if (isTextElement(node)) {
			Text txtElem = (Text) node.getFirstChild();
			if (txtElem != null) {
				return Integer.valueOf(txtElem.getData());
			}
		}
		return null;
	}
	
	public static Integer getElementInteger(Element node, String elementName) {
		Element integerElement = getUniqueElement(node, elementName);
		return getElementInteger(integerElement);
	}
	
	private static boolean isTextElement(Element node) {
		if (node != null && node.getFirstChild() instanceof Text) {
			return true;
		}
		return false;
	}
	
	public static String[] getTexts(Element root, String elementName) {
		NodeList list = root.getElementsByTagName(elementName);
		String[] ret = new String[list.getLength()];
		for (int i = 0; i < list.getLength(); i++) {
			Text txt = (Text) list.item(i).getFirstChild();
			if (txt != null) {
				ret[i] = txt.getData();
			} else {
				ret[i] = ""; //$NON-NLS-1$
			}
		}
		return ret;
	}

	/**
	 * Renvoie sous la forme d'un tableau la valeur des attributs donnés pour
	 * toutes les occurences d'un élément donnée dans le dom
	 * 
	 * <code>
	 *  <toto>
	 *   <titi id="a" val="ba"/>
	 *   <titi id="b" val="bb"/>
	 *  </toto>
	 * </code>
	 * 
	 * et getAttributes(&lt;toto&gt;, "titi", { "id", "val" }) renvoie { { "a",
	 * "ba" } { "b", "bb" } }
	 * 
	 * @param root
	 * @param elementName
	 * @param wantedAttributes
	 * @return
	 */
	public static String[][] getAttributes(Element root, String elementName,
			String[] wantedAttributes) {
		NodeList list = root.getElementsByTagName(elementName);
		String[][] ret = new String[list.getLength()][wantedAttributes.length];
		for (int i = 0; i < list.getLength(); i++) {
			Element elem = (Element) list.item(i);
			for (int j = 0; j < wantedAttributes.length; j++) {
				ret[i][j] = elem.getAttribute(wantedAttributes[j]);
			}
		}
		return ret;
	}

	/**
	 * Renvoie la valeur de l'attribut donné, d'un élément donné qui doit être
	 * unique sous l'élément racine
	 * 
	 * @param root
	 * @param elementName
	 * @param attribute
	 * @return
	 */
	public static String getElementAttribute(Element root, String elementName,
			String attribute) {
		NodeList list = root.getElementsByTagName(elementName);
		if (list.getLength() == 0) {
			return null;
		}
		return ((Element) list.item(0)).getAttribute(attribute);
	}

	/**
	 * Renvoie une élément qui doit être unique dans le document.
	 * 
	 * @param root
	 * @param elementName
	 * @return
	 */
	public static Element getUniqueElement(Element root, String elementName) {
		NodeList list = root.getElementsByTagName(elementName);
		return (Element) list.item(0);
	}

	public static Iterable<Node> getElementsByName(Element parent, String name) {
		final NodeList elements = parent.getElementsByTagName(name);
		return new Iterable<Node>() {
			
			@Override
			public Iterator<Node> iterator() {
				return new Iterator<Node>() {
					
					int index = 0;
					
					@Override
					public void remove() {
						throw new UnsupportedOperationException("cannot remove element");
					}
					
					@Override
					public Node next() {
						return elements.item(index++);
					}
					
					@Override
					public boolean hasNext() {
						return index < elements.getLength();
					}
				};
			}
		};
	}

	public static Element findElementWithUniqueAttribute(Element root,
			String elementName, String attribute, String attributeValue) {
		NodeList list = root.getElementsByTagName(elementName);
		for (int i = 0; i < list.getLength(); i++) {
			Element tmp = (Element) list.item(i);
			if (tmp.getAttribute(attribute).equals(attributeValue)) {
				return tmp;
			}
		}
		return null;
	}

	public static Element createElementAndText(Element parent,
			String elementName, String text) {
		if (text == null) {
			throw new NullPointerException("null text");
		}
		
		String validXml = XmlCharacterFilter.filter(text);
		Element el = parent.getOwnerDocument().createElement(elementName);
		parent.appendChild(el);
		Text txt = el.getOwnerDocument().createTextNode(validXml);
		el.appendChild(txt);
		return el;
	}
	
	public static Element createElementAndText(Element parent, String elementName, int value) {
		return createElementAndText(parent, elementName, String.valueOf(value));
	}

	public static Element createElement(Element parent, String elementName) {
		Element el = parent.getOwnerDocument().createElement(elementName);
		parent.appendChild(el);
		return el;
	}

	public static String serializeHtmlDocument(final Document replyHtmlDoc)
			throws TransformerConfigurationException,
			TransformerFactoryConfigurationError, TransformerException {
		Transformer transformer = fac.newTransformer();
		StringWriter buffer = new StringWriter();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(replyHtmlDoc), new StreamResult(buffer));
		return buffer.toString();
	}
	
	public static void serialize(Document doc, OutputStream out)
			throws TransformerException {
		serialize(doc, out, false);
	}
	
	public static void serialize(Document doc, OutputStream out, boolean pretty)
			throws TransformerException {
		serialize(doc, out, pretty, XMLVersion.XML_10);
	}
	

	public static void serialize(Element element, OutputStream out, boolean pretty)
			throws TransformerException {
		serialize(element, out, pretty, XMLVersion.XML_10);
	}
	
	public static void serialize(Document doc, OutputStream out, XMLVersion xmlVersion)
			throws TransformerException {
		serialize(doc, out, false, xmlVersion);
	}
	
	public static void serialize(Element element, OutputStream out, XMLVersion xmlVersion)
			throws TransformerException {
		serialize(element, out, false, xmlVersion);
	}
	
	public static String serialize(Document doc)
			throws TransformerException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		serialize(doc, byteArrayOutputStream, false);
		return new String(byteArrayOutputStream.toByteArray());
	}
	
	public static String prettySerialize(Element element)
			throws TransformerException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		serialize(element, byteArrayOutputStream, true);
		return new String(byteArrayOutputStream.toByteArray());
	}
	
	public static String prettySerialize(Document doc)
			throws TransformerException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		serialize(doc, byteArrayOutputStream, true);
		return new String(byteArrayOutputStream.toByteArray());
	}
	
	public static void serialize(Document doc, OutputStream out, boolean pretty, XMLVersion xmlVersion) throws TransformerException {
		serialize(doc.getDocumentElement(), out, pretty, xmlVersion);
	}
	
	public static void serialize(Element element, OutputStream out, boolean pretty, XMLVersion xmlVersion) 
			throws TransformerException {
		Transformer tf = fac.newTransformer();
		if (pretty) {
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
		} else {
			tf.setOutputProperty(OutputKeys.INDENT, "no");
		}
		tf.setOutputProperty(OutputKeys.VERSION, xmlVersion.version());
		Source input = new DOMSource(element);
		Result output = new StreamResult(out);
		tf.transform(input, output);
	}
	
	public static void logDom(Document doc) throws TransformerException {
		if (logger.isDebugEnabled()) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			serialize(doc, out, true);
			logger.debug(out.toString());
		}
	}

	public static Document parse(String xmlContent) throws SAXException, IOException {
		return builder().parse(new InputSource(new StringReader(xmlContent)));
	}
	
	public static Document parse(InputStream inputStream) throws SAXException, IOException {
		return builder().parse(inputStream);
	}

	public static Document parse(File file) throws SAXException,
			IOException, FactoryConfigurationError {
		return builder().parse(file);
	}

	public static DocumentFragment parseHtmlAsFragment(final InputSource source) throws SAXException, IOException {
		final DOMFragmentParser parser = new DOMFragmentParser();
		DocumentFragment frag = new HTMLDocumentImpl().createDocumentFragment();
		parser.parse(source,frag);
		return frag;
	}

	public static Document parseHtmlAsDocument(final InputSource source) throws SAXException, IOException {
		final DOMParser parser = new DOMParser();
		parser.parse(source);
		return parser.getDocument();
	}

	public static Document createDoc(String namespace, String rootElement)
			throws FactoryConfigurationError {
		DOMImplementation di = builder().getDOMImplementation();
		Document document = di.createDocument(namespace, rootElement, null);
		return document;
	}

	public static Document createDocFromElement(Element el) {
		Document doc = createDoc(el.getNamespaceURI(), el.getNodeName());
		Node adoptedNode = doc.importNode(el, true);
		doc.replaceChild(adoptedNode, doc.getFirstChild());
		return doc;
	}

	public static void saxParse(InputStream is, DefaultHandler handler)
			throws SAXException, IOException {
		XMLReader reader = XMLReaderFactory.createXMLReader();
		reader.setContentHandler(handler);
		reader.setErrorHandler(handler);
		reader.parse(new InputSource(is));
	}

	public static Document cloneDOM(Document doc) {
		return (Document) doc.cloneNode(true);
	}
	
	public static void createElementAndText(Element element, String tagName, boolean value) {
		createElementAndText(element, tagName, value ? "1" : "0");
	}
	
	public static void createElementAndTextIfNotNull(Element element, String tagName, Long value) {
		if (value != null) {
			createElementAndText(element, tagName, String.valueOf(value));
		}
	}
	
	public static void createElementAndTextIfNotNull(Element element, String tagName, String value) {
		if (value != null) {
			createElementAndText(element, tagName, value);
		}
	}
	
	public static void createElementAndTextIfNotNull(Element element, String tagName, Integer value) {
		if (value != null) {
			createElementAndText(element, tagName, String.valueOf(value));
		}
	}
	
	public static Element createElementAndText(Element parent, String elementName, InputStream inputStream) throws IOException {
		return createElementAndText(parent, elementName, 
				CharStreams.toString(new InputStreamReader(inputStream)));
	}
	
	public static Element createElementAndCDataSection(Element parent, String elementName, String text) {
		Preconditions.checkNotNull(text, "Text is required");
		
		String validXml = XmlCharacterFilter.filter(text);
		Element el = parent.getOwnerDocument().createElement(elementName);
		parent.appendChild(el);
		el.appendChild(el.getOwnerDocument().createCDATASection(validXml));
		return el;
	}
	
	public static Element createElementAndCDataText(Element parent, String elementName, 
			InputStream inputStream, Charset charset) throws IOException {
		return createElementAndCDataSection(parent, elementName, 
				CharStreams.toString(new InputStreamReader(inputStream, charset)));
	}
	
	public enum XMLVersion {
		XML_10("1.0"),
		XML_11("1.1");
		
		private String version;

		private XMLVersion(String version) {
			this.version = version;
		}

		public String version() {
			return version;
		}

	}

}
