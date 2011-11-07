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

import com.google.common.base.Objects;

public class ItemChange implements Serializable {
	
	private String serverId;
	private String parentId;
	
	private String displayName;
	private FolderType itemType;
	private boolean isNew;
	
	private IApplicationData data;

	public ItemChange() {
		this(null, null, null, null, false);
	}
	
	public ItemChange(String serverId) {
		this(serverId, null, null, null, false);
	}

	public ItemChange(String serverId, String parentId, String displayName,
			FolderType itemType, boolean isNew) {
		super();
		this.serverId = serverId;
		this.parentId = parentId;
		this.displayName = displayName;
		this.itemType = itemType;
		this.isNew = isNew;
	}
	
	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public FolderType getItemType() {
		return itemType;
	}

	public void setItemType(FolderType itemType) {
		this.itemType = itemType;
	}

	public IApplicationData getData() {
		return data;
	}

	public void setData(IApplicationData data) {
		this.data = data;
	}

	public Boolean isNew() {
		return isNew;
	}

	public void setIsNew(Boolean isNew) {
		this.isNew = isNew;
	}

	public boolean isMSEmail() {
		if (getData() instanceof MSEmail) {
			return true;
		}
		return false;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(serverId, parentId);
	}

	@Override
	public final boolean equals(Object object){
		if (object instanceof ItemChange) {
			ItemChange that = (ItemChange) object;
			return Objects.equal(this.serverId, that.serverId)
				&& Objects.equal(this.parentId, that.parentId);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("serverId", serverId)
			.add("parentId", parentId)
			.add("displayName", displayName)
			.add("itemType", itemType)
			.add("isNew", isNew)
			.add("data", data)
			.toString();
	}

}