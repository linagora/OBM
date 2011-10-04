package org.obm.push.bean;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import com.google.common.base.Objects;


public class SyncCollection implements Serializable {
	
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
		this(0, null);
	}

	public SyncCollection(int collectionId, String collectionPath) {
		super();
		this.collectionId = collectionId;
		this.collectionPath = collectionPath;
		fetchIds = new LinkedList<String>();
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

	@Override
	public final int hashCode(){
		return Objects.hashCode(syncState, fetchIds, dataClass, collectionId, collectionPath, 
				syncKey, windowSize, moreAvailable, changes, status, dataType, options);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SyncCollection) {
			SyncCollection that = (SyncCollection) object;
			return Objects.equal(this.syncState, that.syncState)
				&& Objects.equal(this.fetchIds, that.fetchIds)
				&& Objects.equal(this.dataClass, that.dataClass)
				&& Objects.equal(this.collectionId, that.collectionId)
				&& Objects.equal(this.collectionPath, that.collectionPath)
				&& Objects.equal(this.syncKey, that.syncKey)
				&& Objects.equal(this.windowSize, that.windowSize)
				&& Objects.equal(this.moreAvailable, that.moreAvailable)
				&& Objects.equal(this.changes, that.changes)
				&& Objects.equal(this.status, that.status)
				&& Objects.equal(this.dataType, that.dataType)
				&& Objects.equal(this.options, that.options);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("syncState", syncState)
			.add("fetchIds", fetchIds)
			.add("dataClass", dataClass)
			.add("collectionId", collectionId)
			.add("collectionPath", collectionPath)
			.add("syncKey", syncKey)
			.add("windowSize", windowSize)
			.add("moreAvailable", moreAvailable)
			.add("changes", changes)
			.add("status", status)
			.add("dataType", dataType)
			.add("options", options)
			.toString();
	}
	
}
