package org.obm.push.backend;

import java.io.InputStream;

import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.PIMDataType;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.ActiveSyncException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.SendEmailException;
import org.obm.push.exception.activesync.ServerErrorException;
import org.obm.push.exception.activesync.SmtpInvalidRcptException;

/**
 * Content management interface, ie. CRUD API.
 */
public interface IContentsImporter {

	String importMessageChange(BackendSession bs, Integer collectionId,
			String serverId, String clientId, IApplicationData data)
			throws ActiveSyncException, DaoException;

	void importMessageDeletion(BackendSession bs, PIMDataType type,
			Integer collectionId, String serverId, Boolean moveToTrash) throws ActiveSyncException, DaoException;

	String importMoveItem(BackendSession bs, PIMDataType type,
			String srcFolder, String dstFolder, String messageId)
			throws ServerErrorException;

	String importCalendarUserStatus(BackendSession bs, Integer invitationCollectionId, MSEmail invitation,
			AttendeeStatus userResponse) throws DaoException;

	void sendEmail(BackendSession bs, InputStream mailContent, Boolean saveInSent)
			throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException;

	void replyEmail(BackendSession bs, InputStream mailContent, Boolean saveInSent,
			Integer collectionId, String serverId) throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException;

	void forwardEmail(BackendSession bs, InputStream mailContent,
			Boolean saveInSent, String collectionId, String serverId)
			throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException;

	void emptyFolderContent(BackendSession bs, String collectionPath,
			boolean deleteSubFolder) throws CollectionNotFoundException,
			NotAllowedException;
}
