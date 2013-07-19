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
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import fr.aliacom.obm.common.user.ObmUser;

public class LdapUser {

	private final static String[] DEFAULT_OBJECT_CLASSES = {
		"posixAccount", "shadowAccount", "inetOrgPerson", "obmUser" };

	private final static String DEFAULT_WEB_ACCESS = "REJECT";
	private final static String FORBIDDEN_EMAIL_ACCESS = "REJECT";
	private final static String PERMITTED_EMAIL_ACCESS = "PERMIT";
	private final static int DEFAULT_CYRUS_PORT = 24;
	private final static boolean DEFAULT_HIDDEN_USER = false;
	
	public static class Uid {

		private final String uid;
	
		public static Uid valueOf(String id) {
			return new Uid(id);
		}
		
		private Uid(String uid) {
			this.uid = uid;
		}
		
		public String get() {
			return uid;
		}

		@Override
		public final boolean equals(Object object){
			if (!(object instanceof Uid))
				return false;
			
			return Objects.equal(uid, ((Uid)object).uid);
		}

		@Override
		public final int hashCode(){
			return Objects.hashCode(uid);
		}
	}
	
	public static class Builder {
		
		private String[] objectClasses;
		private Uid uid;
		private Integer uidNumber;
		private Integer gidNumber;
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
		private LdapDomain domain;
		
		private final Configuration configuration;

		@Inject
		private Builder(Configuration configuration) {
			this.configuration = configuration;
		}
	
		public Builder fromObmUser(ObmUser obmUser) {
			Preconditions.checkArgument(obmUser.getUidNumber() != null,
					"The UID number is mandatory");
			Preconditions.checkArgument(obmUser.getGidNumber() != null,
					"The GID number is mandatory");
			Preconditions.checkArgument(obmUser.getDomain().getName() != null,
					"The domain name is mandatory");
			if (obmUser.getMailHost() != null) {
				Preconditions.checkArgument(
						obmUser.getMailHost().getIp() != null,
						"The IP of the mail host is mandatory");
			}

			String displayName = buildDisplayName(obmUser);
			this.objectClasses = DEFAULT_OBJECT_CLASSES;
			this.uid = new Uid(obmUser.getLogin().toLowerCase());
			this.uidNumber = obmUser.getUidNumber();
			this.gidNumber = obmUser.getGidNumber();
			this.cn = displayName;
			this.displayName = displayName;
			this.sn = Strings.isNullOrEmpty(obmUser.getLastName()) ?
					obmUser.getLastName() :
					obmUser.getLogin();
			this.givenName = obmUser.getFirstName();
			this.homeDirectory = buildHomeDirectory(obmUser);
			this.userPassword = obmUser.getPassword();
			this.webAccess = DEFAULT_WEB_ACCESS;
			this.mailBox = String.format("%s@%s",
					obmUser.getLogin().toLowerCase(),
					obmUser.getDomain().getName());
			this.mailBoxServer = buildMailboxServer(obmUser);
			this.mailAccess = buildEmailAccess(obmUser);
			this.hiddenUser = DEFAULT_HIDDEN_USER;
			this.domain = LdapDomain.valueOf(obmUser.getDomain().getName());
			return this;
		}
	
		private String buildDisplayName(ObmUser obmUser) {
			String cn;
			if (!Strings.isNullOrEmpty(obmUser.getFirstName())
					&& !Strings.isNullOrEmpty(obmUser.getLastName())) {
				cn = String.format("%s %s", obmUser.getFirstName(), obmUser.getLastName());
			}
			else if (!Strings.isNullOrEmpty(obmUser.getLastName())) {
				cn = obmUser.getLastName();
			}
			else {
				cn = obmUser.getLogin();
			}
			return cn;
		}
	
		private String buildHomeDirectory(ObmUser obmUser) {
			return String.format("/home/%s", obmUser.getLogin().toLowerCase());
		}

		private String buildMailboxServer(ObmUser obmUser) {
			return obmUser.getEmail() != null && obmUser.getMailHost() != null ?
					String.format("lmtp:%s:%d",
							obmUser.getMailHost().getIp(),
							DEFAULT_CYRUS_PORT)
					: null;
		}

		private String buildEmailAccess(ObmUser obmUser) {
			return obmUser.getEmail() != null ?
					PERMITTED_EMAIL_ACCESS :
					FORBIDDEN_EMAIL_ACCESS;
		}
	
		public Builder objectClasses(String[] objectClasses) {
			this.objectClasses = objectClasses;
			return this;
		}
		
		public Builder uid(Uid uid) {
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
		
		public Builder domain(LdapDomain domain) {
			this.domain = domain;
			return this;
		}
		
		public LdapUser build() {
			Preconditions.checkState(uid != null, "uid should not be null");
			Preconditions.checkState(objectClasses != null && objectClasses.length > 0, "objectClasses should not be empty");
			Preconditions.checkState(uidNumber != null, "uidNumber should not be null");
			Preconditions.checkState(gidNumber != null, "gidNumber should not be null");
			Preconditions.checkState(cn != null, "cn should not be null");
			Preconditions.checkState(domain != null, "domain should not be null");
	
			return new LdapUser(configuration.getUserBaseDn(domain), objectClasses, uid, uidNumber, gidNumber, loginShell,
					cn, displayName, sn, givenName, homeDirectory, userPassword, webAccess,
					mailBox, mailBoxServer, mailAccess, mail, hiddenUser, domain);
		}
	}
	
	private final Dn userBaseDn;
	private final String[] objectClasses;
	private final Uid uid;
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
	private final LdapDomain domain;
	
	private LdapUser(Dn userBaseDn, String[] objectClasses, Uid uid, int uidNumber, int gidNumber, String loginShell,
			String cn, String displayName, String sn, String givenName, String homeDirectory, String userPassword, String webAccess,
			String mailBox, String mailBoxServer, String mailAccess, String mail, boolean hiddenUser, LdapDomain domain) {
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
		this.domain = domain;
	}

	public String[] getObjectClasses() {
		return objectClasses;
	}

	public Uid getUid() {
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

	public LdapDomain getDomain() {
		return domain;
	}

	public Entry buildEntry() throws LdapException {
		LdapEntry.Builder builder = LdapEntry.builder()
				.dn(buildDn());
		for (String objectClass: getObjectClasses()) {
			builder.attribute(Attribute.valueOf("objectClass", objectClass));
		}
		LdapEntry ldapEntry = builder
			.attribute(Attribute.valueOf("uid", uid.get()))
			.attribute(Attribute.valueOf("uidNumber", uidNumber))
			.attribute(Attribute.valueOf("gidNumber", gidNumber))
			.attribute(Attribute.valueOf("loginShell", loginShell))
			.attribute(Attribute.valueOf("cn", cn))
			.attribute(Attribute.valueOf("displayName", displayName))
			.attribute(Attribute.valueOf("sn", sn))
			.attribute(Attribute.valueOf("givenName", givenName))
			.attribute(Attribute.valueOf("homeDirectory", homeDirectory))
			.attribute(Attribute.valueOf("userPassword", userPassword))
			.attribute(Attribute.valueOf("webAccess", webAccess))
			.attribute(Attribute.valueOf("mailBox", mailBox))
			.attribute(Attribute.valueOf("mailBoxServer", mailBoxServer))
			.attribute(Attribute.valueOf("mailAccess", mailAccess))
			.attribute(Attribute.valueOf("mail", mail))
			.attribute(Attribute.valueOf("hiddenUser", hiddenUser))
			.attribute(Attribute.valueOf("obmDomain", domain.get()))
			.build();
		return ldapEntry.toDefaultEntry();
	}
	
	private org.obm.provisioning.ldap.client.bean.Dn buildDn() {
		return org.obm.provisioning.ldap.client.bean.Dn.valueOf(
				"uid=" + getUid().get() + "," + userBaseDn.getName());
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(uid, uidNumber, gidNumber, loginShell, cn, displayName, sn, givenName, 
				homeDirectory, userPassword, webAccess, mailBox, mailBoxServer, mailAccess, mail, hiddenUser, domain);
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
				&& Objects.equal(this.domain, that.domain);
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
			.add("obmDomain", domain)
			.toString();
	}
}
