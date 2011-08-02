package org.obm.push.store;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.obm.push.bean.InvitationStatus;

public interface FiltrageInvitationDao {

	
	Boolean isMostRecentInvitation(Integer eventCollectionId, String eventUid, Date dtStamp);
	
	boolean haveEmailToDeleted(Integer eventCollectionId, String eventUid);
	
	boolean haveEventToDeleted(Integer eventCollectionId, String eventUid);

	void createOrUpdateInvitationEventAsMustSynced(Integer eventCollectionId, String eventUid, Date dtStamp);
	
	void createOrUpdateInvitationEvent(Integer eventCollectionId, String uid, Date dtStamp, InvitationStatus status);
	
	void createOrUpdateInvitationEvent(Integer eventCollectionId, String eventUid, Date dtStamp, InvitationStatus status, String syncKey);
	
	void createOrUpdateInvitation(Integer eventCollectionId, String eventUid, Integer emailCollectionId, Long emailUid,
			Date dtStamp, InvitationStatus status, String syncKey);
	
	List<String> getInvitationEventMustSynced(Integer eventCollectionId);

	List<Long> getEmailToSynced(Integer emailCollectionId, String syncKey);

	List<Long> getEmailToDeleted(Integer emailCollectionId, String syncKey);

	List<String> getEventToSynced(Integer eventCollectionId, String syncKey);

	List<String> getEventToDeleted(Integer eventCollectionId, String syncKey);
	
	void updateInvitationStatus(InvitationStatus status, Integer emailCollectionId,  Collection<Long> emailUid);
	
	void updateInvitationStatus(InvitationStatus status, String syncKey, Integer emailCollectionId,  Collection<Long> emailUid);
	
	void updateInvitationEventStatus(InvitationStatus status,
			Integer eventCollectionId, Collection<String> eventUids);
	
	void updateInvitationEventStatus(InvitationStatus status, String syncKey,
			Integer eventCollectionId, Collection<String> eventUids);

	int getCountEmailFilterChanges(Integer emailCollectionId, String syncKey);
	
	int getCountEventFilterChanges(Integer eventCollectionId, String syncKey);
	
	boolean eventIsAlreadySynced(Integer eventCollectionId, String eventUid);

	boolean invitationIsAlreadySynced(Integer eventCollectionId, String eventUid);

	void setEventStatusAtToDelete(Integer eventCollectionId, String uid);

	void setInvitationStatusAtToDelete(Integer eventCollectionId,
			String eventUid);

	void removeInvitationStatus(Integer eventCollectionId,
			Integer emailCollectionId, Long email);

}
