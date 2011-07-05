package org.obm.push.backend;

import org.obm.push.store.SyncState;

public interface IHierarchyImporter {

	ServerId importFolderChange(SyncFolder sf);
	
	ServerId importFolderDeletion(SyncFolder sf);

	void configure(SyncState state);
	
	
}
