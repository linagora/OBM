package org.obm.push;

import org.obm.push.backend.DataDelta;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.sync.calendar.EventObmId;

public class DummyInvitationFilterManager implements IInvitationFilterManager {

	@Override
	public DataDelta filterEvent(BackendSession bs, SyncState state,
			Integer eventCollectionId, DataDelta delta) throws DaoException {
		return delta;
	}

	@Override
	public void createOrUpdateInvitation(BackendSession bs, SyncState state,
			Integer emailCollectionId, DataDelta delta) throws DaoException,
			ProcessingEmailException {
		return;
	}

	@Override
	public void handleMeetingResponse(BackendSession bs,
			Integer invitationCollectionId, MSEmail invitation)
			throws DaoException {
		return;
	}

	@Override
	public int getCountFilterChanges(BackendSession bs, String syncKey,
			PIMDataType dataType, Integer collectionId) throws DaoException {
		return 0;
	}

	@Override
	public void deleteFilteredEvent(Integer collectionId, EventObmId eventUid)
			throws DaoException {
		return;
	}

	@Override
	public void deleteFilteredEmail(Integer collectionId, Long mailUid)
			throws DaoException {
		return;
	}

	@Override
	public DataDelta filterInvitation(BackendSession bs, SyncState state,
			Integer emailCollectionId, DataDelta delta) throws DaoException,
			ProcessingEmailException, CollectionNotFoundException {
		return delta;
	}

	@Override
	public void removeInvitationStatus(Integer eventCollectionId,
			Integer emailCollectionId, Long mailUid)
			throws CollectionNotFoundException, ProcessingEmailException {
		return;
	}

}
