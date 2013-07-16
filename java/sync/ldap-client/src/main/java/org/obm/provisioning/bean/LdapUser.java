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
package org.obm.provisioning.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.directory.api.ldap.model.entry.DefaultEntry;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.obm.provisioning.Configuration;

import com.google.common.base.Objects;
import com.google.inject.Inject;

public class LdapUser {
	
	public class Id {

		private final String id;
		
		public Id(String id) {
			this.id = id;
		}
		
		public String get() {
			return id;
		}
	}
	
	public static class Builder {
		
		private String[] objectClasses;
		private String uid;
		private int uidNumber;
		private int gidNumber;
		private String loginShell;
		private String cn;
		private String displayName;
		private String sn;
		private String givenName;
		private String homeDirectory;
		private String userPassword;
		private String webAccess;
		private String mailBox;
		private String mailBoxServer;
		private String mailAccess;
		private String mail;
		private boolean hiddenUser;
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
		
		public Builder uid(String uid) {
			this.uid = uid;
			return this;
		}
		
		public Builder uidNumber(int uidNumber) {
			this.uidNumber = uidNumber;
			return this;
		}
		
		public Builder gidNumber(int gidNumber) {
			this.gidNumber = gidNumber;
			return this;
		}
		
		public Builder loginShell(String loginShell) {
			this.loginShell = loginShell;
			return this;
		}
		
		public Builder cn(String cn) {
			this.cn = cn;
			return this;
		}
		
		public Builder displayName(String displayName) {
			this.displayName = displayName;
			return this;
		}
		
		public Builder sn(String sn) {
			this.sn = sn;
			return this;
		}
		
		public Builder givenName(String givenName) {
			this.givenName = givenName;
			return this;
		}
		
		public Builder homeDirectory(String homeDirectory) {
			this.homeDirectory = homeDirectory;
			return this;
		}
		
		public Builder userPassword(String userPassword) {
			this.userPassword = userPassword;
			return this;
		}
		
		public Builder webAccess(String webAccess) {
			this.webAccess = webAccess;
			return this;
		}
		
		public Builder mailBox(String mailBox) {
			this.mailBox = mailBox;
			return this;
		}
		
		public Builder mailBoxServer(String mailBoxServer) {
			this.mailBoxServer = mailBoxServer;
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
		
		public Builder hiddenUser(boolean hiddenUser) {
			this.hiddenUser = hiddenUser;
			return this;
		}
		
		public Builder obmDomain(String obmDomain) {
			this.obmDomain = obmDomain;
			return this;
		}
		
		public LdapUser build() {
			return new LdapUser(configuration.getUserBaseDn(), objectClasses, uid, uidNumber, gidNumber, loginShell,
					cn, displayName, sn, givenName, homeDirectory, userPassword, webAccess,
					mailBox, mailBoxServer, mailAccess, mail, hiddenUser, obmDomain);
		}
	}
	
	private final Dn userBaseDn;
	private final String[] objectClasses;
	private final String uid;
	private final int uidNumber;
	private final int gidNumber;
	private final String loginShell;
	private final String cn;
	private final String displayName;
	private final String sn;
	private final String givenName;
	private final String homeDirectory;
	private final String userPassword;
	private final String webAccess;
	private final String mailBox;
	private final String mailBoxServer;
	private final String mailAccess;
	private final String mail;
	private final boolean hiddenUser;
	private final String obmDomain;
	
	private LdapUser(Dn userBaseDn, String[] objectClasses, String uid, int uidNumber, int gidNumber, String loginShell,
			String cn, String displayName, String sn, String givenName, String homeDirectory, String userPassword, String webAccess,
			String mailBox, String mailBoxServer, String mailAccess, String mail, boolean hiddenUser, String obmDomain) {
		this.userBaseDn = userBaseDn;
		this.objectClasses = objectClasses;
		this.uid = uid;
		this.uidNumber = uidNumber;
		this.gidNumber = gidNumber;
		this.loginShell = loginShell;
		this.cn = cn;
		this.displayName = displayName;
		this.sn = sn;
		this.givenName = givenName;
		this.homeDirectory = homeDirectory;
		this.userPassword = userPassword;
		this.webAccess = webAccess;
		this.mailBox = mailBox;
		this.mailBoxServer = mailBoxServer;
		this.mailAccess = mailAccess;
		this.mail = mail;
		this.hiddenUser = hiddenUser;
		this.obmDomain = obmDomain;
	}

	public String[] getObjectClasses() {
		return objectClasses;
	}

	public String getUid() {
		return uid;
	}

	public int getUidNumber() {
		return uidNumber;
	}

	public int getGidNumber() {
		return gidNumber;
	}

	public String getLoginShell() {
		return loginShell;
	}

	public String getCn() {
		return cn;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getSn() {
		return sn;
	}

	public String getGivenName() {
		return givenName;
	}

	public String getHomeDirectory() {
		return homeDirectory;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public String getWebAccess() {
		return webAccess;
	}

	public String getMailBox() {
		return mailBox;
	}

	public String getMailBoxServer() {
		return mailBoxServer;
	}

	public String getMailAccess() {
		return mailAccess;
	}

	public String getMail() {
		return mail;
	}

	public boolean isHiddenUser() {
		return hiddenUser;
	}

	public String getObmDomain() {
		return obmDomain;
	}

	public Entry buildEntry() throws LdapException {
		String dn = buildDn();
		
		List<String> attributes = new ArrayList<String>();
		for (String objectClass: getObjectClasses()) {
			attributes.add("objectClass: " + objectClass);
		}
		attributes.add("uid: " + getUid());
		attributes.add("uidNumber: " + getUidNumber());
		attributes.add("gidNumber: " + getGidNumber());
		attributes.add("loginShell: " + getLoginShell());
		attributes.add("cn: " + getCn());
		attributes.add("displayName: " + getDisplayName());
		attributes.add("sn: " + getSn());
		attributes.add("givenName: " + getGivenName());
		attributes.add("homeDirectory: " + getHomeDirectory());
		attributes.add("userPassword: " + getUserPassword());
		attributes.add("webAccess: " + getWebAccess());
		attributes.add("mailBox: " + getMailBox());
		attributes.add("mailBoxServer: " + getMailBoxServer());
		attributes.add("mailAccess: " + getMailAccess());
		attributes.add("mail: " + getMail());
		attributes.add("hiddenUser: " + isHiddenUser());
		attributes.add("obmDomain: " + getObmDomain());
		
		return new DefaultEntry(dn, attributes.toArray(new Object[0]));
	}
	
	private String buildDn() {
		return "uid=" + getUid() + "," + userBaseDn.getName();
	}
	
	public Id getId() {
		return new Id(getUid());
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(uid, uidNumber, gidNumber, loginShell, cn, displayName, sn, givenName, 
				homeDirectory, userPassword, webAccess, mailBox, mailBoxServer, mailAccess, mail, hiddenUser, obmDomain);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof LdapUser) {
			LdapUser that = (LdapUser) object;
			return Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.uidNumber, that.uidNumber)
				&& Objects.equal(this.gidNumber, that.gidNumber)
				&& Objects.equal(this.loginShell, that.loginShell)
				&& Objects.equal(this.cn, that.cn)
				&& Objects.equal(this.displayName, that.displayName)
				&& Objects.equal(this.sn, that.sn)
				&& Objects.equal(this.givenName, that.givenName)
				&& Objects.equal(this.homeDirectory, that.homeDirectory)
				&& Objects.equal(this.userPassword, that.userPassword)
				&& Objects.equal(this.webAccess, that.webAccess)
				&& Objects.equal(this.mailBox, that.mailBox)
				&& Objects.equal(this.mailBoxServer, that.mailBoxServer)
				&& Objects.equal(this.mailAccess, that.mailAccess)
				&& Objects.equal(this.mail, that.mail)
				&& Objects.equal(this.hiddenUser, that.hiddenUser)
				&& Objects.equal(this.obmDomain, that.obmDomain);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("uid", uid)
			.add("uidNumber", uidNumber)
			.add("gidNumber", gidNumber)
			.add("loginShell", loginShell)
			.add("cn", cn)
			.add("displayName", displayName)
			.add("sn", sn)
			.add("givenName", givenName)
			.add("homeDirectory", homeDirectory)
			.add("userPassword", userPassword)
			.add("webAccess", webAccess)
			.add("mailBox", mailBox)
			.add("mailBoxServer", mailBoxServer)
			.add("mailAccess", mailAccess)
			.add("mail", mail)
			.add("hiddenUser", hiddenUser)
			.add("obmDomain", obmDomain)
			.toString();
	}
}
