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

import com.google.common.base.Objects;

/*
 * This legacy class mustn't be used.
 * It's only in use for cache deserialization comptability.
 */
@Deprecated
public class SyncCollectionChange implements Serializable {

	private static final long serialVersionUID = -3115614124120167390L;
	
	private final String serverId;
	private final String clientId;
	private final SyncCommand command;
	private final PIMDataType type;
	private final IApplicationData data;
	
	public SyncCollectionChange(String serverId, String clientId,
			SyncCommand command, IApplicationData data, PIMDataType type) {
		super();
		this.serverId = serverId;
		this.clientId = clientId;
		this.command = command;
		this.data = data;
		this.type = type;
	}

	public String getServerId() {
		return serverId;
	}

	public String getClientId() {
		return clientId;
	}

	public SyncCommand getCommand() {
		return command;
	}

	public IApplicationData getData() {
		return data;
	}

	public PIMDataType getType() {
		return type;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(serverId, clientId, command, type, data);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SyncCollectionChange) {
			SyncCollectionChange that = (SyncCollectionChange) object;
			return Objects.equal(this.serverId, that.serverId)
				&& Objects.equal(this.clientId, that.clientId)
				&& Objects.equal(this.command, that.command)
				&& Objects.equal(this.type, that.type)
				&& Objects.equal(this.data, that.data);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("serverId", serverId)
			.add("clientId", clientId)
			.add("command", command)
			.add("type", type)
			.add("data", data)
			.toString();
	}
	
}
