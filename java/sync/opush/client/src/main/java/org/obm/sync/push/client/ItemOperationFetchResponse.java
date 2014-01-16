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
package org.obm.sync.push.client;

import org.obm.push.bean.ItemOperationsStatus;
import org.w3c.dom.Element;

import com.google.common.base.Objects;

public final class ItemOperationFetchResponse {

	private final ItemOperationsStatus status;
	private final String serverId;
	private final Element data;

	public ItemOperationFetchResponse(ItemOperationsStatus status, String serverId, Element data) {
		this.status = status;
		this.serverId = serverId;
		this.data = data;
	}
	
	public ItemOperationsStatus getStatus() {
		return status;
	}

	public String getServerId() {
		return serverId;
	}

	public Element getData() {
		return data;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(status, serverId, data);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof ItemOperationFetchResponse) {
			ItemOperationFetchResponse that = (ItemOperationFetchResponse) object;
			return Objects.equal(status, that.status)
				&& Objects.equal(serverId, that.serverId)
				&& Objects.equal(data, that.data);
		}
		return false;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(this)
				.add("status", status)
				.add("serverId", serverId)
				.add("data", data)
				.toString();
	}
	
}
