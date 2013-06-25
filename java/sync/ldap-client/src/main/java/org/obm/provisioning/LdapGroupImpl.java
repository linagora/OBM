package org.obm.provisioning;

import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;

import com.google.inject.Inject;

public class LdapGroupImpl implements LdapGroup {
	
	public class IdImpl implements LdapGroup.Id {

		private final String id;
		
		public IdImpl(String id) {
			this.id = id;
		}
		
		@Override
		public String get() {
			return id;
		}
	}
	
	public static class Builder {
		
		private String[] objectClasses;
		private String cn;
		private int gidNumber;
		private String mailAccess;
		private String mail;
		private String obmDomain;

		private final Configuration configuration;

		@Inject
		private Builder(Configuration configuration) {
			this.configuration = configuration;
		}
		
		public Builder objectClasses(String[] objectClasses) {
			this.objectClasses = objectClasses;
			return this;
		}

		public Builder cn(String cn) {
			this.cn = cn;
			return this;
		}

		public Builder gidNumber(int gidNumber) {
			this.gidNumber = gidNumber;
			return this;
		}

		public Builder mailAccess(String mailAccess) {
			this.mailAccess = mailAccess;
			return this;
		}

		public Builder mail(String mail) {
			this.mail = mail;
			return this;
		}

		public Builder obmDomain(String obmDomain) {
			this.obmDomain = obmDomain;
			return this;
		}
		
		public LdapGroupImpl build() {
			return new LdapGroupImpl(configuration.getGroupBaseDn(), objectClasses, cn, gidNumber, mailAccess, mail, obmDomain);
		}
	}
	
	private final Dn groupBaseDn;
	private final String[] objectClasses;
	private final String cn;
	private final int gidNumber;
	private final String mailAccess;
	private final String mail;
	private final String obmDomain;
	
	private LdapGroupImpl(Dn groupBaseDn, String[] objectClasses, String cn, int gidNumber,
			String mailAccess, String mail, String obmDomain) {
		this.groupBaseDn = groupBaseDn;
		this.objectClasses = objectClasses;
		this.cn = cn;
		this.gidNumber = gidNumber;
		this.mailAccess = mailAccess;
		this.mail = mail;
		this.obmDomain = obmDomain;
	}

	@Override
	public String[] getObjectClasses() {
		return objectClasses;
	}

	@Override
	public String getCn() {
		return cn;
	}

	@Override
	public int getGidNumber() {
		return gidNumber;
	}

	@Override
	public String getMailAccess() {
		return mailAccess;
	}

	@Override
	public String getMail() {
		return mail;
	}

	@Override
	public String getObmDomain() {
		return obmDomain;
	}
	
	@Override
	public Entry buildEntry() throws LdapException {
		String dn = buildDn();
		
		List<String> attributes = new ArrayList<String>();
		for (String objectClass: getObjectClasses()) {
			attributes.add("objectClass: " + objectClass);
		}
		attributes.add("cn: " + getCn());
		attributes.add("gidNumber: " + getGidNumber());
		attributes.add("mailAccess: " + getMailAccess());
		attributes.add("mail: " + getMail());
		attributes.add("obmDomain: " + getObmDomain());
		
		return new DefaultEntry(dn, attributes.toArray(new Object[0]));
	}
	
	protected String buildDn() {
		return "cn=" + getCn() + "," + groupBaseDn.getName();
	}

	public LdapGroup.Id getId() {
		return new IdImpl(getCn());
	}
}
