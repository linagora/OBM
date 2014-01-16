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

import org.w3c.dom.Element;

import com.google.common.base.Objects;

public class SyncCollectionRequestCommand {

	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		
		private String name;
		private String serverId;
		private String clientId;
		private Element data;

		private Builder() {}
		
		public Builder name(String name) {
			this.name = name;
			return this;
		}
		
		public Builder serverId(String serverId) {
			this.serverId = serverId;
			return this;
		}

		public Builder clientId(String clientId) {
			this.clientId = clientId;
			return this;
		}

		public Builder applicationData(Element data) {
			this.data = data;
			return this;
		}
		
		public SyncCollectionRequestCommand build() {
			return new SyncCollectionRequestCommand(name, serverId, clientId, data);
		}
	}
	
	private final String name;
	private final String serverId;
	private final String clientId;
	private final Element data;
	
	private SyncCollectionRequestCommand(String name, String serverId, String clientId, Element data) {
		this.name = name;
		this.serverId = serverId;
		this.clientId = clientId;
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public String getServerId() {
		return serverId;
	}

	public String getClientId() {
		return clientId;
	}

	public Element getApplicationData() {
		return data;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(name, serverId, clientId, data);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SyncCollectionRequestCommand) {
			SyncCollectionRequestCommand that = (SyncCollectionRequestCommand) object;
			return Objects.equal(this.name, that.name)
				&& Objects.equal(this.serverId, that.serverId)
				&& Objects.equal(this.clientId, that.clientId)
				&& Objects.equal(this.data, that.data);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("name", name)
			.add("serverId", serverId)
			.add("clientId", clientId)
			.add("data", data)
			.toString();
	}
}
