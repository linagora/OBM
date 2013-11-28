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

import org.obm.push.bean.SyncCollectionCommands.Response;
import org.obm.push.bean.change.SyncCommand;
import org.obm.push.bean.change.item.ItemChange;
import org.obm.push.bean.change.item.ItemDeletion;
import org.obm.push.exception.activesync.ASRequestIntegerFieldException;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class SyncCollectionResponse extends AbstractSyncCollection<SyncCollectionCommands.Response> implements Serializable {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private PIMDataType dataType;
		private SyncKey syncKey;
		private Integer collectionId;
		private SyncStatus status;
		private boolean moreAvailable;
		private SyncCollectionCommands.Response responses;

		private Builder() {
			super();
		}
		
		public Builder dataType(PIMDataType dataType) {
			this.dataType = dataType;
			return this;
		}
		
		public Builder syncKey(SyncKey syncKey) {
			this.syncKey = syncKey;
			return this;
		}
		
		public Builder collectionId(Integer collectionId) {
			this.collectionId = collectionId;
			return this;
		}
		
		public Builder status(SyncStatus status) {
			this.status = status;
			return this;
		}
		
		public Builder moreAvailable(boolean moreAvailable) {
			this.moreAvailable = moreAvailable;
			return this;
		}
		
		public Builder responses(SyncCollectionCommands.Response responses) {
			this.responses = responses;
			return this;
		}
		
		private void checkSyncCollectionCommonElements() {
			if (collectionId == null) {
				throw new ASRequestIntegerFieldException("Collection id field is required");
			}
		}
		
		public SyncCollectionResponse build() {
			checkSyncCollectionCommonElements();
			boolean moreAvailable = Objects.firstNonNull(this.moreAvailable, false);
			SyncKey syncKey = Objects.firstNonNull(this.syncKey, SyncKey.INITIAL_FOLDER_SYNC_KEY);
			return new SyncCollectionResponse(dataType, syncKey, collectionId, status, moreAvailable, responses);
		}

	}
	
	private final SyncStatus status;
	private final boolean moreAvailable;
	
	private SyncCollectionResponse(PIMDataType dataType, SyncKey syncKey, int collectionId,
			SyncStatus status, boolean moreAvailable, SyncCollectionCommands.Response responses) {
		super(dataType, syncKey, collectionId, responses);
		this.status = status;
		this.moreAvailable = moreAvailable;
	}
	
	public SyncStatus getStatus() {
		return status;
	}

	public boolean isMoreAvailable() {
		return moreAvailable;
	}

	public SyncCollectionCommands.Response getResponses() {
		return getCommands();
	}

	public List<ItemDeletion> getItemChangesDeletion() {
		Response commands = getCommands();
		if (commands != null) {
			return FluentIterable.from(
					commands.getCommandsForType(SyncCommand.DELETE))
					.transform(new Function<SyncCollectionCommand, ItemDeletion>() {
	
						@Override
						public ItemDeletion apply(SyncCollectionCommand input) {
							return ItemDeletion.builder().serverId(input.getServerId()).build();
						}
					}).toList();
		}
		return Lists.newArrayList();
	}
	
	public List<ItemChange> getItemFetchs() {
		Iterable<SyncCollectionCommand.Response> fetchs = getFetchs();
		if (fetchs != null) {
			return FluentIterable.from(fetchs)
					.transform(new Function<SyncCollectionCommand.Response, ItemChange>() {
	
						@Override
						public ItemChange apply(SyncCollectionCommand.Response fetch) {
							ItemChange itemChange = new ItemChange(fetch.getServerId());
							itemChange.setNew(false);
							itemChange.setData(fetch.getApplicationData());
							return itemChange;
						}
					}).toList();
		}
		return Lists.newArrayList();
	}

	private Iterable<SyncCollectionCommand.Response> getFetchs() {
		Response commands = getCommands();
		if (commands != null) {
			return commands.getCommandsForType(SyncCommand.FETCH);
		}
		return Lists.newArrayList();
	}

	public List<ItemChange> getItemChanges() {
		Iterable<SyncCollectionCommand.Response> changes = getChanges();
		if (changes != null) {
			return FluentIterable.from(changes)
					.transform(new Function<SyncCollectionCommand.Response, ItemChange>() {
	
						@Override
						public ItemChange apply(SyncCollectionCommand.Response change) {
							ItemChange itemChange = new ItemChange(change.getServerId());
							itemChange.setNew(SyncCommand.ADD.equals(change.getType()));
							itemChange.setData(change.getApplicationData());
							return itemChange;
						}
					}).toList();
		}
		return Lists.newArrayList();
	}

	private Iterable<SyncCollectionCommand.Response> getChanges() {
		Response commands = getCommands();
		if (commands != null) {
			return Iterables.concat(commands.getCommandsForType(SyncCommand.ADD),
					commands.getCommandsForType(SyncCommand.CHANGE),
					commands.getCommandsForType(SyncCommand.MODIFY));
		}
		return Lists.newArrayList();
	}

	@Override
	protected int hashCodeImpl() {
		return Objects.hashCode(status, moreAvailable);
	}
	
	@Override
	protected boolean equalsImpl(AbstractSyncCollection<?> object) {
		if (object instanceof SyncCollectionResponse) {
			SyncCollectionResponse that = (SyncCollectionResponse) object;
			return Objects.equal(this.status, that.status)
				&& Objects.equal(this.moreAvailable, that.moreAvailable);
		}
		return false;
	}


	@Override
	protected String toStringImpl() {
		return Objects.toStringHelper(this)
				.add("status", status)
				.add("moreAvailable", moreAvailable)
			.toString();
	}
	}
