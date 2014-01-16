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
package org.obm.push.bean;

import com.google.common.base.Objects;

public class SearchResult {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private String displayName;
		private String alias;
		private String emailAddress;
		private String firstName;
		private String lastName;
		private String phone;
		private String office;
		private String title;
		private String company;
		private String homePhone;
		private String mobilePhone;
	
		private Builder() {}
		
		public Builder displayName(String displayName) {
			this.displayName = displayName;
			return this;
		}
		
		public Builder alias(String alias) {
			this.alias = alias;
			return this;
		}
		
		public Builder emailAddress(String emailAddress) {
			this.emailAddress = emailAddress;
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
		
		public Builder phone(String phone) {
			this.phone = phone;
			return this;
		}
		
		public Builder office(String office) {
			this.office = office;
			return this;
		}
		
		public Builder title(String title) {
			this.title = title;
			return this;
		}
		
		public Builder company(String company) {
			this.company = company;
			return this;
		}
		
		public Builder homePhone(String homePhone) {
			this.homePhone = homePhone;
			return this;
		}
		
		public Builder mobilePhone(String mobilePhone) {
			this.mobilePhone = mobilePhone;
			return this;
		}
		
		public SearchResult build() {
			return new SearchResult(displayName, alias, emailAddress, firstName, lastName, phone, office, title, company, homePhone, mobilePhone);
		}
	}
	
	private final String displayName;
	private final String alias;
	private final String emailAddress;
	private final String firstName;
	private final String lastName;
	private final String phone;
	private final String office;
	private final String title;
	private final String company;
	private final String homePhone;
	private final String mobilePhone;

	private SearchResult(String displayName, String alias, String emailAddress, String firstName, String lastName, 
		String phone, String office, String title, String company, String homePhone, String mobilePhone) {
		this.displayName = displayName;
		this.alias = alias;
		this.emailAddress = emailAddress;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phone = phone;
		this.office = office;
		this.title = title;
		this.company = company;
		this.homePhone = homePhone;
		this.mobilePhone = mobilePhone;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public String getAlias() {
		return alias;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getPhone() {
		return phone;
	}

	public String getOffice() {
		return office;
	}

	public String getTitle() {
		return title;
	}

	public String getCompany() {
		return company;
	}

	public String getHomePhone() {
		return homePhone;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(displayName, alias, emailAddress, firstName, lastName, 
				phone, office, title, company, homePhone, mobilePhone);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SearchResult) {
			SearchResult that = (SearchResult) object;
			return Objects.equal(this.displayName, that.displayName)
				&& Objects.equal(this.alias, that.alias)
				&& Objects.equal(this.emailAddress, that.emailAddress)
				&& Objects.equal(this.firstName, that.firstName)
				&& Objects.equal(this.lastName, that.lastName)
				&& Objects.equal(this.phone, that.phone)
				&& Objects.equal(this.office, that.office)
				&& Objects.equal(this.title, that.title)
				&& Objects.equal(this.company, that.company)
				&& Objects.equal(this.homePhone, that.homePhone)
				&& Objects.equal(this.mobilePhone, that.mobilePhone);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("displayName", displayName)
			.add("alias", alias)
			.add("emailAddress", emailAddress)
			.add("firstName", firstName)
			.add("lastName", lastName)
			.add("Phone", phone)
			.add("Office", office)
			.add("Title", title)
			.add("Company", company)
			.add("HomePhone", homePhone)
			.add("MobilePhone", mobilePhone)
			.toString();
	}
}
