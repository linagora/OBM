package fr.aliasource.obm.autoconf;

import java.util.HashMap;

import org.w3c.dom.Document;

import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPException;

import fr.aliasource.obm.autoconf.impl.AutoconfTestCase;
import fr.aliasource.obm.utils.ConstantService;

public class TemplateLoaderTests extends AutoconfTestCase {

	public void testApplyTemplates() throws LDAPException {
		TemplateLoader tl = new TemplateLoader(dc.getConfigXml(),
				ConstantService.getInstance());

		LDAPQueryTool lqt = new LDAPQueryTool(dc);
		LDAPAttributeSet las = lqt.getLDAPInformations();
		assertNotNull(las);

		try {
			Document doc = tl.applyTemplate(las, new HashMap<String, String>());
			assertNotNull(doc);
//			DOMUtils.logDom(doc);
			System.out.println("valid template: "+tl.isValidTemplate(doc.getDocumentElement()));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Error should not occur");
		}
	}

}
