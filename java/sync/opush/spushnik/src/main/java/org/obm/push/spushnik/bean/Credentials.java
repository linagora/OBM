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
package org.obm.push.spushnik.bean;

import java.util.Arrays;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class Credentials {

	public static Builder builder() {
		return new Builder();
	}
	
	public static Credentials createEmptyRequest() {
		return builder().build();
	}
	
	public static class Builder {
		private String loginAtDomain;
		private String password;
		private String pkcs12Password;
		private byte[] pkcs12;
		
		private Builder() {
			super();
		}
		
		public Builder loginAtDomain(String loginAtDomain) {
			this.loginAtDomain = loginAtDomain;
			return this;
		}
		
		public Builder password(String password) {
			this.password = password;
			return this;
		}
		
		public Builder pkcs12(byte[] pkcs12) {
			this.pkcs12 = pkcs12;
			return this;
		}

		public Builder pkcs12Password(String pkcs12Password) {
			this.pkcs12Password = pkcs12Password;
			return this;
		}
		
		public Credentials build() {
			Preconditions.checkState(loginAtDomain != null, "loginAtDomain is required");
			Preconditions.checkState(password != null, "password is required");
			return new Credentials(loginAtDomain, password, pkcs12Password, pkcs12);
		}
	}
	
	private final String loginAtDomain;
	private final String password;
	private final String pkcs12Password;
	private final byte[] pkcs12;

	private Credentials() {
		this(null, null, null, null);
	}
	
	private Credentials(String loginAtDomain, String password, String pkcs12Password, byte[] pkcs12) {
		this.loginAtDomain = loginAtDomain;
		this.password = password;
		this.pkcs12 = pkcs12;
		this.pkcs12Password = pkcs12Password;
	}
	
	public String getLoginAtDomain() {
		return loginAtDomain;
	}
	
	public String getPassword() {
		return password;
	}
	
	public byte[] getPkcs12() {
		return pkcs12;
	}
	
	public char[] getPkcs12Password() {
		if (pkcs12Password == null) {
			return null;
		}
		return pkcs12Password.toCharArray();
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(loginAtDomain, password, Arrays.hashCode(pkcs12), pkcs12Password);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Credentials) {
			Credentials that = (Credentials) object;
			return Objects.equal(this.loginAtDomain, that.loginAtDomain)
				&& Objects.equal(this.password, that.password)
				&& Objects.equal(this.pkcs12Password, that.pkcs12Password)
				&& Arrays.equals(this.pkcs12, that.pkcs12);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("loginAtDomain", loginAtDomain)
			.add("password", password)
			.add("pkcs12Password", pkcs12Password)
			.add("pkcs12", pkcs12)
			.toString();
	}
}
