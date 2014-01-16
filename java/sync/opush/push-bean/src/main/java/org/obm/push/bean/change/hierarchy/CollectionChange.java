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
package org.obm.push.bean.change.hierarchy;

import java.io.Serializable;

import org.obm.push.bean.FolderType;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class CollectionChange implements Serializable {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {

		private String collectionId;
		private String parentCollectionId;
		private String displayName;
		private FolderType folderType;
		private Boolean isNew;
		
		private Builder() {
			super();
		}

		public Builder collectionId(String collectionId) {
			this.collectionId = collectionId;
			return this;
		}

		public Builder parentCollectionId(String parentCollectionId) {
			this.parentCollectionId = parentCollectionId;
			return this;
		}

		public Builder displayName(String displayName) {
			this.displayName = displayName;
			return this;
		}

		public Builder folderType(FolderType folderType) {
			this.folderType = folderType;
			return this;
		}

		public Builder isNew(boolean isNew) {
			this.isNew = isNew;
			return this;
		}
		
		public CollectionChange build() {
			Preconditions.checkArgument(!Strings.isNullOrEmpty(collectionId));
			Preconditions.checkArgument(!Strings.isNullOrEmpty(parentCollectionId));
			Preconditions.checkArgument(!Strings.isNullOrEmpty(displayName));
			Preconditions.checkNotNull(folderType);
			Preconditions.checkNotNull(isNew);
			
			return new CollectionChange(collectionId, parentCollectionId, displayName, folderType, isNew);
		}
	}
	
	private final String collectionId;
	private final String parentCollectionId;
	private final String displayName;
	private final FolderType folderType;
	private final boolean isNew;
	
	public CollectionChange(
			String collectionId, String parentCollectionId,
			String displayName, FolderType folderType, boolean isNew) {
		
		this.collectionId = collectionId;
		this.parentCollectionId = parentCollectionId;
		this.displayName = displayName;
		this.folderType = folderType;
		this.isNew = isNew;
	}
	
	public String getCollectionId() {
		return collectionId;
	}

	public String getParentCollectionId() {
		return parentCollectionId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public FolderType getFolderType() {
		return folderType;
	}

	public boolean isNew() {
		return isNew;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(collectionId, parentCollectionId, displayName, folderType, isNew);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof CollectionChange) {
			CollectionChange that = (CollectionChange) object;
			return Objects.equal(this.collectionId, that.collectionId)
				&& Objects.equal(this.parentCollectionId, that.parentCollectionId)
				&& Objects.equal(this.displayName, that.displayName)
				&& Objects.equal(this.folderType, that.folderType)
				&& Objects.equal(this.isNew, that.isNew);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("collectionId", collectionId)
			.add("collectionId", parentCollectionId)
			.add("displayName", displayName)
			.add("itemType", folderType)
			.add("isNew", isNew)
			.toString();
	}
}