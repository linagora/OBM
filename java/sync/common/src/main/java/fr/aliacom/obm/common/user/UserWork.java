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

import com.google.common.base.Objects;

public class UserWork {

	private static final UserWork EMPTY = builder().build();
	
	public static class Builder {
		
		private String service;
		private String title;
		private String company;
		private String direction;
		
		private Builder() {
		}

		public Builder from(UserWork work) {
			this.service = work.service;
			this.title = work.title;
			this.company = work.company;
			this.direction = work.direction;
			return this;
		}
		
		public Builder service(String service) {
			this.service = service;
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
		
		public Builder direction(String direction) {
			this.direction = direction;
			return this;
		}
		
		public UserWork build() {
			return new UserWork(service, title, company, direction);
		}

	}

	public static Builder builder() {
		return new Builder();
	}
	
	public static UserWork empty() {
		return EMPTY;
	}

	
	private final String service;
	private final String title;
	private final String company;
	private final String direction;
	
	private UserWork(String service, String title, String company, String direction) {
		this.service = service;
		this.title = title;
		this.company = company;
		this.direction = direction;
	}
	
	public String getCompany() {
		return company;
	}
	
	public String getDirection() {
		return direction;
	}
	
	public String getService() {
		return service;
	}
	
	public String getTitle() {
		return title;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(service, title, company, direction);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof UserWork) {
			UserWork that = (UserWork) object;
			return Objects.equal(this.service, that.service)
				&& Objects.equal(this.title, that.title)
				&& Objects.equal(this.company, that.company)
				&& Objects.equal(this.direction, that.direction);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("service", service)
			.add("title", title)
			.add("company", company)
			.add("direction", direction)
			.toString();
	}
	
}
