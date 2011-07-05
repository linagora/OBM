package org.obm.push.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Objects;


public class SyncCollection {
	
	private SyncState syncState;
	private List<String> fetchIds;
	private String dataClass;
	private Integer conflict;
	private Integer collectionId;
	private String collectionPath;
	private String syncKey;
	private Integer truncation;
	private Boolean deletesAsMoves;
	private FilterType filterType;
	private Integer windowSize;
	private boolean moreAvailable;
	private Integer mimeSupport;
	private Integer mimeTruncation;
	private Map<MSEmailBodyType,BodyPreference> bodyPreferences;
	private Set<SyncCollectionChange> changes;
	private SyncStatus status;
	private PIMDataType dataType;
	
	public SyncCollection() {
		fetchIds = new LinkedList<String>();
		conflict = 1;
		collectionId = 0;
		truncation = SyncHandler.SYNC_TRUNCATION_ALL;
		moreAvailable = false;
		windowSize = 100;
		deletesAsMoves = true;
		this.bodyPreferences = new HashMap<MSEmailBodyType, BodyPreference>();
		changes = new HashSet<SyncCollectionChange>();
		status = SyncStatus.OK;
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
	public Integer getConflict() {
		return conflict;
	}
	public void setConflict(Integer conflict) {
		this.conflict = conflict;
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

	public Integer getTruncation() {
		return truncation;
	}

	public void setTruncation(Integer truncation) {
		this.truncation = truncation;
	}

	public boolean isDeletesAsMoves() {
		return deletesAsMoves;
	}

	public void setDeletesAsMoves(Boolean deletesAsMoves) {
		if(deletesAsMoves != null){
			this.deletesAsMoves = deletesAsMoves;
		}
	}

	public List<String> getFetchIds() {
		return fetchIds;
	}

	public void setFetchIds(List<String> fetchIds) {
		this.fetchIds = fetchIds;
	}

	public FilterType getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
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

	public Integer getMimeSupport() {
		return mimeSupport;
	}

	public void setMimeSupport(Integer mimeSupport) {
		this.mimeSupport = mimeSupport;
	}

	public Integer getMimeTruncation() {
		return mimeTruncation;
	}

	public void setMimeTruncation(Integer mimeTruncation) {
		this.mimeTruncation = mimeTruncation;
	}
	
	public Map<MSEmailBodyType,BodyPreference> getBodyPreferences() {
		return bodyPreferences;
	}
	
	public BodyPreference getBodyPreference(MSEmailBodyType type) {
		return bodyPreferences.get(type);
	}

	public void addBodyPreference(BodyPreference bodyPreference) {
		this.bodyPreferences.put(bodyPreference.getType(), bodyPreference);
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
}
