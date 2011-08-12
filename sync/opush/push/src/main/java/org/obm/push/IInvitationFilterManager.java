package org.obm.push;

import org.obm.push.backend.DataDelta;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.ProcessingEmailException;

public interface IInvitationFilterManager {

	DataDelta filterEvent(BackendSession bs, SyncState state, Integer eventCollectionId, DataDelta delta) throws DaoException;

	void filterInvitation(BackendSession bs, SyncState state, Integer emailCollectionId, DataDelta delta) throws DaoException, ProcessingEmailException;

	void handleMeetingResponse(BackendSession bs, Integer invitationCollexctionId, MSEmail invitation) throws DaoException;

	int getCountFilterChanges(BackendSession bs, String syncKey, PIMDataType dataType, Integer collectionId) throws DaoException;

	void deleteFilteredEvent(Integer collectionId, String eventUid) throws DaoException;

	void deleteFilteredEmail(Integer collectionId, Long mailUid) throws DaoException;

}
