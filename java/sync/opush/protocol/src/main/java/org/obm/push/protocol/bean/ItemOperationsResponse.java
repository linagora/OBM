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

import org.obm.push.bean.ItemOperationsStatus;
import org.obm.push.bean.SyncCollectionResponse;
import org.obm.push.bean.change.item.ItemChange;

public class ItemOperationsResponse {

	public static class EmptyFolderContentsResult {
		
		private ItemOperationsStatus itemOperationsStatus;
		private int collectionId;
		
		public int getCollectionId() {
			return collectionId;
		}
		
		public void setCollectionId(int collectionId) {
			this.collectionId = collectionId;
		}
		
		public ItemOperationsStatus getItemOperationsStatus() {
			return itemOperationsStatus;
		}
		
		public void setItemOperationsStatus(ItemOperationsStatus itemOperationsStatus) {
			this.itemOperationsStatus = itemOperationsStatus;
		}
	}
	
	public static class MailboxFetchResult {

		private FetchAttachmentResult fileReferenceFetch;
		private FetchItemResult fetchItemResult;

		public void setFetchAttachmentResult(FetchAttachmentResult fileReferenceFetch) {
			this.fileReferenceFetch = fileReferenceFetch;
		}
		
		public FetchAttachmentResult getFileReferenceFetch() {
			return fileReferenceFetch;
		}

		public void setFetchItemResult(FetchItemResult fetchItemResult) {
			this.fetchItemResult = fetchItemResult;
		}

		public FetchItemResult getFetchItemResult() {
			return fetchItemResult;
		}
		
		public byte[] getAttachmentData() {
			if (fileReferenceFetch != null) {
				return fileReferenceFetch.attch;
			} else {
				return null;
			}
		}
		
		public static class FetchItemResult {
			private ItemChange itemChange;
			private ItemOperationsStatus status;
			private SyncCollectionResponse syncCollection;
			private String serverId;
			
			public ItemChange getItemChange() {
				return itemChange;
			}
			
			public void setItemChange(ItemChange itemChange) {
				this.itemChange = itemChange;
			}
			
			public ItemOperationsStatus getStatus() {
				return status;
			}
			
			public void setStatus(ItemOperationsStatus status) {
				this.status = status;
			}
			
			public SyncCollectionResponse getSyncCollection() {
				return syncCollection;
			}
			
			public void setSyncCollection(SyncCollectionResponse syncCollection) {
				this.syncCollection = syncCollection;
			}

			public String getServerId() {
				return serverId;
			}

			public void setServerId(String serverId) {
				this.serverId = serverId;
			}
			
			
			
		}

		
		public static class FetchAttachmentResult {
			
			private String reference;
			private ItemOperationsStatus status;
			private byte[] attch;
			private String contentType;

			public String getReference() {
				return reference;
			}
			
			public void setReference(String reference) {
				this.reference = reference;
			}
			
			public ItemOperationsStatus getStatus() {
				return status;
			}
			
			public void setStatus(ItemOperationsStatus status) {
				this.status = status;
			}
			
			public byte[] getAttch() {
				return attch;
			}
			
			public void setAttch(byte[] attch) {
				this.attch = attch;
			}
			
			public String getContentType() {
				return contentType;
			}
			
			public void setContentType(String contentType) {
				this.contentType = contentType;
			}
			
		}
		
	}
	
	private EmptyFolderContentsResult emptyFolderContentsResult;
	private MailboxFetchResult mailboxFetchResult;
	
	public MailboxFetchResult getMailboxFetchResult() {
		return mailboxFetchResult;
	}
	
	public void setMailboxFetchResult(MailboxFetchResult mailboxFetchResult) {
		this.mailboxFetchResult = mailboxFetchResult;
	}
	
	public EmptyFolderContentsResult getEmptyFolderContentsResult() {
		return emptyFolderContentsResult;
	}
	
	public void setEmptyFolderContentsResult(EmptyFolderContentsResult emptyFolderContentsResult) {
		this.emptyFolderContentsResult = emptyFolderContentsResult;
	}

	public byte[] getAttachmentData() {
		if (mailboxFetchResult != null) {
			return mailboxFetchResult.getAttachmentData();
		}
		return null;
	}

	public boolean hasFileReference() {
		if (mailboxFetchResult != null) {
			return mailboxFetchResult.getFileReferenceFetch() != null;
		}
		return false;
	}
	
}
