package fr.aliasource.obm.autoconf;

import java.util.Iterator;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;

public class LDAPQueryToolTests extends AutoconfTestCase {

	@SuppressWarnings("unchecked")
	public void testQuery() {
		LDAPQueryTool lq = new LDAPQueryTool(dc);
		LDAPAttributeSet atts = lq.getLDAPInformations();
		assertNotNull(atts);
		Iterator it = atts.iterator();
		while (it.hasNext()) {
			LDAPAttribute att = (LDAPAttribute) it.next();
			System.out.println(att.getName() + ": " + att.getStringValue());
		}
	}

}
