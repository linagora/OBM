/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package fr.aliacom.obm.common.user;

import org.obm.sync.utils.DisplayNameUtils;

import com.google.common.base.Objects;

public class UserIdentity {

	private static final UserIdentity EMPTY = builder().build();
	
	public static class Builder {

		private String lastName;
		private String firstName;
		private String commonName;
		private String kind;

		private Builder() {
		}

		public Builder from(UserIdentity name) {
			lastName(name.lastName);
			firstName(name.firstName);
			commonName(name.commonName);
			kind(name.kind);
			return this;
		}
		
		public Builder lastName(String lastName) {
			this.lastName = lastName;
			return this;
		}

		public Builder firstName(String firstName) {
			this.firstName = firstName;
			return this;
		}

		public Builder commonName(String commonName) {
			this.commonName = commonName;
			return this;
		}

		public Builder kind(String kind) {
			this.kind = kind;
			return this;
		}
		
		public UserIdentity build() {
			return new UserIdentity(lastName, firstName, commonName, kind);
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	public static UserIdentity empty() {
		return EMPTY;
	}
	
	private final String lastName;
	private final String firstName;
	private final String commonName;
	private final String kind;
	
	private UserIdentity(String lastName, String firstName, String commonName, String kind) {
		super();
		this.lastName = lastName;
		this.firstName = firstName;
		this.commonName = commonName;
		this.kind = kind;
	}

	public String getCommonName() {
		return commonName;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public String getKind() {
		return kind;
	}
	
	public String getDisplayName() {
		return DisplayNameUtils.getDisplayName(commonName, firstName, lastName);
	}
	
	@Override
	public int hashCode(){
		return Objects.hashCode(lastName, firstName, commonName, kind);
	}

	@Override
	public boolean equals(Object object){
		if (object instanceof UserIdentity) {
			UserIdentity that = (UserIdentity) object;
			return Objects.equal(this.lastName, that.lastName)
					&& Objects.equal(this.firstName, that.firstName)
					&& Objects.equal(this.commonName, that.commonName)
					&& Objects.equal(this.kind, that.kind);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("lastName", lastName)
				.add("firstName", firstName)
				.add("commonName", commonName)
				.add("kind", kind)
				.toString();

	}
	
}