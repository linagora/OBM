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

import com.google.common.base.Objects;

public class UIDomain {

	public static Builder builder() {
		return new Builder();
	}
	public static UIDomain globalDomain() {
		return builder().name("Global Domain").selectValue(1).build();
	}
	
	public static class Builder {
		
		private String name;
		private int selectValue;
		
		private Builder() {
			super();
		}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder selectValue(int selectValue) {
			this.selectValue = selectValue;
			return this;
		}
		
		public UIDomain build() {
			return new UIDomain(name, selectValue);
		}
		
	}
	
	private final String name;
	private final int selectValue;
	
	public UIDomain(String name, int selectValue) {
		this.name = name;
		this.selectValue = selectValue;
	}
	
	public String getName() {
		return name;
	}
	
	public int getSelectValue() {
		return selectValue;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(name, selectValue);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof UIDomain) {
			UIDomain that = (UIDomain) object;
			return Objects.equal(this.name, that.name)
				&& Objects.equal(this.selectValue, that.selectValue);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("name", name)
			.add("selectValue", selectValue)
			.toString();
	}
	
}
