package org.obm.push.backend;

import java.sql.SQLException;
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

	List<ItemChange> getChanged(BackendSession bs) throws SQLException;

	int getCount(BackendSession bs) throws SQLException;

	List<ItemChange> getDeleted(BackendSession bs);

	int getRootFolderId(BackendSession bs) throws ActiveSyncException, SQLException;

}
