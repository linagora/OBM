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
package org.obm.sync.book;

import java.io.Serializable;

import org.obm.annotations.database.DatabaseField;

import com.google.common.base.Objects;

public class Address implements IMergeable, Serializable {

	public static final String ADDRESS_TABLE = "Address";

	private String street;
	private String zipCode;
	private String expressPostal;
	private String town;
	private String country;
	private String state;

	public Address() {
	}

	public Address(String street, String zipCode, String expressPostal,
			String town, String country, String state) {
		super();
		this.street = street;
		this.zipCode = zipCode;
		this.expressPostal = expressPostal;
		this.town = town;
		this.country = country;
		this.state = state;
	}

	@DatabaseField(table = ADDRESS_TABLE, column = "address_country")
	public String getCountry() {
		return country;
	}

	@DatabaseField(table = ADDRESS_TABLE, column = "address_expresspostal")
	public String getExpressPostal() {
		return expressPostal;
	}

	@DatabaseField(table = ADDRESS_TABLE, column = "address_street")
	public String getStreet() {
		return street;
	}

	@DatabaseField(table = ADDRESS_TABLE, column = "address_town")
	public String getTown() {
		return town;
	}

	@DatabaseField(table = ADDRESS_TABLE, column = "address_zipcode")
	public String getZipCode() {
		return zipCode;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setExpressPostal(String expressPostal) {
		this.expressPostal = expressPostal;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public void setTown(String town) {
		this.town = town;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	@Override
	public void merge(IMergeable previous) {
		if (previous instanceof Address) {
			Address prev = (Address) previous;
			if (getStreet() == null && prev.getStreet() != null) {
				setStreet(prev.getStreet());
			}
			if (getCountry() == null && prev.getCountry() != null) {
				setCountry(prev.getCountry());
			}
			if (getExpressPostal() == null && prev.getExpressPostal() != null) {
				setExpressPostal(prev.getExpressPostal());
			}
			if (getZipCode() == null && prev.getZipCode() != null) {
				setZipCode(prev.getZipCode());
			}
			if (getTown() == null && prev.getTown() != null) {
				setTown(prev.getTown());
			}
		}
	}

	@DatabaseField(table = ADDRESS_TABLE, column = "address_state")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(getStreet(), getZipCode(), getExpressPostal(), getTown(), getCountry(), getState());
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof Address) {
			Address that = (Address) object;
			return Objects.equal(this.getStreet(), that.getStreet())
				&& Objects.equal(this.getZipCode(), that.getZipCode())
				&& Objects.equal(this.getExpressPostal(), that.getExpressPostal())
				&& Objects.equal(this.getTown(), that.getTown())
				&& Objects.equal(this.getCountry(), that.getCountry())
				&& Objects.equal(this.getState(), that.getState());
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("street", getStreet())
			.add("zipCode", getZipCode())
			.add("expressPostal", getExpressPostal())
			.add("town", getTown())
			.add("country", getCountry())
			.add("state", getState())
			.toString();
	}
}
