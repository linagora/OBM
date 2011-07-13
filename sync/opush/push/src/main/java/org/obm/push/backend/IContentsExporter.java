package org.obm.push.backend;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.obm.push.ItemChange;
import org.obm.push.exception.ObjectNotFoundException;
import org.obm.push.store.ActiveSyncException;
import org.obm.push.store.FilterType;
import org.obm.push.store.PIMDataType;
import org.obm.push.store.SyncCollection;
import org.obm.push.store.SyncState;

/**
 * The exporter API fetches data from the backend store and returns it to the
 * mobile device
 */
public interface IContentsExporter {

	DataDelta getChanged(BackendSession bs, SyncState state, FilterType filterType, Integer collectionId) 
			throws ActiveSyncException, SQLException;

	int getCount(BackendSession bs, SyncState state, FilterType filterType, Integer collectionId) 
			throws ActiveSyncException, SQLException;

	List<ItemChange> fetch(BackendSession bs, PIMDataType getDataType,
			List<String> fetchIds) throws ActiveSyncException;
	
	List<ItemChange> fetchEmails(BackendSession bs,
			Integer collectionId, Collection<Long> uids) throws ActiveSyncException;
	
	List<ItemChange> fetchCalendars(BackendSession bs,
			Integer collectionId, Collection<String> uids) throws ActiveSyncException;

	List<ItemChange> fetchCalendarDeletedItems(BackendSession bs,
			Integer collectionId, Collection<String> uids) throws ActiveSyncException;
	
	MSAttachementData getEmailAttachement(BackendSession bs,
			String attachmentName) throws ObjectNotFoundException;

	boolean validatePassword(String userID, String password);

	boolean getFilterChanges(BackendSession bs, SyncCollection collection);
	
}
