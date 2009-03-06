package fr.aliasource.obm.autoconf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPSearchResults;

public class LDAPQueryTool {

	private DirectoryConfig dc;
	private Log logger;

	public LDAPQueryTool(DirectoryConfig dc) {
		logger = LogFactory.getLog(getClass());
		this.dc = dc;
	}

	LDAPAttributeSet getLDAPInformations() throws LDAPException {
		LDAPConnection ld = new LDAPConnection();
		LDAPSearchResults searchResults;
		LDAPAttributeSet attributeSet;
		try {
			ld.connect(dc.getLdapHost(), dc.getLdapPort());
			searchResults = ld.search(dc.getLdapSearchBase(),
					LDAPConnection.SCOPE_SUB, dc.getLdapFilter(), dc
							.getLdapAtts(), false);
			LDAPEntry nextEntry = searchResults.next();
			attributeSet = nextEntry.getAttributeSet();
			return attributeSet;
		} catch (LDAPException e) {
			logger.error("Error finding user info", e);
			throw e;
		} finally {
			try {
				ld.disconnect();
			} catch (LDAPException e) {
			}
		}
	}

}
