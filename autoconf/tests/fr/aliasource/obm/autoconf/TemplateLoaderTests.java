package fr.aliasource.obm.autoconf;

import java.io.ByteArrayOutputStream;

import com.novell.ldap.LDAPAttributeSet;

import fr.aliasource.obm.autoconf.impl.AutoconfTestCase;

public class TemplateLoaderTests extends AutoconfTestCase {

	public void testApplyTemplates() {
		TemplateLoader tl = new TemplateLoader(dc);

		LDAPQueryTool lqt = new LDAPQueryTool(dc);
		LDAPAttributeSet las = lqt.getLDAPInformations();
		assertNotNull(las);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			tl.applyTemplate(las, "imapMailhost", "smtpMailHost", "ldapHost",  out);
			String transformed = out.toString();
			assertNotNull(transformed);
			System.out.println("transformed:\n" + transformed);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error should not occur");
		}

	}

}
