package fr.aliasource.obm.autoconf;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

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
			HashMap<String, String> servicesHostNames) throws IOException {
		try {
			Document doc = DOMUtils.parse(new FileInputStream(configXml));
			generateXMLConfig(doc, attributeSet, servicesHostNames);
			return doc;
		} catch (Exception e) {
			logger.error("Invalid config.xml document", e);
			throw new IOException("Invalid config.xml document");
		}
	}

	private void generateXMLConfig(Document doc, LDAPAttributeSet attributeSet,
			HashMap<String, String> servicesHostNames) throws IOException {

		Element root = doc.getDocumentElement();
		replaceInNode(attributeSet, servicesHostNames, root);
	}

	private void replaceInNode(LDAPAttributeSet attributeSet,
			HashMap<String, String> servicesHostNames, Element root) {
		NodeList nl = root.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;
				NamedNodeMap atts = e.getAttributes();
				for (int j = 0; j < atts.getLength(); j++) {
					Attr at = (Attr) atts.item(j);
					String oldVal = at.getValue();
					String val = doAttributeExpansion(attributeSet, servicesHostNames, oldVal);
					if (!oldVal.equals(val)) {
						e.setAttribute(at.getName(), val);
					}
				}
				replaceInNode(attributeSet, servicesHostNames, e);
			}
		}
	}
	
	/**
	 * @param root
	 * @return
	 */
	public boolean isValidTemplate(Element root) {
		NodeList nl = root.getChildNodes();
		boolean ret = testAttributes(true, root);
		for (int i = 0; i < nl.getLength(); i++) {
			Node n = nl.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				Element e = (Element) n;
				ret = testAttributes(ret, e) && isValidTemplate(e);
				if (!ret) {
					return ret;
				}
			}
		}
		return ret;
	}

	private boolean testAttributes(boolean ret, Element e) {
		NamedNodeMap atts = e.getAttributes();
		for (int j = 0; j < atts.getLength(); j++) {
			Attr at = (Attr) atts.item(j);
			String oldVal = at.getValue();
			ret = ret && !(oldVal.contains("${") && oldVal.contains("}"));
			if (!ret) {
				logger.warn("template validity check failed on attribute "+at.getName()+" with value "+oldVal);
			}
		}
		return ret;
	}

	private void replaceServiceHostName(String line, String serviceParam, String serviceHostName) {
		if (serviceHostName != null && !serviceHostName.equals("")) {
			line = line.replace(serviceParam, serviceHostName);
		}
	}
	
	@SuppressWarnings("unchecked")
	private String doAttributeExpansion(LDAPAttributeSet attributeSet,
			HashMap<String, String> servicesHostNames, String line) {
		if (attributeSet != null) {
			Iterator iterator = attributeSet.iterator();
			while (iterator.hasNext()) {
				LDAPAttribute att = (LDAPAttribute) iterator.next();
				line = line.replace("${" + att.getName() + "}", att
							.getStringValue());
			}
		}
		replaceServiceHostName(line, "${imapMailHost}", servicesHostNames.get("imap_frontend"));
		replaceServiceHostName(line, "${smtpMailHost}",  servicesHostNames.get("smtp_out"));
		replaceServiceHostName(line, "${ldapHost}",  servicesHostNames.get("ldap"));
		replaceServiceHostName(line, "${obmSyncHost}",  servicesHostNames.get("obm_sync"));

		for (Object key : constants.getKeySet()) {
			String k = (String) key;
			line = line.replace("${" + k + "}", constants.getStringValue(k));
		}
		return line;
	}
}
