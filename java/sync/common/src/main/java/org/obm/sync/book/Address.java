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

import com.google.common.base.Objects;

public class Address implements IMergeable, Serializable {

	private String street;
	private String zipCode;
	private String expressPostal;
	private String town;
	private String country;
	private String state;

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

	public String getCountry() {
		return country;
	}

	public String getExpressPostal() {
		return expressPostal;
	}

	public String getStreet() {
		return street;
	}

	public String getTown() {
		return town;
	}

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

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(street, zipCode, expressPostal, town, country, state);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof Address) {
			Address that = (Address) object;
			return Objects.equal(this.street, that.street)
				&& Objects.equal(this.zipCode, that.zipCode)
				&& Objects.equal(this.expressPostal, that.expressPostal)
				&& Objects.equal(this.town, that.town)
				&& Objects.equal(this.country, that.country)
				&& Objects.equal(this.state, that.state);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("street", street)
			.add("zipCode", zipCode)
			.add("expressPostal", expressPostal)
			.add("town", town)
			.add("country", country)
			.add("state", state)
			.toString();
	}
}
