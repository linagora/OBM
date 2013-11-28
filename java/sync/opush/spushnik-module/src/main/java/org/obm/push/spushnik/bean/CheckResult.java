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

import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

public class CheckResult {

	public static Builder builder() {
		return new Builder();
	}
	
	public static CheckResult createEmptyRequest() {
		return builder().build();
	}
	
	public static class Builder {
		private CheckStatus checkStatus;
		private final Set<String> messages;
		
		private Builder() {
			messages = Sets.newHashSet();
		}
		
		public Builder checkStatus(CheckStatus checkStatus) {
			this.checkStatus = checkStatus;
			return this;
		}
		
		public Builder addMessage(String message) {
			this.messages.add(message);
			return this;
		}
		
		public CheckResult build() {
			Preconditions.checkState(checkStatus != null, "CheckStatus is required");
			return new CheckResult(checkStatus, messages);
		}
	}
	
	private final int status;
	private final Set<String> messages;
	
	public CheckResult(CheckStatus checkStatus, Set<String> messages) {
		this.status = checkStatus.asSpecificationValue();
		this.messages = messages;
	}
	
	public int getStatus() {
		return status;
	}
	
	public Set<String> getMessages() {
		return messages;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(status, messages);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof CheckResult) {
			CheckResult that = (CheckResult) object;
			return Objects.equal(this.status, that.status)
				&& Objects.equal(this.messages, that.messages);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("status", status)
			.add("messages", messages)
			.toString();
	}
}
