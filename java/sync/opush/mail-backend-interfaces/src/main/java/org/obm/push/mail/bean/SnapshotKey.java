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
package org.obm.push.mail.bean;

import java.io.Serializable;

import org.obm.push.bean.DeviceId;
import org.obm.push.bean.SyncKey;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

public class SnapshotKey implements Serializable{

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private SyncKey syncKey;
		private DeviceId deviceId;
		private Integer collectionId;
		
		private Builder() {}
		
		public Builder syncKey(SyncKey syncKey) {
			this.syncKey = syncKey;
			return this;
		}
		public Builder deviceId(DeviceId deviceId) {
			this.deviceId = deviceId;
			return this;
		}
		
		public Builder collectionId(Integer collectionId) {
			this.collectionId = collectionId;
			return this;
		}

		public SnapshotKey build() {
			Preconditions.checkArgument(syncKey != null, "syncKey can't be null or empty");
			Preconditions.checkArgument(deviceId != null, "deviceId can't be null or empty");
			Preconditions.checkArgument(collectionId != null, "collectionId can't be null or empty");
			return new SnapshotKey(syncKey, deviceId, collectionId);
		}
	}
	
	private static final long serialVersionUID = 1978530090812057347L;
	
	private final SyncKey syncKey;
	private final DeviceId deviceId;
	private final Integer collectionId;

	private SnapshotKey(SyncKey syncKey, DeviceId deviceId, Integer collectionId) {
		this.syncKey = syncKey;
		this.deviceId = deviceId;
		this.collectionId = collectionId;
	}

	public SyncKey getSyncKey() {
		return syncKey;
	}

	public DeviceId getDeviceId() {
		return deviceId;
	}

	public Integer getCollectionId() {
		return collectionId;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(syncKey, deviceId, collectionId);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SnapshotKey) {
			SnapshotKey that = (SnapshotKey) object;
			return Objects.equal(this.syncKey, that.syncKey)
				&& Objects.equal(this.deviceId, that.deviceId)
				&& Objects.equal(this.collectionId, that.collectionId);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("syncKey", syncKey)
			.add("deviceId", deviceId)
			.add("collectionId", collectionId)
			.toString();
	}
	
}
