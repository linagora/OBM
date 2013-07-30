/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013 Linagora
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
package org.obm.provisioning.ldap.client.bean;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.obm.provisioning.ldap.client.Configuration;

import com.google.common.base.Objects;
import com.google.inject.Inject;

public class LdapGroup {
	
	public static class Cn {

		private final String cn;
	
		public static Cn valueOf(String cn) {
			return new Cn(cn);
		}

		private Cn(String cn) {
			this.cn = cn;
		}
		
		public String get() {
			return cn;
		}

		@Override
		public final boolean equals(Object object){
			if (!(object instanceof Cn))
				return false;
			
			return Objects.equal(cn, ((Cn)object).cn);
		}

		@Override
		public final int hashCode(){
			return Objects.hashCode(cn);
		}
	}
	
	public static class Builder {
		
		private String[] objectClasses;
		private Cn cn;
		private int gidNumber;
		private String mailAccess;
		private String mail;
		private LdapDomain domain;

		private final Configuration configuration;

		@Inject
		private Builder(Configuration configuration) {
			this.configuration = configuration;
		}
		
		public Builder objectClasses(String[] objectClasses) {
			this.objectClasses = objectClasses;
			return this;
		}

		public Builder cn(Cn cn) {
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

		public Builder domain(LdapDomain domain) {
			this.domain = domain;
			return this;
		}
		
		public LdapGroup build() {
			return new LdapGroup(configuration.getGroupBaseDn(domain), objectClasses, cn, gidNumber, mailAccess, mail, domain);
		}
	}
	
	private final Dn groupBaseDn;
	private final String[] objectClasses;
	private final Cn cn;
	private final int gidNumber;
	private final String mailAccess;
	private final String mail;
	private final LdapDomain domain;
	
	private LdapGroup(Dn groupBaseDn, String[] objectClasses, Cn cn, int gidNumber,
			String mailAccess, String mail, LdapDomain domain) {
		this.groupBaseDn = groupBaseDn;
		this.objectClasses = objectClasses;
		this.cn = cn;
		this.gidNumber = gidNumber;
		this.mailAccess = mailAccess;
		this.mail = mail;
		this.domain = domain;
	}

	public String[] getObjectClasses() {
		return objectClasses;
	}

	public Cn getCn() {
		return cn;
	}

	public int getGidNumber() {
		return gidNumber;
	}

	public String getMailAccess() {
		return mailAccess;
	}

	public String getMail() {
		return mail;
	}

	public LdapDomain getDomain() {
		return domain;
	}
	
	public Entry buildEntry() throws LdapException {
		LdapEntry.Builder builder = LdapEntry.builder().dn(
				buildDn());
		
		for (String objectClass: getObjectClasses()) {
			builder.attribute(Attribute.valueOf("objectClass", objectClass));
		}
		LdapEntry ldapEntry = builder
				.attribute(Attribute.valueOf("cn", cn.get()))
				.attribute(Attribute.valueOf("gidNumber", gidNumber))
				.attribute(Attribute.valueOf("mailAccess", mailAccess))
				.attribute(Attribute.valueOf("mail", mail))
				.attribute(Attribute.valueOf("obmDomain", domain.get()))
				.build();
				
		return ldapEntry.toDefaultEntry();
	}
	
	private org.obm.provisioning.ldap.client.bean.Dn buildDn() {
		return org.obm.provisioning.ldap.client.bean.Dn.valueOf(
				"cn=" + getCn().get() + "," + groupBaseDn.getName());
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(cn, gidNumber, mailAccess, mail, domain);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof LdapGroup) {
			LdapGroup that = (LdapGroup) object;
			return Objects.equal(this.cn, that.cn)
				&& Objects.equal(this.gidNumber, that.gidNumber)
				&& Objects.equal(this.mailAccess, that.mailAccess)
				&& Objects.equal(this.mail, that.mail)
				&& Objects.equal(this.domain, that.domain);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("cn", cn)
			.add("gidNumber", gidNumber)
			.add("mailAccess", mailAccess)
			.add("mail", mail)
			.add("obmDomain", domain)
			.toString();
	}
}
