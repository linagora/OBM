package org.obm.push.backend;

import java.io.InputStream;
import java.sql.SQLException;

import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.PIMDataType;
import org.obm.push.exception.ActiveSyncException;
import org.obm.push.exception.CollectionNotFoundException;
import org.obm.push.exception.NotAllowedException;
import org.obm.push.exception.ProcessingEmailException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.ServerErrorException;
import org.obm.push.exception.SmtpInvalidRcptException;

/**
 * Content management interface, ie. CRUD API.
 */
public interface IContentsImporter {

	String importMessageChange(BackendSession bs, Integer collectionId,
			String serverId, String clientId, IApplicationData data)
			throws ActiveSyncException;

	void importMessageDeletion(BackendSession bs, PIMDataType type,
			Integer collectionId, String serverId, Boolean moveToTrash) throws ActiveSyncException;

	String importMoveItem(BackendSession bs, PIMDataType type,
			String srcFolder, String dstFolder, String messageId)
			throws ServerErrorException;

	String importCalendarUserStatus(BackendSession bs, Integer invitationCollectionId, MSEmail invitation,
			AttendeeStatus userResponse) throws SQLException;

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
