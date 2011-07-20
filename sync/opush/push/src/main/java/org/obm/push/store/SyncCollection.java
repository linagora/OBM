package org.obm.push.store;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;


public class SyncCollection {
	
	private SyncState syncState;
	private List<String> fetchIds;
	private String dataClass;
	private Integer collectionId;
	private String collectionPath;
	private String syncKey;
	private Integer windowSize;
	private boolean moreAvailable;
	private Set<SyncCollectionChange> changes;
	private SyncStatus status;
	private PIMDataType dataType;
	private SyncCollectionOptions options;
	
	public SyncCollection() {
		fetchIds = new LinkedList<String>();
		collectionId = 0;
		moreAvailable = false;
		windowSize = 100;
		changes = new HashSet<SyncCollectionChange>();
		status = SyncStatus.OK;
		options = new SyncCollectionOptions();
	}

	public SyncState getSyncState() {
		return syncState;
	}

	public void setSyncState(SyncState syncState) {
		this.syncState = syncState;
	}

	public String getDataClass() {
		return dataClass;
	}

	public void setDataClass(String dataClass) {
		this.dataClass = dataClass;
	}

	public Integer getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(Integer collectionId) {
		this.collectionId = collectionId;
	}

	public String getSyncKey() {
		return syncKey;
	}

	public void setSyncKey(String syncKey) {
		this.syncKey = syncKey;
	}

	public List<String> getFetchIds() {
		return fetchIds;
	}

	public void setFetchIds(List<String> fetchIds) {
		this.fetchIds = fetchIds;
	}

	public Integer getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(Integer windowSize) {
		this.windowSize = windowSize;
	}
	
	public boolean isMoreAvailable() {
		return moreAvailable;
	}

	public void setMoreAvailable(boolean moreAvailable) {
		this.moreAvailable = moreAvailable;
	}
	
	public String getCollectionPath() {
		return collectionPath;
	}

	public void setCollectionPath(String collectionPath) {
		this.collectionPath = collectionPath;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SyncCollection other = (SyncCollection) obj;
		if (collectionId == null) {
			if (other.collectionId != null)
				return false;
		} else if (!collectionId.equals(other.collectionId))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(collectionId);
	}

	public Set<SyncCollectionChange> getChanges() {
		return changes;
	}

	public void addChange(SyncCollectionChange change) {
		this.changes.add(change);
	}
	
	public SyncStatus getStatus(){
		return status;
	}
	
	public void setError(SyncStatus status){
		this.status = status;
	}

	public PIMDataType getDataType() {
		return dataType;
	}

	public void setDataType(PIMDataType dataType) {
		this.dataType = dataType;
	}

	public SyncCollectionOptions getOptions() {
		return options;
	}

	public void setOptions(SyncCollectionOptions options) {
		this.options = options;
	}
}
