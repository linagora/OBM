package org.obm.push.protocol.bean;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.SyncCollection;
import org.obm.push.protocol.data.EncoderFactory;

public class SyncResponse {

	public static class SyncCollectionResponse {
		private final SyncCollection syncCollection;
		private List<ItemChange> itemChanges;
		private List<ItemChange> itemChangesDeletion;
		private boolean syncStatevalid;
		private String allocateNewSyncKey;
		
		public SyncCollectionResponse(SyncCollection syncCollection) {
			this.syncCollection = syncCollection;
		}

		public SyncCollection getSyncCollection() {
			return syncCollection;
		}
		public void setSyncStateValid(boolean syncStateValid) {
			syncStatevalid = syncStateValid;
		}
		public boolean isSyncStatevalid() {
			return syncStatevalid;
		}
		public void setItemChanges(List<ItemChange> itemChanges) {
			this.itemChanges = itemChanges;
		}
		public List<ItemChange> getItemChanges() {
			return itemChanges;
		}
		public void setNewSyncKey(String allocateNewSyncKey) {
			this.allocateNewSyncKey = allocateNewSyncKey;
		}
		public String getAllocateNewSyncKey() {
			return allocateNewSyncKey;
		}
		public void setItemChangesDeletion(List<ItemChange> itemChangesDeletion) {
			this.itemChangesDeletion = itemChangesDeletion;
		}
		public List<ItemChange> getItemChangesDeletion() {
			return itemChangesDeletion;
		}
	}
	
	private final Collection<SyncCollectionResponse> collectionResponses;
	private final BackendSession backendSession;
	private final EncoderFactory encoderFactory;
	private final Map<String, String> processedClientIds;
	
	public SyncResponse(Collection<SyncCollectionResponse> collectionResponses, BackendSession bs, EncoderFactory encoderFactory, Map<String, String> processedClientIds) {
		this.collectionResponses = collectionResponses;
		this.backendSession = bs;
		this.encoderFactory = encoderFactory;
		this.processedClientIds = processedClientIds;
	}
	
	public SyncResponse() {
		this(null, null, null, null);
	}

	public BackendSession getBackendSession() {
		return backendSession;
	}
	
	public EncoderFactory getEncoderFactory() {
		return encoderFactory;
	}
	
	public Collection<SyncCollectionResponse> getCollectionResponses() {
		return collectionResponses;
	}
	
	public Map<String, String> getProcessedClientIds() {
		return processedClientIds;
	}
	
}
