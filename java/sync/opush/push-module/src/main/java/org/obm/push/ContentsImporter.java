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
package org.obm.push;

import org.obm.push.backend.IContentsImporter;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSContact;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.PIMDataType;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.UnknownObmSyncServerException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.ServerItemNotFoundException;
import org.obm.push.mail.MailBackend;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContentsImporter implements IContentsImporter {

	private final MailBackend mailBackend;
	private final CalendarBackend calBackend;
	private final ContactsBackend contactBackend;

	@Inject
	private ContentsImporter(MailBackend mailBackend,
			CalendarBackend calBackend, ContactsBackend contactBackend) {

		this.mailBackend = mailBackend;
		this.calBackend = calBackend;
		this.contactBackend = contactBackend;
	}

	@Override
	public String importMessageChange(BackendSession bs, Integer collectionId, String serverId, String clientId, IApplicationData data) 
			throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException, ProcessingEmailException, ServerItemNotFoundException {
		
		String resultServerId = null;
		switch (data.getType()) {
		case CONTACTS:
			resultServerId = contactBackend.createOrUpdate(bs, collectionId, serverId, (MSContact) data);
			break;
		case EMAIL:
			resultServerId = mailBackend.createOrUpdate(bs, collectionId, serverId,
					clientId, (MSEmail) data);
			break;
		case TASKS:
		case CALENDAR:
			resultServerId = calBackend.createOrUpdate(bs, collectionId, serverId, data);
			break;
		}
		return resultServerId;
	}

	@Override
	public String importMessageDeletion(BackendSession bs, PIMDataType type, Integer collectionId, String serverId, Boolean moveToTrash) 
					throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException, ProcessingEmailException, ServerItemNotFoundException {
		
		String serverIdDeleted = serverId;
		switch (type) {
		case CALENDAR:
			calBackend.delete(bs, collectionId, serverId);
			break;
		case CONTACTS:
			serverIdDeleted = contactBackend.delete(bs, serverId);
			break;
		case EMAIL:
			mailBackend.delete(bs, serverId,moveToTrash);
			break;
		case TASKS:
			calBackend.delete(bs, collectionId, serverId);
			break;
		}
		return serverIdDeleted;
	}

	public String importMoveItem(BackendSession bs, PIMDataType type,
			String srcFolder, String dstFolder, String messageId) throws CollectionNotFoundException, DaoException, ProcessingEmailException {
		switch (type) {
		case EMAIL:
			return mailBackend.move(bs, srcFolder, dstFolder, messageId);
		case CALENDAR:
		case CONTACTS:
		case TASKS:
			break;
		}
		return null;
	}

	@Override
	public void sendEmail(BackendSession bs, byte[] mailContent,
			Boolean saveInSent)  throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException {
		mailBackend.sendEmail(bs, mailContent, saveInSent);
	}

	@Override
	public void replyEmail(BackendSession bs, byte[] mailContent, Boolean saveInSent, Integer collectionId, String serverId)  
					throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException, CollectionNotFoundException, 
					DaoException, UnknownObmSyncServerException {
		mailBackend.replyEmail(bs, mailContent, saveInSent, collectionId, serverId);
	}

	@Override
	public String importCalendarUserStatus(BackendSession bs,  Integer invitationCollexctionId, MSEmail invitation,
			AttendeeStatus userResponse) throws DaoException, CollectionNotFoundException, UnknownObmSyncServerException, ServerItemNotFoundException {
		return calBackend.handleMeetingResponse(bs, invitation, userResponse);
	}

	@Override
	public void forwardEmail(BackendSession bs, byte[] mailContent, Boolean saveInSent, String collectionId, String serverId)  
			throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException, CollectionNotFoundException, 
			UnknownObmSyncServerException, DaoException {
		mailBackend.forwardEmail(bs, mailContent, saveInSent, collectionId, serverId);
	}

	@Override
	public void emptyFolderContent(BackendSession bs, String collectionPath, boolean deleteSubFolder) 
			throws CollectionNotFoundException, NotAllowedException, DaoException, ProcessingEmailException {
		
		if (collectionPath != null && collectionPath.contains("email\\")) {
			mailBackend.purgeFolder(bs, collectionPath, deleteSubFolder);
		} else {
			throw new NotAllowedException(
					"emptyFolderContent is only supported for emails, collection was "
							+ collectionPath);
		}
	}

}
