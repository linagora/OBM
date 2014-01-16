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

import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

public class SyncCollectionRequestCommands {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private List<String> fetchIds;
		private List<SyncCollectionRequestCommand> commands;

		private Builder() {}
		
		public Builder fetchIds(List<String> fetchIds) {
			this.fetchIds = fetchIds;
			return this;
		}
		
		public Builder commands(List<SyncCollectionRequestCommand> commands) {
			this.commands = commands;
			return this;
		}
		
		public SyncCollectionRequestCommands build() {
			if (fetchIds == null) {
				fetchIds = ImmutableList.of();
			}
			if (commands == null) {
				commands = ImmutableList.of();
			}
			
			return new SyncCollectionRequestCommands(fetchIds, commands);
		}
	}
	
	private final List<String> fetchIds;
	private final List<SyncCollectionRequestCommand> commands;
	
	private SyncCollectionRequestCommands(List<String> fetchIds, List<SyncCollectionRequestCommand> commands) {
		this.fetchIds = fetchIds;
		this.commands = commands;
	}

	public List<String> getFetchIds() {
		return fetchIds;
	}
	
	public List<SyncCollectionRequestCommand> getCommands() {
		return commands;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(fetchIds, commands);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SyncCollectionRequestCommands) {
			SyncCollectionRequestCommands that = (SyncCollectionRequestCommands) object;
			return Objects.equal(this.fetchIds, that.fetchIds)
				&& Objects.equal(this.commands, that.commands);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("fetchIds", fetchIds)
			.add("commands", commands)
			.toString();
	}
	
}
