package org.obm.sync.items;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.sync.book.Folder;

public class FolderChangesResponse {

	private FolderChanges folderChanges;
	private Date lastSync;
	
	public void setFolderChanges(FolderChanges folderChanges) {
		this.folderChanges = folderChanges;
	}
	
	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}
	
	public Date getLastSync() {
		return lastSync;
	}

	public Set<Integer> getRemoved() {
		return folderChanges.getRemoved();
	}

	public List<Folder> getUpdated() {
		return folderChanges.getUpdated();
	}

	public void setRemoved(Set<Integer> removedIds) {
		folderChanges.setRemoved(removedIds);
	}

	public void setUpdated(List<Folder> updatedFolders) {
		folderChanges.setUpdated(updatedFolders);
		
	}

	public FolderChanges getFolderChanges() {
		return folderChanges;
	}
}
