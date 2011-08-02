package org.obm.push.store;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.obm.push.bean.InvitationStatus;
import org.obm.push.exception.DaoException;

public interface FiltrageInvitationDao {

	
	Boolean isMostRecentInvitation(Integer eventCollectionId, String eventUid, Date dtStamp) throws DaoException;
	
	boolean haveEmailToDeleted(Integer eventCollectionId, String eventUid) throws DaoException;
	
	boolean haveEventToDeleted(Integer eventCollectionId, String eventUid) throws DaoException;

	void createOrUpdateInvitationEventAsMustSynced(Integer eventCollectionId, String eventUid, Date dtStamp) throws DaoException;
	
	void createOrUpdateInvitationEvent(Integer eventCollectionId, String uid, Date dtStamp, InvitationStatus status) throws DaoException;
	
	void createOrUpdateInvitationEvent(Integer eventCollectionId, String eventUid, Date dtStamp, InvitationStatus status, String syncKey) throws DaoException;
	
	void createOrUpdateInvitation(Integer eventCollectionId, String eventUid, Integer emailCollectionId, Long emailUid,
			Date dtStamp, InvitationStatus status, String syncKey) throws DaoException;
	
	List<String> getInvitationEventMustSynced(Integer eventCollectionId) throws DaoException;

	List<Long> getEmailToSynced(Integer emailCollectionId, String syncKey) throws DaoException;

	List<Long> getEmailToDeleted(Integer emailCollectionId, String syncKey) throws DaoException;

	List<String> getEventToSynced(Integer eventCollectionId, String syncKey) throws DaoException;

	List<String> getEventToDeleted(Integer eventCollectionId, String syncKey) throws DaoException;
	
	void updateInvitationStatus(InvitationStatus status, Integer emailCollectionId,  Collection<Long> emailUid) throws DaoException;
	
	void updateInvitationStatus(InvitationStatus status, String syncKey, Integer emailCollectionId,  Collection<Long> emailUid) throws DaoException;
	
	void updateInvitationEventStatus(InvitationStatus status,
			Integer eventCollectionId, Collection<String> eventUids) throws DaoException;
	
	void updateInvitationEventStatus(InvitationStatus status, String syncKey,
			Integer eventCollectionId, Collection<String> eventUids) throws DaoException;

	int getCountEmailFilterChanges(Integer emailCollectionId, String syncKey) throws DaoException;
	
	int getCountEventFilterChanges(Integer eventCollectionId, String syncKey) throws DaoException;
	
	boolean eventIsAlreadySynced(Integer eventCollectionId, String eventUid) throws DaoException;

	boolean invitationIsAlreadySynced(Integer eventCollectionId, String eventUid) throws DaoException;

	void setEventStatusAtToDelete(Integer eventCollectionId, String uid) throws DaoException;

	void setInvitationStatusAtToDelete(Integer eventCollectionId,
			String eventUid) throws DaoException;

	void removeInvitationStatus(Integer eventCollectionId,
			Integer emailCollectionId, Long email) throws DaoException;

}
