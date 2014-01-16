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

import org.obm.push.bean.change.SyncCommand;
import org.w3c.dom.Element;

import com.google.common.base.Objects;

public abstract class SyncCollectionCommand implements Serializable {

	private static final long serialVersionUID = 5244279911428703760L;

	public static class Request extends SyncCollectionCommand {
		
		private static final long serialVersionUID = 6165838750984946199L;

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder extends SyncCollectionCommand.Builder<Request> {
			private Element data;
			
			private Builder() {
				super();
			}
			
			@Override
			protected Builder applicationDataImpl(Object data) {
				this.data = (Element) data;
				return this;
			}
			
			@Override
			protected Request buildImpl(SyncCommand commandType, String serverId, String clientId) {
				return new Request(commandType, serverId, clientId, data);
			}
		}
		
		private final Element data;
		
		private Request(SyncCommand commandType, String serverId, String clientId, Element data) {
			super(commandType, serverId, clientId);
			this.data = data;
		}
		
		public Element getApplicationData() {
			return data;
		}
	}
	
	public static class Response extends SyncCollectionCommand {
		
		private static final long serialVersionUID = -246587854210988404L;

		public static Builder builder() {
			return new Builder();
		}

		public static class Builder extends SyncCollectionCommand.Builder<Response> {
			private IApplicationData data;
			
			private Builder() {
				super();
			}
			
			@Override
			protected Builder applicationDataImpl(Object data) {
				this.data = (IApplicationData) data;
				return this;
			}
			
			@Override
			protected Response buildImpl(SyncCommand commandType, String serverId, String clientId) {
				return new Response(commandType, serverId, clientId, data);
			}
		}
		
		private final IApplicationData data;
		
		private Response(SyncCommand commandType, String serverId, String clientId, IApplicationData data) {
			super(commandType, serverId, clientId);
			this.data = data;
		}
		
		public IApplicationData getApplicationData() {
			return data;
		}
	}
	
	public abstract static class Builder<T extends SyncCollectionCommand> {
		
		private SyncCommand commandType;
		private String serverId;
		private String clientId;

		private Builder() {}
		
		public Builder<T> name(String commandType) {
			this.commandType = SyncCommand.fromSpecificationValue(commandType);
			return this;
		}
		
		public Builder<T> commandType(SyncCommand commandtype) {
			this.commandType = commandtype;
			return this;
		}
		
		public Builder<T> serverId(String serverId) {
			this.serverId = serverId;
			return this;
		}

		public Builder<T> clientId(String clientId) {
			this.clientId = clientId;
			return this;
		}

		public Builder<T> applicationData(Object data) {
			return applicationDataImpl(data);
		}
		
		protected abstract Builder<T> applicationDataImpl(Object data);
		
		public T build() {
			return buildImpl(commandType, serverId, clientId);
		}
		
		protected abstract T buildImpl(SyncCommand commandType, String serverId, String clientId);
	}
	
	private final SyncCommand type;
	private final String serverId;
	private final String clientId;
	
	private SyncCollectionCommand(SyncCommand type, String serverId, String clientId) {
		this.type = type;
		this.serverId = serverId;
		this.clientId = clientId;
	}

	public SyncCommand getType() {
		return type;
	}

	public String getServerId() {
		return serverId;
	}

	public String getClientId() {
		return clientId;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(type, serverId, clientId);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SyncCollectionCommand) {
			SyncCollectionCommand that = (SyncCollectionCommand) object;
			return Objects.equal(this.type, that.type)
				&& Objects.equal(this.serverId, that.serverId)
				&& Objects.equal(this.clientId, that.clientId);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("name", type)
			.add("serverId", serverId)
			.add("clientId", clientId)
 			.toString();
	}
}
