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
	
	private boolean multipart;
	private boolean gzip;
	private Fetch fetch;
	private EmptyFolderContentsRequest emptyFolderContents;
	
	public boolean isMultipart() {
		return multipart;
	}

	public void setMultipart(boolean multipart) {
		this.multipart = multipart;
	}
	
	public Fetch getFetch() {
		return fetch;
	}

	public void setFetch(Fetch fetch) {
		this.fetch = fetch;
	}
	
	public boolean isGzip() {
		return gzip;
	}
	
	public void setGzip(boolean gzip) {
		this.gzip = gzip;
	}
	
	public void setEmptyFolderContents(EmptyFolderContentsRequest emptyFolderContents) {
		this.emptyFolderContents = emptyFolderContents;
	}
	
	public EmptyFolderContentsRequest getEmptyFolderContents() {
		return emptyFolderContents;
	}
	
}
