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

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.obm.push.bean.DeviceId;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.ServerId;
import org.obm.push.bean.SyncKey;
import org.obm.push.exception.activesync.InvalidServerId;
import org.obm.push.exception.activesync.ProtocolException;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class Snapshot implements Serializable {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private DeviceId deviceId;
		private FilterType filterType;
		private SyncKey syncKey;
		private Integer collectionId;
		private long uidNext;
		private Collection<Email> emails;
		
		private Builder() {
			emails = Lists.newArrayList();
		}
		
		public Builder deviceId(DeviceId deviceId) {
			this.deviceId = deviceId;
			return this;
		}
		
		public Builder filterType(FilterType filterType) {
			this.filterType = filterType;
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
		
		public Builder uidNext(long uidNext) {
			this.uidNext = uidNext;
			return this;
		}
		
		public Builder emails(Collection<Email> emails) {
			this.emails = ImmutableList.copyOf(emails);
			return this;
		}
		
		public Builder addEmail(Email email) {
			emails.add(email);
			return this;
		}
		
		public Snapshot actualizeSnapshot(Snapshot snapshot, SyncKey newSynckKey) {
			return Snapshot.builder()
				.deviceId(snapshot.getDeviceId())
				.filterType(snapshot.getFilterType())
				.syncKey(newSynckKey)
				.collectionId(snapshot.getCollectionId())
				.uidNext(snapshot.getUidNext())
				.emails(snapshot.getEmails())
				.build();
		}
		
		public Snapshot build() {
			Preconditions.checkArgument(deviceId != null, "deviceId can't be null or empty");
			Preconditions.checkArgument(filterType != null, "filterType can't be null or empty");
			Preconditions.checkArgument(syncKey != null, "syncKey can't be null or empty");
			Preconditions.checkArgument(collectionId != null, "collectionId can't be null or empty");
			return new Snapshot(deviceId, filterType, syncKey, collectionId, uidNext, emails);
		}
	}
	
	private static final long serialVersionUID = -8674207692296869251L;
	
	private final DeviceId deviceId;
	private final FilterType filterType;
	private final SyncKey syncKey;
	private final Integer collectionId;
	private final long uidNext;
	private final Collection<Email> emails;
	private final MessageSet messageSet;
	
	protected Snapshot(DeviceId deviceId, FilterType filterType, SyncKey syncKey, Integer collectionId, long uidNext, Collection<Email> emails) {
		this.deviceId = deviceId;
		this.filterType = filterType;
		this.syncKey = syncKey;
		this.collectionId = collectionId;
		this.uidNext = uidNext;
		this.emails = emails;
		this.messageSet = generateMessageSet();
	}

	protected MessageSet generateMessageSet() {
		MessageSet.Builder builder = MessageSet.builder();
		for (Email email : emails) {
			builder.add(email.getUid());
		}
		return builder.build();
	}

	public boolean containsAllIds(List<String> serverIds) throws InvalidServerId {
		Preconditions.checkNotNull(serverIds);
		for (String serverId: serverIds) {
			Integer mailUid = new ServerId(serverId).getItemId();
			if (mailUid == null) {
				throw new ProtocolException(String.format("ServerId '%s' must reference an Item", serverId));
			}
			if (!messageSet.contains(mailUid)) {
				return false;
			}
		}
		return true;
	}

	
	public DeviceId getDeviceId() {
		return deviceId;
	}

	public FilterType getFilterType() {
		return filterType;
	}
	
	public SyncKey getSyncKey() {
		return syncKey;
	}

	public Integer getCollectionId() {
		return collectionId;
	}

	public long getUidNext() {
		return uidNext;
	}

	public Collection<Email> getEmails() {
		return emails;
	}

	public MessageSet getMessageSet() {
		return messageSet;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(deviceId, filterType, syncKey, collectionId, uidNext, emails, messageSet);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof Snapshot) {
			Snapshot that = (Snapshot) object;
			return Objects.equal(this.deviceId, that.deviceId) && 
				Objects.equal(this.filterType, that.filterType) && 
				Objects.equal(this.syncKey, that.syncKey) && 
				Objects.equal(this.collectionId, that.collectionId) && 
				Objects.equal(this.uidNext, that.uidNext) && 
				Objects.equal(this.emails, that.emails) &&  
				Objects.equal(this.messageSet, that.messageSet); 
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("deviceId", deviceId)
			.add("filterType", filterType)
			.add("syncKey", syncKey)
			.add("collectionId", collectionId)
			.add("uidNext", uidNext)
			.add("emails", emails)
			.add("messageSet", messageSet)
			.toString();
	}
}
