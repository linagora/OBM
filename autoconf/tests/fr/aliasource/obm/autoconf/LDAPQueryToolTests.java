package fr.aliasource.obm.autoconf;

import java.util.Iterator;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPException;

import fr.aliasource.obm.autoconf.impl.AutoconfTestCase;

public class LDAPQueryToolTests extends AutoconfTestCase {

	@SuppressWarnings("unchecked")
	public void testQuery() throws LDAPException {
		LDAPQueryTool lq = new LDAPQueryTool(dc);
		LDAPAttributeSet atts = lq.getLDAPInformations();
		assertNotNull(atts);
		Iterator<LDAPAttribute> it = (Iterator<LDAPAttribute>) atts.iterator();
		while (it.hasNext()) {
			LDAPAttribute att = (LDAPAttribute) it.next();
			System.out.println(att.getName() + ": " + att.getStringValue());
		}
	}

}
