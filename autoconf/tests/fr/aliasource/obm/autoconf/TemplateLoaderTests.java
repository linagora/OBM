package fr.aliasource.obm.autoconf;

import org.w3c.dom.Document;

import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPException;

import fr.aliasource.obm.autoconf.impl.AutoconfTestCase;
import fr.aliasource.obm.utils.ConstantService;
import fr.aliasource.obm.utils.DOMUtils;

public class TemplateLoaderTests extends AutoconfTestCase {

	public void testApplyTemplates() throws LDAPException {
		TemplateLoader tl = new TemplateLoader(dc.getConfigXml(),
				ConstantService.getInstance());

		LDAPQueryTool lqt = new LDAPQueryTool(dc);
		LDAPAttributeSet las = lqt.getLDAPInformations();
		assertNotNull(las);

		try {
			Document doc = tl.applyTemplate(las, "imapMailhost",
					"smtpMailHost", "ldapHost", "att", "attvalue");
			assertNotNull(doc);
			DOMUtils.logDom(doc);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error should not occur");
		}

	}

}
