package org.obm.sync.push.client;

import java.util.Map;

public class FolderSyncResponse implements IEasReponse {

	private FolderHierarchy fl;
	private String key;

	public FolderSyncResponse(String key, Map<FolderType, Folder> fl) {
		this.fl = new FolderHierarchy(fl);
		this.key = key;
	}
	
	@Override
	public String getReturnedSyncKey() {
		return key;
	}

	public FolderHierarchy getFolders() {
		return fl;
	}
	
}
