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

import org.obm.push.bean.MSEmailBodyType;
import org.obm.push.bean.StoreName;

public class ItemOperationsRequest {

	public static class Fetch {
		
		private StoreName storeName;
		private String fileReference;
		private String collectionId;
		private String serverId;
		private MSEmailBodyType type;
		
		public Fetch() {
		}
		
		public StoreName getStoreName() {
			return storeName;
		}

		public void setStoreName(StoreName storeName) {
			this.storeName = storeName;
		}
		
		public String getFileReference() {
			return fileReference;
		}

		public void setFileReference(String fileReference) {
			this.fileReference = fileReference;
		}
		
		public String getCollectionId() {
			return collectionId;
		}
		
		public void setCollectionId(String collectionId) {
			this.collectionId = collectionId;
		}
		
		public String getServerId() {
			return serverId;
		}
		
		public void setServerId(String serverId) {
			this.serverId = serverId;
		}

		public MSEmailBodyType getType() {
			return type;
		}
		
		public void setType(MSEmailBodyType type) {
			this.type = type;
		}
		
	}
	
	public static class EmptyFolderContentsRequest {

		private Integer collectionId;
		private boolean deleteSubFolderElem;

		public Integer getCollectionId() {
			return collectionId;
		}
		
		public void setCollectionId(Integer collectionId) {
			this.collectionId = collectionId;
		}

		public boolean isDeleteSubFolderElem() {
			return deleteSubFolderElem;
		}
		
		public void setDeleteSubFolderElem(boolean deleteSubFolderElem) {
			this.deleteSubFolderElem = deleteSubFolderElem;
		}

	}
	
	private Fetch fetch;
	private EmptyFolderContentsRequest emptyFolderContents;
	
	public Fetch getFetch() {
		return fetch;
	}

	public void setFetch(Fetch fetch) {
		this.fetch = fetch;
	}
	
	public void setEmptyFolderContents(EmptyFolderContentsRequest emptyFolderContents) {
		this.emptyFolderContents = emptyFolderContents;
	}
	
	public EmptyFolderContentsRequest getEmptyFolderContents() {
		return emptyFolderContents;
	}
	
}
