/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */


package org.obm.imap.archive.beans;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import fr.aliacom.obm.common.user.UserExtId;

public class ExcludedUser {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private UserExtId id;
		private String login;
		private Builder() {}
		
		public Builder id(UserExtId id) {
			Preconditions.checkNotNull(id);
			this.id = id;
			return this;
		}
		
		public Builder login(String login) {
			Preconditions.checkNotNull(login);
			this.login = login;
			return this;
		}
		
		public ExcludedUser build() {
			Preconditions.checkState(id != null);
			Preconditions.checkState(login != null);
			
			return new ExcludedUser(id, login);
		}
	}
	
	private final UserExtId id;
	private final String login;
	
	private ExcludedUser(UserExtId id, String login) {
		this.id = id;
		this.login = login;
	}
	
	public UserExtId getId() {
		return id;
	}
	
	public String getLogin() {
		return login;
	}
	
	public String serializeId() {
		return id.getExtId();
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(id, login);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof ExcludedUser) {
			ExcludedUser that = (ExcludedUser) object;
			return Objects.equal(this.id, that.id)
				&& Objects.equal(this.login, that.login);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("id", id)
			.add("login", login)
			.toString();
	}
}
