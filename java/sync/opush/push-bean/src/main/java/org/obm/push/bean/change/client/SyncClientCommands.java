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
package org.obm.push.bean.change.client;

import java.util.List;

import org.obm.push.bean.change.SyncCommand;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class SyncClientCommands {

	public static SyncClientCommands empty() {
		return builder().build();
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private final List<Add> adds;
		private final List<Change> changes;

		private Builder() {
			adds = Lists.newArrayList();
			changes = Lists.newArrayList();
		}
		
		public Builder putChange(Change change) {
			changes.add(change);
			return this;
		}
		
		public Builder putAdd(Add add) {
			adds.add(add);
			return this;
		}

		public Builder merge(SyncClientCommands clientCommands) {
			adds.addAll(clientCommands.getAdds());
			changes.addAll(clientCommands.getChanges());
			return this;
		}
		
		public SyncClientCommands build() {
			return new SyncClientCommands(ImmutableList.copyOf(adds), ImmutableList.copyOf(changes));
		}
	}

	public static interface SyncClientCommand {
		SyncCommand syncCommand();
	}
	
	public static abstract class Change implements SyncClientCommand {
		
		public final String serverId;

		public Change(String serverId) {
			Preconditions.checkArgument(!Strings.isNullOrEmpty(serverId), "serverId is required");
			this.serverId = serverId;
		}
		
		@Override
		public final int hashCode(){
			return Objects.hashCode(serverId);
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this).add("serverId", serverId).toString();
		}
	}
	
	public static class Update extends Change {

		public Update(String serverId) {
			super(serverId);
		}

		@Override
		public SyncCommand syncCommand() {
			return SyncCommand.CHANGE;
		}

		@Override
		public final boolean equals(Object obj){
			if (obj instanceof Update) {
				return Objects.equal(this.serverId, ((Update)obj).serverId);
			}
			return false;
		}
	}
	
	public static class Deletion extends Change {

		public Deletion(String serverId) {
			super(serverId);
		}

		@Override
		public SyncCommand syncCommand() {
			return SyncCommand.DELETE;
		}

		@Override
		public final boolean equals(Object obj){
			if (obj instanceof Deletion) {
				return Objects.equal(this.serverId, ((Deletion)obj).serverId);
			}
			return false;
		}
	}

	public static class Add implements SyncClientCommand {
		
		public final String serverId;
		public final String clientId;

		public Add(String clientId, String serverId) {
			Preconditions.checkArgument(!Strings.isNullOrEmpty(clientId), "clientId is required");
			Preconditions.checkArgument(!Strings.isNullOrEmpty(serverId), "serverId is required");
			this.clientId = clientId;
			this.serverId = serverId;
		}

		public String getClientId() {
			return clientId;
		}
		
		@Override
		public SyncCommand syncCommand() {
			return SyncCommand.ADD;
		}

		@Override
		public final int hashCode(){
			return Objects.hashCode(clientId, serverId);
		}
		
		@Override
		public final boolean equals(Object object){
			if (object instanceof Add) {
				Add that = (Add) object;
				return Objects.equal(this.clientId, that.clientId)
					&& Objects.equal(this.serverId, that.serverId);
			}
			return false;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)
				.add("clientId", clientId)
				.add("serverId", serverId)
				.toString();
		}
	}
	
	private final List<Add> adds;
	private final List<Change> changes;

	private SyncClientCommands(List<Add> adds, List<Change> changes) {
		this.adds = adds;
		this.changes = changes;
	}
	
	public List<Add> getAdds() {
		return adds;
	}

	public List<Change> getChanges() {
		return changes;
	}

	public int sumOfCommands() {
		return changes.size() + adds.size();
	}

	public boolean hasCommandWithServerId(String serverId) {
		return hasChangeWithServerId(serverId) || hasAddWithServerId(serverId);
	}

	public boolean hasChangeWithServerId(String serverId) {
		return changes.contains(new Update(serverId));
	}

	public boolean hasAddWithServerId(final String serverId) {
		return getAddWithServerId(serverId).isPresent();
	}

	public Change getChange(String serverId) {
		return getChangeWithServerId(serverId).get();
	}

	public Optional<Change> getChangeWithServerId(final String serverId) {
		return FluentIterable.from(changes).firstMatch(new Predicate<Change>() {
			
				@Override
				public boolean apply(Change input) {
					return serverId.equals(input.serverId);
				}
			});
	}

	public Optional<Add> getAddWithServerId(final String serverId) {
		return FluentIterable.from(adds).firstMatch(new Predicate<Add>() {
			
				@Override
				public boolean apply(Add input) {
					return serverId.equals(input.serverId);
				}
			});
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(changes, adds);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SyncClientCommands) {
			SyncClientCommands that = (SyncClientCommands) object;
			return Objects.equal(this.changes, that.changes)
				&& Objects.equal(this.adds, that.adds);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("changes", changes)
			.add("adds", adds)
			.toString();
	}
	
}