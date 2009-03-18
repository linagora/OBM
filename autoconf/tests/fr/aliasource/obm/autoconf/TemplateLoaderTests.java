package fr.aliasource.obm.autoconf;

import java.io.ByteArrayOutputStream;

import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPException;

import fr.aliasource.obm.autoconf.impl.AutoconfTestCase;
import fr.aliasource.obm.utils.ConstantService;

public class TemplateLoaderTests extends AutoconfTestCase {

	public void testApplyTemplates() throws LDAPException {
		TemplateLoader tl = new TemplateLoader(dc.getConfigXml(), ConstantService.getInstance());

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
