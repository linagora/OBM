/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
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
