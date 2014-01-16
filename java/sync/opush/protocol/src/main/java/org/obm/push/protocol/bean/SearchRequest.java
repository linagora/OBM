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
package org.obm.push.protocol.bean;

import org.obm.push.bean.StoreName;

import com.google.common.base.Objects;

public class SearchRequest {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private StoreName storeName;
		private String query;
		private Integer rangeLower;
		private Integer rangeUpper;

		private Builder() {}
		
		public Builder storeName(StoreName storeName) {
			this.storeName = storeName;
			return this;
		}
		
		public Builder query(String query) {
			this.query = query;
			return this;
		}
		
		public Builder rangeLower(Integer rangeLower) {
			this.rangeLower = rangeLower;
			return this;
		}
		
		public Builder rangeUpper(Integer rangeUpper) {
			this.rangeUpper = rangeUpper;
			return this;
		}
		
		public SearchRequest build() {
			return new SearchRequest(storeName, query, rangeLower, rangeUpper);
		}
	}
	
	private final StoreName storeName;
	private final String query;
	private final Integer rangeLower;
	private final Integer rangeUpper;
	
	private SearchRequest(StoreName storeName, String query, Integer rangeLower, Integer rangeUpper) {
		this.storeName = storeName;
		this.query = query;
		this.rangeLower = rangeLower;
		this.rangeUpper = rangeUpper;
	}
	
	public StoreName getStoreName() {
		return storeName;
	}

	public String getQuery() {
		return query;
	}

	public Integer getRangeLower() {
		return rangeLower;
	}

	public Integer getRangeUpper() {
		return rangeUpper;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(storeName, query, rangeLower, rangeUpper);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SearchRequest) {
			SearchRequest that = (SearchRequest) object;
			return Objects.equal(this.storeName, that.storeName)
				&& Objects.equal(this.query, that.query)
				&& Objects.equal(this.rangeLower, that.rangeLower)
				&& Objects.equal(this.rangeUpper, that.rangeUpper);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("storeName", storeName)
			.add("query", query)
			.add("rangeLower", rangeLower)
			.add("rangeUpper", rangeUpper)
			.toString();
	}
}
