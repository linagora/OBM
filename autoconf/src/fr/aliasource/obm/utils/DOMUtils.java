package fr.aliasource.obm.utils;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Méthodes utilitaires pour extraire des infos d'un DOM.
 * 
 * @author tom
 *
 */
public class DOMUtils {

	public static String getElementText(Element root, String elementName) {
		NodeList list = root.getElementsByTagName(elementName);
		if (list.getLength() == 0) {
			return null;
		}
		Text txtElem = (Text) list.item(0).getFirstChild();
		return txtElem.getData();
	}

	public static String getElementText(Element root) {
		Text txtElem = (Text) root.getFirstChild();
		return txtElem.getData();
	}

	public static String[] getTexts(Element root, String elementName) {
		NodeList list = root.getElementsByTagName(elementName);
		String[] ret = new String[list.getLength()];
		for (int i = 0; i < list.getLength(); i++) {
			ret[i] = ((Text) list.item(i).getFirstChild()).getData();
		}
		return ret;
	}

	/**
	 * Renvoie sous la forme d'un tableau la valeur des attributs
	 * donnés pour toutes les occurences d'un élément donnée dans le dom
	 * 
	 * <code>
	 *  <toto>
	 *   <titi id="a" val="ba"/>
	 *   <titi id="b" val="bb"/>
	 *  </toto>
	 * </code>
	 * 
	 * et getAttributes(&lt;toto&gt;, "titi", { "id", "val" })
	 * renvoie
	 * 
	 * { { "a", "ba" } { "b", "bb" } }
	 *  
	 * @param root
	 * @param elementName
	 * @param wantedAttributes
	 * @return
	 */
	public static String[][] getAttributes(
		Element root,
		String elementName,
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
	 * Renvoie la valeur de l'attribut donné, d'un élément donné qui
	 * doit être unique sous l'élément racine
	 * 
	 * @param root
	 * @param elementName
	 * @param attribute
	 * @return
	 */
	public static String getElementAttribute(
		Element root,
		String elementName,
		String attribute) {
		NodeList list = root.getElementsByTagName(elementName);
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

	public static Element findElementWithUniqueAttribute(
		Element root,
		String elementName,
		String attribute,
		String attributeValue) {
		NodeList list = root.getElementsByTagName(elementName);
		for (int i=0; i < list.getLength();i++) {
			Element tmp = (Element) list.item(i);
			if (tmp.getAttribute(attribute).equals(attributeValue)) {
				return tmp;
			}
		}
		return null;
	}

	public static Element createElementAndText(
		Element parent,
		String elementName,
		String text) {
		if (text == null) {
			throw new NullPointerException(
				"element '" + elementName + "' with null text.");
		}
		Element el = parent.getOwnerDocument().createElement(elementName);
		parent.appendChild(el);
		Text txt = el.getOwnerDocument().createTextNode(text);
		el.appendChild(txt);
		return el;
	}
	public static Element createElement(Element parent, String elementName) {
		Element el = parent.getOwnerDocument().createElement(elementName);
		parent.appendChild(el);
		return el;
	}

	public static void serialise(Document doc, OutputStream out)
		throws TransformerException {
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer tf = factory.newTransformer();
		tf.setOutputProperty(OutputKeys.INDENT, "yes");
		Source input = new DOMSource(doc.getDocumentElement());
		Result output = new StreamResult(out);
		tf.transform(input, output);
	}

	public static void logDom(Object logSource, Document doc)
		throws TransformerException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		serialise(doc, out);
		Log logger = LogFactory.getLog(logSource.getClass());
		logger.debug(new String(out.toByteArray()));
	}

	public static Document parse(InputStream is)
		throws
			SAXException,
			IOException,
			ParserConfigurationException,
			FactoryConfigurationError {
		DocumentBuilder builder =
			DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return builder.parse(is);
	}

	public static Document createDoc()
		throws ParserConfigurationException, FactoryConfigurationError {
		DocumentBuilder builder =
			DocumentBuilderFactory.newInstance().newDocumentBuilder();
		return builder.newDocument();
	}
}
