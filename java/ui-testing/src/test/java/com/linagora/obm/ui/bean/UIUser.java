/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package com.linagora.obm.ui.bean;

import java.util.Date;

import lombok.Data;

import com.google.common.base.Objects;

@Data
public class UIUser {
	
	public static Builder builder() {
		return new Builder();
	}
	public static UIUser admin0() {
		return builder().login("admin0").password("admin").profile(UIUserProfile.ADMIN).build();
	}
	
	public static UIUser admin() { 
		String admin = Objects.firstNonNull(System.getProperty("admin"), "admin");
		return builder().login(admin).password(admin).commonName(admin).profile(UIUserProfile.ADMIN).build();
	}
	
	public static UIUser user() {
		String user = Objects.firstNonNull(System.getProperty("user"), "userb");
		return builder().login(user).password(user).commonName(user).profile(UIUserProfile.USER).build();		
	}
	
	public static class Builder {
		
		private String login;
		private String password;
		private String firstName;
		private String lastName;
		private String commonName;
		private boolean mailboxHidden;
		private boolean mailboxArchive;
		private String delegation;
		private String title;
		private Date dateBegin;
		private Date dateExpire;
		private boolean noExpire;
		private String phone;
		private String phone2;
		private String phoneMobile;
		private String phoneFax;
		private String phoneFax2;
		private String company;
		private String direction;
		private String service;
		private String address1;
		private String address2;
		private String address3;
		private String addressZip;
		private String addressTown;
		private String addressCedex;
		private String description;
		private boolean emailInternalEnabled;
		private String emailAddress;
		private UIUserKind kind;
		private UIUserProfile profile;
		
		private Builder() {
			profile = UIUserProfile.USER;
		}
		
		public Builder login(String login) {
			this.login = login;
			return this;
		}
		
		public Builder password(String password) {
			this.password = password;
			return this;
		}
		
		public Builder commonName(String commonName) {
			this.commonName = commonName;
			return this;
		}
		
		public Builder firstName(String firstName) {
			this.firstName = firstName;
			return this;
		}
		
		public Builder lastName(String lastName) {
			this.lastName = lastName;
			return this;
		}
		
		public Builder mailboxHidden(boolean mailboxHidden) {
			this.mailboxHidden = mailboxHidden;
			return this;
		}
		
		public Builder mailboxArchive(boolean mailboxArchive) {
			this.mailboxArchive = mailboxArchive;
			return this;
		}
		
		public Builder delegation(String delegation) {
			this.delegation = delegation;
			return this;
		}
		
		public Builder title(String title) {
			this.title = title;
			return this;
		}
		
		public Builder dateBegin(Date dateBegin) {
			this.dateBegin = dateBegin;
			return this;
		}
		
		public Builder dateExpire(Date dateExpire) {
			this.dateExpire = dateExpire;
			return this;
		}
		
		public Builder noExpire(boolean noExpire) {
			this.noExpire = noExpire;
			return this;
		}
		
		public Builder phone(String phone) {
			this.phone = phone;
			return this;
		}
		
		public Builder phone2(String phone2) {
			this.phone2 = phone2;
			return this;
		}
		
		public Builder phoneMobile(String phoneMobile) {
			this.phoneMobile = phoneMobile;
			return this;
		}
		
		public Builder phoneFax(String phoneFax) {
			this.phoneFax = phoneFax;
			return this;
		}
		
		public Builder phoneFax2(String phoneFax2) {
			this.phoneFax2 = phoneFax2;
			return this;
		}
		
		public Builder company(String company) {
			this.company = company;
			return this;
		}
		
		public Builder direction(String direction) {
			this.direction = direction;
			return this;
		}
		
		public Builder service(String service) {
			this.service = service;
			return this;
		}
		
		public Builder address1(String address1) {
			this.address1 = address1;
			return this;
		}
		
		public Builder address2(String address2) {
			this.address2 = address2;
			return this;
		}
		
		public Builder address3(String address3) {
			this.address3 = address3;
			return this;
		}
		
		public Builder addressZip(String addressZip) {
			this.addressZip = addressZip;
			return this;
		}
		
		public Builder addressTown(String addressTown) {
			this.addressTown = addressTown;
			return this;
		}
		
		public Builder addressCedex(String addressCedex) {
			this.addressCedex = addressCedex;
			return this;
		}
		
		public Builder description(String description) {
			this.description = description;
			return this;
		}
		
		public Builder emailInternalEnabled(boolean emailInternalEnabled) {
			this.emailInternalEnabled = emailInternalEnabled;
			return this;
		}
		
		public Builder emailAddress(String emailAddress) {
			this.emailAddress = emailAddress;
			return this;
		}
		
		public Builder kind(UIUserKind kind) {
			this.kind = kind;
			return this;
		}

		public Builder profile(UIUserProfile profile) {
			this.profile = profile;
			return this;
		}

		public UIUser build() {
			return new UIUser(
					nullToEmpty(login), nullToEmpty(password), nullToEmpty(firstName), nullToEmpty(lastName),
					nullToEmpty(commonName), mailboxHidden, mailboxArchive,
					nullToEmpty(delegation), nullToEmpty(title), dateBegin, dateExpire,
					noExpire, nullToEmpty(phone), nullToEmpty(phone2), nullToEmpty(phoneMobile),
					nullToEmpty(phoneFax), nullToEmpty(phoneFax2), nullToEmpty(company), nullToEmpty(direction), 
					nullToEmpty(service), nullToEmpty(address1), nullToEmpty(address2), nullToEmpty(address3),
					nullToEmpty(addressZip), nullToEmpty(addressTown), nullToEmpty(addressCedex), 
					nullToEmpty(description), emailInternalEnabled, nullToEmpty(emailAddress),
					kind, profile);
		}
		
		private String nullToEmpty(String string) {
			if (string == null) {
				return "";
			}
			return string;
		}
	}
	
	private final String login;
	private final String password;
	private final String firstName;
	private final String lastName;
	private final String commonName;
	private final boolean mailboxHidden;
	private final boolean mailboxArchive;
	private final String delegation;
	private final String title;
	private final Date dateBegin;
	private final Date dateExpire;
	private final boolean noExpire;
	private final String phone;
	private final String phone2;
	private final String phoneMobile;
	private final String phoneFax;
	private final String phoneFax2;
	private final String company;
	private final String direction;
	private final String service;
	private final String address1;
	private final String address2;
	private final String address3;
	private final String addressZip;
	private final String addressTown;
	private final String addressCedex;
	private final String description;
	private final boolean emailInternalEnabled;
	private final String emailAddress;
	private final UIUserKind kind;
	private final UIUserProfile profile;
	
	public boolean hasKindDefined() {
		return kind != null;
	}
	
}
