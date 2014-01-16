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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

public abstract class AbstractSyncCollection<T extends SyncCollectionCommands<?>> implements Serializable {
	
	private final PIMDataType dataType;
	private final SyncKey syncKey;
	private final int collectionId;
	private final T commands;
	
	protected AbstractSyncCollection(PIMDataType dataType, SyncKey syncKey, int collectionId, T commands) {
		this.dataType = dataType;
		this.syncKey = syncKey;
		this.collectionId = collectionId;
		this.commands = commands;
	}
	
	public PIMDataType getDataType() {
		return dataType;
	}
	
	public String getDataClass() {
		if (dataType != null && dataType != PIMDataType.UNKNOWN) {
			return dataType.asXmlValue();
		}
		return null;
	}

	public int getCollectionId() {
		return collectionId;
	}

	public SyncKey getSyncKey() {
		return syncKey;
	}
	
	public T getCommands() {
		return commands;
	}

	public List<String> getFetchIds() {
		if (commands == null) {
			return ImmutableList.of();
		}
		return FluentIterable.from(
				commands.getCommandsForType(SyncCommand.FETCH))
				.transform(new Function<SyncCollectionCommand, String>() {
					@Override
					public String apply(SyncCollectionCommand input) {
						return input.getServerId();
					}
				}).toList();
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(dataType, syncKey, collectionId, commands, hashCodeImpl());
	}
	
	protected abstract int hashCodeImpl();

	@Override
	public final boolean equals(Object object){
		if (object instanceof AbstractSyncCollection<?>) {
			AbstractSyncCollection<?> that = (AbstractSyncCollection<?>) object;
			return Objects.equal(this.dataType, that.dataType)
				&& Objects.equal(this.syncKey, that.syncKey)
				&& Objects.equal(this.collectionId, that.collectionId)
				&& Objects.equal(this.commands, that.commands)
				&& equalsImpl(that);
		}
		return false;
	}

	protected abstract boolean equalsImpl(AbstractSyncCollection<?> object);

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("dataType", dataType)
			.add("syncKey", syncKey)
			.add("collectionId", collectionId)
			.add("commands", commands)
			.add("syncKey", syncKey)
			.toString()
			.concat(toStringImpl());
	}

	protected abstract String toStringImpl();
}
