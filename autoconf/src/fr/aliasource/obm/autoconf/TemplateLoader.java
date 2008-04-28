package fr.aliasource.obm.autoconf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;

/**
 * Apply ldap attributes on config.xml template
 * 
 * @author tom
 * 
 */
public class TemplateLoader {

	private String configXml;

	public TemplateLoader(DirectoryConfig dc) {
		this.configXml = dc.getConfigXml();
	}

	public void applyTemplate(LDAPAttributeSet attributeSet, String mailHost, OutputStream out)
			throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(configXml));
		generateXMLConfig(reader, out, attributeSet, mailHost);
	}

	@SuppressWarnings("unchecked")
	private void generateXMLConfig(BufferedReader reader, OutputStream out,
			LDAPAttributeSet attributeSet, String mailHost) throws IOException {

		String line = null;
		while ((line = reader.readLine()) != null) {
			Iterator iterator = attributeSet.iterator();
			while (iterator.hasNext()) {
				LDAPAttribute att = (LDAPAttribute) iterator.next();
				line = line.replace("|" + att.getName() + "|", att
						.getStringValue());
			}
			line = line.replace ("|mailHost|", mailHost); 

			out.write(line.getBytes());
		}

		reader.close();
		out.close();
	}
}
