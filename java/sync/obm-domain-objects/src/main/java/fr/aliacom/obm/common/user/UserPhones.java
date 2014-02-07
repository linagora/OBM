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

import java.util.List;

import org.obm.push.utils.collection.ImmutableFixedSizeList;

import com.google.common.base.Objects;

public class UserPhones {

	private static final UserPhones EMPTY = builder().build();
	
	public static class Builder {

		private static final int MAXIMUM_SUPPORTED_FAXES = 2;
		private static final int MAXIMUM_SUPPORTED_PHONES = 2;

		private ImmutableFixedSizeList.Builder<String> phones;
		private ImmutableFixedSizeList.Builder<String> faxes;
		private String mobile;

		private Builder() {
			this.phones = ImmutableFixedSizeList.<String>builder().size(MAXIMUM_SUPPORTED_PHONES);
			this.faxes = ImmutableFixedSizeList.<String>builder().size(MAXIMUM_SUPPORTED_FAXES);
		}

		public Builder from(UserPhones other) {
			this.phones.addAll(other.phones);
			this.faxes.addAll(other.faxes);
			this.mobile = other.mobile;
			return this;
		}
		
		public Builder addPhone(String phone) {
			this.phones.add(phone);
			return this;
		}
		
		public Builder phones(Iterable<String> phones) {
			this.phones.addAll(phones);
			return this;
		}

		public Builder addFax(String fax) {
			this.faxes.add(fax);
			return this;
		}
		
		public Builder faxes(Iterable<String> faxes) {
			this.faxes.addAll(faxes);
			return this;
		}

		public Builder mobile(String mobile) {
			this.mobile = mobile;
			return this;
		}

		public UserPhones build() {
			return new UserPhones(phones.build(), faxes.build(), mobile);
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	public static UserPhones empty() {
		return EMPTY;
	}
	
	private final List<String> phones;
	private final List<String> faxes;
	private final String mobile;

	private UserPhones(List<String> phones,
			List<String> faxes,
			String mobile) {

		this.phones = phones;
		this.faxes = faxes;
		this.mobile = mobile;
	}

	public List<String> getPhones() {
		return phones;
	}

	public String getPhone1() {
		return phones.get(0);
	}

	public String getPhone2() {
		return phones.get(1);
	}

	public List<String> getFaxes() {
		return faxes;
	}

	public String getFax1() {
		return faxes.get(0);
	}

	public String getFax2() {
		return faxes.get(1);
	}

	public String getMobile() {
		return mobile;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(phones, faxes, mobile);
	}

	@Override
	public boolean equals(Object object){
		if (object instanceof UserPhones) {
			UserPhones that = (UserPhones) object;
			return Objects.equal(this.phones, that.phones)
					&& Objects.equal(this.faxes, that.faxes)
					&& Objects.equal(this.mobile, that.mobile);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("phones", phones)
				.add("faxes", faxes)
				.add("mobile", mobile)
				.toString();
	}

}

