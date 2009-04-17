package fr.aliasource.obm.autoconf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;

import fr.aliasource.obm.utils.ConstantService;

/**
 * Apply ldap attributes on config.xml template
 * 
 * @author tom
 * 
 */
public class TemplateLoader {

	private String configXml;
	private ConstantService constants;

	public TemplateLoader(String configXml, ConstantService constants) {
		this.configXml = configXml;
		this.constants = constants;
	}

	public void applyTemplate(LDAPAttributeSet attributeSet,
			String imapMailHost, String smtpMailHost, String ldapHost,
			OutputStream out, String allowedAtt, String allowedValue) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(configXml));
		generateXMLConfig(reader, out, attributeSet, imapMailHost,
				smtpMailHost, ldapHost, allowedAtt, allowedValue);
	}
	
	@SuppressWarnings("unchecked")
	private void generateXMLConfig(BufferedReader reader, OutputStream out,
			LDAPAttributeSet attributeSet, String imapMailHost,
			String smtpMailHost, String ldapHost, String allowedAtt, String allowedValue) throws IOException {
		String line = null;
		while ((line = reader.readLine()) != null) {
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
					line = line.replace("|" + att.getName() + "|", att.getStringValue());
				}
			}
			line = line.replace("|imapMailHost|", imapMailHost);
			line = line.replace("|smtpMailHost|", smtpMailHost);
			line = line.replace("|ldapHost|", ldapHost);

			for (Object key : constants.getKeySet()) {
				String k = (String) key;
				line = line.replace("|" + k + "|", constants.getStringValue(k));
			}

			out.write(line.getBytes());
		}

		reader.close();
		out.close();
	}
}
