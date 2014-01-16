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
package org.obm.push.bean;

import java.io.Serializable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

public class MSAddress implements Serializable {
	
	private static final long serialVersionUID = 7029809664557900622L;
	
	private final String mail;
	private final String displayName;

	public MSAddress(String mail) {
		this(null, mail);
	}
	
	public MSAddress(String displayName, String mail) {
		super();
		this.displayName = displayName;
		this.mail = mail;
	}
	
	public String getMail() {
		return mail;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String toMSProtocol() {
		StringBuilder sb = new StringBuilder();
		if (!Strings.isNullOrEmpty(displayName)) {
			sb.append("\"").append(displayName).append("\"");
		}
		if (!Strings.isNullOrEmpty(mail)) {
			sb.append(" <").append(mail).append("> ");
		}
		return sb.toString();
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(mail, displayName);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSAddress) {
			MSAddress that = (MSAddress) object;
			return Objects.equal(this.mail, that.mail)
				&& Objects.equal(this.displayName, that.displayName);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("mail", mail)
			.add("displayName", displayName)
			.toString();
	}

}
