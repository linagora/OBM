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
package org.obm.push.mail.bean;

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

public class NameSpaceInfo {
	
	private List<String> personal;
	private List<String> otherUsers;
	private List<String> mailShares;
	
	public List<String> getPersonal() {
		return personal;
	}
	public void setPersonal(List<String> personal) {
		this.personal = personal;
	}
	public void addPersonal(List<String> personal) {
		if (this.personal == null) {
			this.personal = Lists.newArrayList();
		}
		this.personal.addAll(personal);
	}
	public List<String> getOtherUsers() {
		return otherUsers;
	}
	public void setOtherUsers(List<String> otherUsers) {
		this.otherUsers = otherUsers;
	}
	public void addOtherUsers(List<String> otherUsers) {
		if (this.otherUsers == null) {
			this.otherUsers = Lists.newArrayList();
		}
		this.otherUsers.addAll(otherUsers);
	}
	public List<String> getMailShares() {
		return mailShares;
	}
	public void setMailShares(List<String> mailShares) {
		this.mailShares = mailShares;
	}
	public void addMailShares(List<String> mailShares) {
		if (this.mailShares == null) {
			this.mailShares = Lists.newArrayList();
		}
		this.mailShares.addAll(mailShares);
	}

	public void addAll(NameSpaceInfo nameSpaceInfo) {
		addPersonal(nameSpaceInfo.getPersonal());
		addOtherUsers(nameSpaceInfo.getOtherUsers());
		addMailShares(nameSpaceInfo.getMailShares());
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this).
			add("personal", personal).
			add("otherUsers", otherUsers).
			add("mailShares", mailShares).toString();
	}

}
