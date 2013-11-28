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
import java.util.List;

import org.obm.push.bean.change.SyncCommand;
import org.obm.push.bean.change.client.SyncClientCommands;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;

public abstract class SyncCollectionCommands<T extends SyncCollectionCommand> implements Serializable {

	private static final long serialVersionUID = 5403154747427044879L;

	public static class Request extends SyncCollectionCommands<SyncCollectionCommand.Request> {
		
		private static final long serialVersionUID = 7346187155191351839L;

		private Request(
				ImmutableListMultimap<SyncCommand, SyncCollectionCommand.Request> commandsByType, 
				List<SyncCollectionCommand.Request> commands) {
			super(commandsByType, commands);
		}
		
		public static Builder builder() {
			return new Builder();
		}

		public static class Builder extends SyncCollectionCommands.Builder<SyncCollectionCommand.Request, Request> {
			
			private Builder() {
				super();
			}
			
			@Override
			protected Request buildImpl(
					ImmutableListMultimap<SyncCommand, SyncCollectionCommand.Request> commandsByType,
					List<SyncCollectionCommand.Request> commands) {
				return new Request(commandsByType, commands);
			}
		}
		
	}
	
	public static class Response extends SyncCollectionCommands<SyncCollectionCommand.Response> {
		
		private static final long serialVersionUID = -6871877347639563687L;

		private Response(
				ImmutableListMultimap<SyncCommand, SyncCollectionCommand.Response> commandsByType, 
				List<SyncCollectionCommand.Response> commands) {
			super(commandsByType, commands);
		}
		
		public static Builder builder() {
			return new Builder();
		}
		
		public static class Builder extends SyncCollectionCommands.Builder<SyncCollectionCommand.Response, Response> {
			
			private Builder() {
				super();
			}
			
			@Override
			protected Response buildImpl(
					ImmutableListMultimap<SyncCommand, SyncCollectionCommand.Response> commandsByType, 
					List<SyncCollectionCommand.Response> commands) {
				return new Response(commandsByType, commands);
			}
			
			public Builder changes(List<ItemChange> changes, SyncClientCommands clientCommands) {
				for (ItemChange change: changes) {
					String serverId = change.getServerId();
					
					SyncCollectionCommand.Response.Builder builder = SyncCollectionCommand.Response.builder();
					builder.applicationData(change.getData())
						.commandType(retrieveCommandType(change))
						.serverId(serverId);
					
					if (clientCommands.hasAddWithServerId(serverId)){
						builder.clientId(clientCommands.getAddWithServerId(serverId).get().getClientId());
					}
					addCommand(builder.build());
				}
				return this;
			}

			public Builder fetchs(List<ItemChange> fetchs) {
				for (ItemChange fetch: fetchs) {
					addCommand(
							SyncCollectionCommand.Response.builder()
								.applicationData(fetch.getData())
								.commandType(SyncCommand.FETCH)
								.serverId(fetch.getServerId())
								.build());
				}
				return this;
			}

			private SyncCommand retrieveCommandType(ItemChange change) {
				return change.isNew() ? SyncCommand.ADD : SyncCommand.CHANGE;
			}

			public Builder deletions(List<ItemDeletion> deletions) {
				for (ItemDeletion deletion: deletions) {
					addCommand(
							SyncCollectionCommand.Response.builder()
								.commandType(SyncCommand.DELETE)
								.serverId(deletion.getServerId())
								.build());
				}
				return this;
			}
		}
	}
	
	public abstract static class Builder<T extends SyncCollectionCommand, C extends SyncCollectionCommands<?>> {
		private final ImmutableList.Builder<T> commandsBuilder;

		private Builder() {
			super();
			commandsBuilder = ImmutableList.builder();
		}
		
		public Builder<T, C> addCommand(T command) {
			commandsBuilder.add(command);
			return this;
		}
		
		public C build() {
			ImmutableList<T> commands = this.commandsBuilder.build();
			return buildImpl(commandsByType(commands), commands);
		}
		
		protected abstract C buildImpl(
			ImmutableListMultimap<SyncCommand, T> commandsByType, 
			List<T> commands);
		
		private ImmutableListMultimap<SyncCommand, T> commandsByType(List<T> commands) {
			return FluentIterable.from(commands)
						.index(new Function<T, SyncCommand>() {

					@Override
					public SyncCommand apply(T input) {
						return input.getType();
					}
				});
		}
	}
	
	private final ImmutableListMultimap<SyncCommand, T> commandsByType;
	private final List<T> commands;
	
	private SyncCollectionCommands(
		ImmutableListMultimap<SyncCommand, T> commandsByType, 
		List<T> commands) {
		
		this.commandsByType = commandsByType;
		this.commands = commands;
	}

	public List<T> getCommandsForType(SyncCommand type) {
		return Objects.firstNonNull(commandsByType.get(type), ImmutableList.<T>of());
	}
	
	public List<T> getCommands() {
		return commands;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(commandsByType, commands);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SyncCollectionCommands) {
			SyncCollectionCommands<?> that = (SyncCollectionCommands<?>) object;
			return Objects.equal(this.commandsByType, that.commandsByType)
				&& Objects.equal(this.commands, that.commands);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("commandsByType", commandsByType)
			.toString();
	}
	
}
