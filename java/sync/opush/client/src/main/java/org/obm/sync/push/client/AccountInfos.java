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
package org.obm.sync.push.client;

import org.apache.commons.codec.binary.Base64;
import com.google.common.base.Objects;

public final class AccountInfos {
	private String login;
	private String password;
	private String userId;
	private String devId;
	private String devType;
	private String url;
	private String userAgent;

	public AccountInfos(String login, String password, String devId,
			String devType, String url, String userAgent) {
		this.login = login;
		int idx = login.indexOf('@');
		if (idx > 0) {
			String d = login.substring(idx + 1);
			this.userId = d + "\\" + login.substring(0, idx);
		}

		this.password = password;
		this.devId = devId;
		this.devType = devType;
		this.url = url;
		this.userAgent = userAgent;
	}

	public String getLogin() {
		return login;
	}

	public String getPassword() {
		return password;
	}

	public String getUserId() {
		return userId;
	}

	public String getDevId() {
		return devId;
	}

	public String getDevType() {
		return devType;
	}

	public String getUrl() {
		return url;
	}

	public String getUserAgent() {
		return userAgent;
	}
	
	public String authValue() {
		StringBuilder sb = new StringBuilder();
		sb.append("Basic ");
		String unCodedString = userId + ":" + password;
		String encoded = new String(Base64.encodeBase64(unCodedString.getBytes()));
		sb.append(encoded);
		String ret = sb.toString();
		return ret;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(login, password, userId, devId, devType, url, userAgent);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof AccountInfos) {
			AccountInfos that = (AccountInfos) object;
			return Objects.equal(this.login, that.login)
				&& Objects.equal(this.password, that.password)
				&& Objects.equal(this.userId, that.userId)
				&& Objects.equal(this.devId, that.devId)
				&& Objects.equal(this.devType, that.devType)
				&& Objects.equal(this.url, that.url)
				&& Objects.equal(this.userAgent, that.userAgent);
		}
		return false;
	}

}
