package org.obm.push.store;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;


public interface ISyncStorage {

	void updateState(String devId, Integer collectionId, SyncState state);

	SyncState findStateForDevice(String devId, Integer collectionId);

	SyncState findStateForKey(String syncKey);

	long findLastHearbeat(String devId);

	void updateLastHearbeat(String devId, long hearbeat);

	/**
	 * Stores device informations for the given user. Returns <code>true</code>
	 * if the device is allowed to synchronize.
	 */
	boolean initDevice(String loginAtDomain, String deviceId, String deviceType);

	Integer addCollectionMapping(String deviceId, String collection);

	/**
	 * Fetches the id associated with a given collection id string.
	 */
	Integer getCollectionMapping(String deviceId, String collectionId)
			throws CollectionNotFoundException;

	String getCollectionPath(Integer collectionId)
			throws CollectionNotFoundException;

	PIMDataType getDataClass(String collectionId);

	void resetForFullSync(String devId);

	Integer getDevId(String deviceId);

	Set<Integer> getAllCollectionId(String devId);

	void resetCollection(String devId, Integer collectionId);

	/**
	 * Returns <code>true</code> if the device is authorized to synchronize.
	 */
	boolean syncAuthorized(String loginAtDomain, String deviceId);
	
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
	
	
	  /////////////////
	 //  Sync mail  //
	/////////////////
	void removeInvitationStatus(Integer eventCollectionId,  Integer emailCollectionId, Long email);
	
	void removeMessages(Integer devId, Integer collectionId,
			Collection<Long> mailUids) throws SQLException;
	
	void removeMessages(Integer devId, Integer collectionId, Date lastSync,
			Collection<Long> uids) throws SQLException;

	void addMessages(Integer devId, Integer collectionId,
			Collection<EmailCache> emails) throws SQLException;
	
	void addMessages(Integer devId, Integer collectionId, Date lastSync, Collection<EmailCache> messages) throws SQLException;
	
	Set<EmailCache> getSyncedMail(Integer devId, Integer collectionId);

	Set<Long> getDeletedMail(Integer devId, Integer collectionId, Date lastSync);

	Set<EmailCache> getUpdatedMail(Integer devId, Integer collectionId,
			Date updatedFrom);

	boolean eventIsAlreadySynced(Integer eventCollectionId, String eventUid);

	void setEventStatusAtToDelete(Integer eventCollectionId, String uid);

	boolean invitationIsAlreadySynced(Integer eventCollectionId, String eventUid);

	void setInvitationStatusAtToDelete(Integer eventCollectionId, String eventUid);

}
