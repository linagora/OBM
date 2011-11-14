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
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.AttachementNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;

/**
 * The exporter API fetches data from the backend store and returns it to the
 * mobile device
 */
public interface IContentsExporter {

	DataDelta getChanged(BackendSession bs, SyncState state, FilterType filterType, Integer collectionId, PIMDataType pimDataType) 
			throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException, ProcessingEmailException;

	List<ItemChange> fetch(BackendSession bs, PIMDataType getDataType,
			List<String> fetchIds) throws CollectionNotFoundException, DaoException, ProcessingEmailException, UnknownObmSyncServerException;
	
	List<ItemChange> fetchEmails(BackendSession bs,
			Integer collectionId, Collection<Long> uids) throws DaoException, CollectionNotFoundException, ProcessingEmailException;
	
	MSAttachementData getEmailAttachement(BackendSession bs,
			String attachmentName) throws AttachementNotFoundException, CollectionNotFoundException, DaoException, ProcessingEmailException;

	boolean getFilterChanges(BackendSession bs, SyncCollection collection) throws DaoException;

	int getItemEstimateSize(BackendSession bs, FilterType filterType, Integer collectionId, SyncState state, PIMDataType dataType) 
			throws CollectionNotFoundException, ProcessingEmailException, DaoException, UnknownObmSyncServerException;
	
}
