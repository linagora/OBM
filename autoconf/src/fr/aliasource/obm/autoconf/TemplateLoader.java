package fr.aliasource.obm.autoconf;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;

import fr.aliasource.obm.utils.ConstantService;
import fr.aliasource.obm.utils.DOMUtils;

/**
 * Apply ldap attributes on config.xml template
 * 
 * @author tom
 * 
 */
public class TemplateLoader {

	private String configXml;
	private ConstantService constants;
	private static final Log logger = LogFactory.getLog(TemplateLoader.class);

	public TemplateLoader(String configXml, ConstantService constants) {
		this.configXml = configXml;
		this.constants = constants;
	}

	public Document applyTemplate(LDAPAttributeSet attributeSet,
			String imapMailHost, String smtpMailHost, String ldapHost,
			String allowedAtt, String allowedValue) throws IOException {
		try {
			Document doc = DOMUtils.parse(new FileInputStream(configXml));
			generateXMLConfig(doc, attributeSet, imapMailHost, smtpMailHost,
					ldapHost, allowedAtt, allowedValue);
			return doc;
		} catch (Exception e) {
			logger.error("Invalid config.xml document", e);
			throw new IOException("Invalid config.xml document");
		}
	}

	private void generateXMLConfig(Document doc, LDAPAttributeSet attributeSet,
			String imapMailHost, String smtpMailHost, String ldapHost,
			String allowedAtt, String allowedValue) throws IOException {

		Element root = doc.getDocumentElement();
		replaceInNode(attributeSet, imapMailHost, smtpMailHost, ldapHost,
				allowedAtt, allowedValue, root);
	}

	private void replaceInNode(LDAPAttributeSet attributeSet,
			String imapMailHost, String smtpMailHost, String ldapHost,
			String allowedAtt, String allowedValue, Element root) {
		NodeList nl = root.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;
				NamedNodeMap atts = e.getAttributes();
				for (int j = 0; j < atts.getLength(); j++) {
					Attr at = (Attr) atts.item(j);
					String oldVal = at.getValue();
					String val = doAttributeExpansion(attributeSet,
							imapMailHost, smtpMailHost, ldapHost, allowedAtt,
							allowedValue, oldVal);
					if (!oldVal.equals(val)) {
						e.setAttribute(at.getName(), val);
					}
				}
				replaceInNode(attributeSet, imapMailHost, smtpMailHost, ldapHost, allowedAtt, allowedValue, e);
			}
		}
	}

	private String doAttributeExpansion(LDAPAttributeSet attributeSet,
			String imapMailHost, String smtpMailHost, String ldapHost,
			String allowedAtt, String allowedValue, String line) {
		Iterator iterator = attributeSet.iterator();
		while (iterator.hasNext()) {
			LDAPAttribute att = (LDAPAttribute) iterator.next();
			if (att.getName().equalsIgnoreCase(allowedAtt)) {
				List<String> values = Arrays.asList(att.getStringValueArray());
				if (values.contains(allowedValue)) {
					line = line.replace("|" + att.getName() + "|", "true");
				} else {
					line = line.replace("|" + att.getName() + "|", "false");
				}
			} else {
				line = line.replace("|" + att.getName() + "|", att
						.getStringValue());
			}
		}
		if (imapMailHost != null) {
			line = line.replace("|imapMailHost|", imapMailHost);
		}
		if (smtpMailHost != null) {
			line = line.replace("|smtpMailHost|", smtpMailHost);
		}
		if (ldapHost != null) {
			line = line.replace("|ldapHost|", ldapHost);
		}

		for (Object key : constants.getKeySet()) {
			String k = (String) key;
			line = line.replace("|" + k + "|", constants.getStringValue(k));
		}
		return line;
	}
}
