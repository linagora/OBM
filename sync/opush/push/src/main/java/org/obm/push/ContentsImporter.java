package org.obm.push;

import java.io.InputStream;
import java.sql.SQLException;

import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSContact;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.PIMDataType;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.exception.NotAllowedException;
import org.obm.push.exception.ProcessingEmailException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.ServerErrorException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.mail.MailBackend;

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
	public String importMessageChange(BackendSession bs, Integer collectionId,
			String serverId, String clientId, IApplicationData data)
			throws ActiveSyncException {
		String id = null;
		switch (data.getType()) {
		case CONTACTS:
			id = contactBackend.createOrUpdate(bs, collectionId, serverId, (MSContact) data);
			break;
		case EMAIL:
			id = mailBackend.createOrUpdate(bs, collectionId, serverId,
					clientId, (MSEmail) data);
			break;
		case TASKS:
		case CALENDAR:
			id = calBackend.createOrUpdate(bs, collectionId, serverId, data);
			break;
		case FOLDER:
			break;
		}
		return id;
	}

	@Override
	public void importMessageDeletion(BackendSession bs, PIMDataType type, 
			Integer collectionId, String serverId, Boolean moveToTrash) throws ActiveSyncException {
		switch (type) {
		case CALENDAR:
			String eventUid = calBackend.getEventUidFromServerId(serverId);
			calBackend.delete(bs, collectionId, serverId);
			if(eventUid != null){
				invitationFilterManager.deleteFilteredEvent(collectionId, eventUid);
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
		case FOLDER:
			break;
		}
	}

	public String importMoveItem(BackendSession bs, PIMDataType type,
			String srcFolder, String dstFolder, String messageId) throws ServerErrorException {
		switch (type) {
		case EMAIL:
			return mailBackend.move(bs, srcFolder, dstFolder, messageId);
		case CALENDAR:
		case CONTACTS:
		case TASKS:
		case FOLDER:
			break;
		}
		return null;
	}

	@Override
	public void sendEmail(BackendSession bs, InputStream mailContent,
			Boolean saveInSent)  throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException {
		mailBackend.sendEmail(bs, mailContent, saveInSent);
	}

	@Override
	public void replyEmail(BackendSession bs, InputStream mailContent,
			Boolean saveInSent, Integer collectionId, String serverId)  throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException {
		mailBackend.replyEmail(bs, mailContent, saveInSent, collectionId,
				serverId);
	}

	@Override
	public String importCalendarUserStatus(BackendSession bs,  Integer invitationCollexctionId, MSEmail invitation,
			AttendeeStatus userResponse) throws SQLException {
		String ret = calBackend.handleMeetingResponse(bs, invitation, userResponse);
		invitationFilterManager.handleMeetingResponse(bs, invitationCollexctionId, invitation);
		return ret;
	}

	@Override
	public void forwardEmail(BackendSession bs, InputStream mailContent,
			Boolean saveInSent, String collectionId, String serverId)  throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException {
		mailBackend.forwardEmail(bs, mailContent, saveInSent, collectionId,
				serverId);
	}

	@Override
	public void emptyFolderContent(BackendSession bs, String collectionPath,
			boolean deleteSubFolder) throws CollectionNotFoundException,
			NotAllowedException {
		if (collectionPath != null && collectionPath.contains("email\\")) {
			mailBackend.purgeFolder(bs, collectionPath, deleteSubFolder);
		} else {
			throw new NotAllowedException(
					"emptyFolderContent is only supported for emails, collection was "
							+ collectionPath);
		}

	}

}
