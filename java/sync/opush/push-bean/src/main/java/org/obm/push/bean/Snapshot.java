/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
import java.util.Collection;

import org.obm.push.mail.bean.Email;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
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
		private int uidNext;
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
		
		public Builder uidNext(int uidNext) {
			this.uidNext = uidNext;
			return this;
		}
		
		public Builder emails(Collection<Email> emails) {
			this.emails = emails;
			return this;
		}
		
		public Builder addEmail(Email email) {
			emails.add(email);
			return this;
		}
		
		public Snapshot build() {
			Preconditions.checkArgument(deviceId != null, "deviceId can't be null or empty");
			Preconditions.checkArgument(filterType != null, "filterType can't be null or empty");
			Preconditions.checkArgument(syncKey != null, "syncKey can't be null or empty");
			Preconditions.checkArgument(collectionId != null, "collectionId can't be null or empty");
			return new Snapshot(deviceId, filterType, syncKey, collectionId, uidNext, emails);
		}
	}
	
	private final DeviceId deviceId;
	private final FilterType filterType;
	private final SyncKey syncKey;
	private final Integer collectionId;
	private final int uidNext;
	private final Collection<Email> emails;
	
	private Snapshot(DeviceId deviceId, FilterType filterType, SyncKey syncKey, Integer collectionId, int uidNext, Collection<Email> emails) {
		this.deviceId = deviceId;
		this.filterType = filterType;
		this.syncKey = syncKey;
		this.collectionId = collectionId;
		this.uidNext = uidNext;
		this.emails = emails;
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

	public int getUidNext() {
		return uidNext;
	}

	public Collection<Email> getEmails() {
		return emails;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(deviceId, filterType, syncKey, collectionId, uidNext, emails);
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
				Objects.equal(this.emails, that.emails); 
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
			.toString();
	}
}
