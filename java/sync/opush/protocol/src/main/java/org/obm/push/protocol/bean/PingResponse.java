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
package org.obm.push.protocol.bean;

import java.util.Set;

import org.obm.push.bean.PingStatus;
import org.obm.push.bean.SyncCollectionResponse;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

public class PingResponse {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private PingStatus pingStatus;
		private final ImmutableSet.Builder<SyncCollectionResponse> syncCollectionBuilder;
		
		private Builder() {
			super();
			syncCollectionBuilder = ImmutableSet.builder();
		}
		
		public Builder pingStatus(PingStatus pingStatus) {
			this.pingStatus = pingStatus;
			return this;
		}
		
		public Builder syncCollections(Set<SyncCollectionResponse> syncCollections) {
			if (syncCollections != null) {
				syncCollectionBuilder.addAll(syncCollections);
			}
			return this;
		}
		
		public Builder add(SyncCollectionResponse syncCollectionResponse) {
			syncCollectionBuilder.add(syncCollectionResponse);
			return this;
		}
		
		public PingResponse build() {
			return new PingResponse(
					this.pingStatus, syncCollectionBuilder.build());
		}
	}
	
	private final PingStatus pingStatus;
	private final Set<SyncCollectionResponse> syncCollections;
	
	private PingResponse( PingStatus pingStatus, Set<SyncCollectionResponse> syncCollections) {
		this.syncCollections = syncCollections;
		this.pingStatus = pingStatus;
	}

	public Set<SyncCollectionResponse> getSyncCollections() {
		return syncCollections;
	}
	
	public PingStatus getPingStatus() {
		return pingStatus;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(pingStatus, syncCollections);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof PingResponse) {
			PingResponse that = (PingResponse) object;
			return Objects.equal(this.pingStatus, that.pingStatus)
				&& Objects.equal(this.syncCollections, that.syncCollections);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("pingStatus", pingStatus)
			.add("syncCollections", syncCollections)
			.toString();
	}
}
