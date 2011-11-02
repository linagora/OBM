/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.store;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.obm.push.bean.InvitationStatus;
import org.obm.push.exception.DaoException;
import org.obm.sync.calendar.EventObmId;

public interface FiltrageInvitationDao {

	
	Boolean isMostRecentInvitation(Integer eventCollectionId, EventObmId eventUid, Date dtStamp) throws DaoException;
	
	boolean haveEmailToDeleted(Integer eventCollectionId, EventObmId eventUid) throws DaoException;
	
	boolean haveEventToDeleted(Integer eventCollectionId, EventObmId eventUid) throws DaoException;

	void createOrUpdateInvitationEventAsMustSynced(Integer eventCollectionId, EventObmId eventUid, Date dtStamp) throws DaoException;
	
	void createOrUpdateInvitationEvent(Integer eventCollectionId, EventObmId uid, Date dtStamp, InvitationStatus status) throws DaoException;
	
	void createOrUpdateInvitationEvent(Integer eventCollectionId, EventObmId eventUid, Date dtStamp, InvitationStatus status, String syncKey) throws DaoException;
	
	void createOrUpdateInvitation(Integer eventCollectionId, EventObmId eventUid, Integer emailCollectionId, Long emailUid,
			Date dtStamp, InvitationStatus status, String syncKey) throws DaoException;
	
	List<EventObmId> getInvitationEventMustSynced(Integer eventCollectionId) throws DaoException;

	List<Long> getEmailToSynced(Integer emailCollectionId, String syncKey) throws DaoException;

	List<Long> getEmailToDeleted(Integer emailCollectionId, String syncKey) throws DaoException;

	List<EventObmId> getEventToSynced(Integer eventCollectionId, String syncKey) throws DaoException;

	List<EventObmId> getEventToDeleted(Integer eventCollectionId, String syncKey) throws DaoException;
	
	void updateInvitationStatus(InvitationStatus status, Integer emailCollectionId,  Collection<Long> emailUid) throws DaoException;
	
	void updateInvitationStatus(InvitationStatus status, String syncKey, Integer emailCollectionId,  Collection<Long> emailUid) throws DaoException;
	
	void updateInvitationEventStatus(InvitationStatus status,
			Integer eventCollectionId, Collection<EventObmId> eventUids) throws DaoException;
	
	void updateInvitationEventStatus(InvitationStatus status, String syncKey,
			Integer eventCollectionId, Collection<EventObmId> eventUids) throws DaoException;

	int getCountEmailFilterChanges(Integer emailCollectionId, String syncKey) throws DaoException;
	
	int getCountEventFilterChanges(Integer eventCollectionId, String syncKey) throws DaoException;
	
	boolean eventIsAlreadySynced(Integer eventCollectionId, EventObmId eventUid) throws DaoException;

	boolean invitationIsAlreadySynced(Integer eventCollectionId, EventObmId eventUid) throws DaoException;

	void setEventStatusAtToDelete(Integer eventCollectionId, EventObmId uid) throws DaoException;

	void setInvitationStatusAtToDelete(Integer eventCollectionId,
			EventObmId eventUid) throws DaoException;

	void removeInvitationStatus(Integer eventCollectionId,
			Integer emailCollectionId, Long email) throws DaoException;

}
