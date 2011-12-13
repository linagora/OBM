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
import org.obm.sync.calendar.Event;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ContentsImporter implements IContentsImporter {

	private final MailBackend mailBackend;
	private final CalendarBackend calBackend;
	private final ContactsBackend contactBackend;
	private final IInvitationFilterManager invitationFilterManager;

	@Inject
	private ContentsImporter(MailBackend mailBackend,
			CalendarBackend calBackend, ContactsBackend contactBackend,
			IInvitationFilterManager invitationFilterManager) {

		this.mailBackend = mailBackend;
		this.calBackend = calBackend;
		this.contactBackend = contactBackend;
		this.invitationFilterManager = invitationFilterManager;
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
	public void importMessageDeletion(BackendSession bs, PIMDataType type, Integer collectionId, String serverId, Boolean moveToTrash) 
					throws CollectionNotFoundException, DaoException, UnknownObmSyncServerException, ProcessingEmailException, ServerItemNotFoundException {
		
		switch (type) {
		case CALENDAR:
			Event event = calBackend.getEventFromServerId(bs, serverId);
			calBackend.delete(bs, collectionId, serverId);
			if (event.getObmId() != null) {
				invitationFilterManager.deleteFilteredEvent(collectionId, event.getObmId());
			}
			break;
		case CONTACTS:
			contactBackend.delete(bs, serverId);
			break;
		case EMAIL:
			Long emailUid = mailBackend.getEmailUidFromServerId(serverId);
			mailBackend.delete(bs, serverId,moveToTrash);
			if(emailUid != null){
				invitationFilterManager.deleteFilteredEmail(collectionId, emailUid);
			}
			break;
		case TASKS:
			calBackend.delete(bs, collectionId, serverId);
			break;
		}
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
		
		String serverId = calBackend.handleMeetingResponse(bs, invitation, userResponse);
		invitationFilterManager.handleMeetingResponse(bs, invitationCollexctionId, invitation);
		return serverId;
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
