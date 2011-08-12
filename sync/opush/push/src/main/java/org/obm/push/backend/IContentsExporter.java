package org.obm.push.backend;

import java.util.Collection;
import java.util.List;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncCollection;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.ActiveSyncException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ObjectNotFoundException;

/**
 * The exporter API fetches data from the backend store and returns it to the
 * mobile device
 */
public interface IContentsExporter {

	DataDelta getChanged(BackendSession bs, SyncState state, FilterType filterType, Integer collectionId) 
			throws DaoException, CollectionNotFoundException;

	int getCount(BackendSession bs, SyncState state, FilterType filterType, Integer collectionId) 
			throws ActiveSyncException, DaoException;

	List<ItemChange> fetch(BackendSession bs, PIMDataType getDataType,
			List<String> fetchIds) throws ObjectNotFoundException, CollectionNotFoundException, DaoException ;
	
	List<ItemChange> fetchEmails(BackendSession bs,
			Integer collectionId, Collection<Long> uids) throws ActiveSyncException, DaoException;
	
	List<ItemChange> fetchCalendars(BackendSession bs,
			Integer collectionId, Collection<String> uids) throws ActiveSyncException;

	List<ItemChange> fetchCalendarDeletedItems(BackendSession bs,
			Integer collectionId, Collection<String> uids) throws ActiveSyncException;
	
	MSAttachementData getEmailAttachement(BackendSession bs,
			String attachmentName) throws ObjectNotFoundException;

	boolean validatePassword(String userID, String password);

	boolean getFilterChanges(BackendSession bs, SyncCollection collection) throws DaoException;
	
}
