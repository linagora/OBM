package org.obm.push.backend;

import java.util.List;

import org.obm.push.ItemChange;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.SyncState;

/**
 * The exporter API fetches data from the backend store and returns it to the
 * mobile device
 * 
 * @author tom
 * 
 */
public interface IHierarchyExporter {

	void configure(SyncState state, String dataClass, Integer filterType,
			int i, int j);
	
	String getRootFolderUrl(BackendSession bs);

//	SyncState getState(BackendSession bs);

	// void synchronize(BackendSession bs);

	List<ItemChange> getChanged(BackendSession bs);

	int getCount(BackendSession bs);

	List<ItemChange> getDeleted(BackendSession bs);

	int getRootFolderId(BackendSession bs) throws ActiveSyncException;

}
