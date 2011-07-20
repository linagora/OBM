package org.obm.push.store;

import java.util.HashMap;
import java.util.Map;

public class SyncCollectionOptions {
	
	private Integer truncation;
	private Integer mimeSupport;
	private Integer mimeTruncation;
	private Integer conflict;
	private Boolean deletesAsMoves;
	private FilterType filterType;
	private Map<MSEmailBodyType,BodyPreference> bodyPreferences;
	
	public SyncCollectionOptions() {
		conflict = 1;
		truncation = SyncHandler.SYNC_TRUNCATION_ALL;
		deletesAsMoves = true;
		this.bodyPreferences = new HashMap<MSEmailBodyType, BodyPreference>();
	}
	
	public Integer getConflict() {
		return conflict;
	}
	public void setConflict(Integer conflict) {
		this.conflict = conflict;
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

	public FilterType getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
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
}
