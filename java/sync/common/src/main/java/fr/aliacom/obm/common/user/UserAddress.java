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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.obm.push.utils.collection.ImmutableFixedSizeList;

import com.google.common.base.Objects;

public class UserAddress {

	private static final UserAddress EMPTY = builder().build();

	public static class Builder {

		private static final int MAXIMUM_SUPPORTED_ADDRESSES = 3;

		private ImmutableFixedSizeList.Builder<String> addressParts;
		private String town;
		private String zipCode;
		private String expressPostal;
		private String countryCode;

		private Builder() {
			this.addressParts = ImmutableFixedSizeList.<String>builder().size(MAXIMUM_SUPPORTED_ADDRESSES);
		}

		public Builder from(UserAddress address) {
			this.addressParts.addAll(address.getAddressParts());
			this.town = address.getTown();
			this.zipCode = address.getZipCode();
			this.expressPostal = address.getExpressPostal();
			this.countryCode = address.getCountryCode();
			return this;
		}
		
		public Builder addressPart(String addressPart) {
			this.addressParts.add(addressPart);
			return this;
		}
		
		public Builder addressParts(Collection<String> addressParts) {
			this.addressParts.addAll(addressParts);
			return this;
		}

		public Builder town(String town) {
			this.town = town;
			return this;
		}

		public Builder zipCode(String zipCode) {
			this.zipCode = zipCode;
			return this;
		}
		public Builder expressPostal(String expressPostal) {
			this.expressPostal = expressPostal;
			return this;
		}

		public Builder countryCode(String countryCode) {
			this.countryCode = countryCode;
			return this;
		}

		public UserAddress build() {
			return new UserAddress(addressParts.build(), town, zipCode, expressPostal, countryCode);
		}

	}

	public static Builder builder() {
		return new Builder();
	}

	public static UserAddress empty() {
		return EMPTY;
	}
	
	private List<String> addressParts;

	private String town;
	private String zipCode;
	private String expressPostal;
	private String countryCode;

	private UserAddress(List<String> addressParts,
			String town, String zipCode, String expressPostal, String countryCode) {
		this.addressParts = addressParts;
		this.town = town;
		this.zipCode = zipCode;
		this.expressPostal = expressPostal;
		this.countryCode = countryCode;
	}

	public List<String> getAddressParts() {
		return Arrays.asList(getAddress1(), getAddress2(), getAddress3());
	}

	public String getAddress1() {
		return addressParts.get(0);
	}

	public String getAddress2() {
		return addressParts.get(1);
	}

	public String getAddress3() {
		return addressParts.get(2);
	}

	public String getTown() {
		return town;
	}

	public String getZipCode() {
		return zipCode;
	}

	public String getExpressPostal() {
		return expressPostal;
	}

	public String getCountryCode() {
		return countryCode;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(addressParts, town, zipCode, expressPostal, countryCode);
	}

	@Override
	public boolean equals(Object object){
		if (object instanceof UserAddress) {
			UserAddress that = (UserAddress) object;
			return Objects.equal(this.addressParts, that.addressParts)
					&& Objects.equal(this.town, that.town)
					&& Objects.equal(this.zipCode, that.zipCode)
					&& Objects.equal(this.expressPostal, that.expressPostal)
					&& Objects.equal(this.countryCode, that.countryCode);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("addresses", addressParts)
				.add("town", town)
				.add("zipcode", zipCode)
				.add("businessZipcode", expressPostal)
				.add("country", countryCode)
				.toString();
	}

}
