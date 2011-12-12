package org.obm.push.protocol.bean;

import org.obm.push.bean.ItemChange;
import org.obm.push.bean.ItemOperationsStatus;
import org.obm.push.bean.SyncCollection;

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
		
		public static class FetchItemResult {
			private ItemChange itemChange;
			private ItemOperationsStatus status;
			private SyncCollection syncCollection;
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
			
			public SyncCollection getSyncCollection() {
				return syncCollection;
			}
			
			public void setSyncCollection(SyncCollection syncCollection) {
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
	private boolean multipart;
	private boolean gzip;
	
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

	public boolean isMultipart() {
		return multipart;
	}
	
	public void setMultipart(boolean multipart) {
		this.multipart = multipart;
	}

	public boolean isGzip() {
		return gzip;
	}
	
	public void setGzip(boolean gzip) {
		this.gzip = gzip;
	}
	
}
