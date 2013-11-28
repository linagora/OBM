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

import java.util.Collection;
import java.util.Map;

import org.obm.push.bean.SyncCollectionCommand;
import org.obm.push.bean.SyncCollectionCommands.Response;
import org.obm.push.bean.SyncCollectionResponse;
import org.obm.push.bean.SyncStatus;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class SyncResponse {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private final ImmutableList.Builder<SyncCollectionResponse> responsesBuilder;
		private SyncStatus status;
		
		private Builder() {
			this.responsesBuilder = ImmutableList.builder();
		}
		
		public Builder addResponse(SyncCollectionResponse response) {
			responsesBuilder.add(response);
			return this;
		}
		
		public Builder status(SyncStatus status) {
			this.status = status;
			return this;
		}
		
		public SyncResponse build() {
			SyncStatus status = Objects.firstNonNull(this.status, SyncStatus.OK);
			ImmutableList<SyncCollectionResponse> responses = responsesBuilder.build();
			return new SyncResponse(responses, 
					buildProcessedClientIds(responses), status);
		}
		
		private Map<String, String> buildProcessedClientIds(ImmutableList<SyncCollectionResponse> responses) {
			ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
			for (SyncCollectionResponse response : responses) {
				Response commands = response.getCommands();
				if (commands != null) {
					for (SyncCollectionCommand.Response command : commands.getCommands()) {
						if (!Strings.isNullOrEmpty(command.getClientId())) {
							builder.put(command.getServerId(), command.getClientId());
						}
					}
				}
			}
			return builder.build();
		}
	}
	
	private final Collection<SyncCollectionResponse> collectionResponses;
	private final Map<String, String> processedClientIds;
	private final SyncStatus status;
	
	private SyncResponse(Collection<SyncCollectionResponse> collectionResponses, Map<String, String> processedClientIds,
			SyncStatus status) {
		this.collectionResponses = collectionResponses;
		this.processedClientIds = processedClientIds;
		this.status = status;
	}

	public Collection<SyncCollectionResponse> getCollectionResponses() {
		return collectionResponses;
	}
	
	public Map<String, String> getProcessedClientIds() {
		return processedClientIds;
	}

	public SyncStatus getStatus() {
		return status;
	}
	
}
