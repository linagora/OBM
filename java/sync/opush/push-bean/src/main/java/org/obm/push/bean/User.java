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
package org.obm.push.bean;

import java.io.Serializable;

import org.obm.push.utils.UserEmailParserUtils;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;

public class User implements Serializable {

	@Singleton
	public static class Factory {

		private final UserEmailParserUtils userEmailParserUtils;

		@Inject
		private Factory(UserEmailParserUtils userEmailParserUtils) {
			this.userEmailParserUtils = userEmailParserUtils;
		}
		
		public static Factory create() {
			return new Factory(new UserEmailParserUtils());
		}
		
		public String getLoginAtDomain(String userId) {
			return createUser(userId, null, null).getLoginAtDomain();
		}
		
		public User createUser(String userId, String email, String displayName) {
			return new User(userEmailParserUtils.getLogin(userId),
							userEmailParserUtils.getDomain(userId), 
							email, displayName);
		}
		
	}
	
	private static final long serialVersionUID = -3352107588631943099L;
	
	private final String login;
	private final String domain;
	
	private final String email;
	private final String displayName;

	private User(String login, String domain, String email, String displayName) {
		super();
		this.login = login;
		this.domain = domain;
		this.email = email;
		this.displayName = displayName;
	}
	
	public String getLoginAtDomain() {
		return getLogin() + "@" + getDomain();
	}

	public String getLogin() {
		return login.toLowerCase();
	}
	
	public String getDomain() {
		return domain.toLowerCase();
	}
	
	public String getEmail() {
		return email;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(login, domain, email, displayName);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof User) {
			User that = (User) object;
			return Objects.equal(this.login, that.login)
				&& Objects.equal(this.domain, that.domain)
				&& Objects.equal(this.email, that.email)
				&& Objects.equal(this.displayName, that.displayName);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("login", login)
			.add("domain", domain)
			.add("email", email)
			.add("displayName", displayName)
			.toString();
	}
	
}
