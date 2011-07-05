package org.obm.push;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.DataDelta;
import org.obm.push.backend.MSEmail;
import org.obm.push.store.PIMDataType;
import org.obm.push.store.SyncState;

public interface IInvitationFilterManager {

	DataDelta filterEvent(BackendSession bs, SyncState state, Integer eventCollectionId, DataDelta delta);
	void filterInvitation(BackendSession bs, SyncState state, Integer emailCollectionId, DataDelta delta);
	void handleMeetingResponse(BackendSession bs, Integer invitationCollexctionId, MSEmail invitation);
	int getCountFilterChanges(BackendSession bs, String syncKey, PIMDataType dataType, Integer collectionId);
	void deleteFilteredEvent(Integer collectionId, String eventUid);
	void deleteFilteredEmail(Integer collectionId, Long mailUid);

}
