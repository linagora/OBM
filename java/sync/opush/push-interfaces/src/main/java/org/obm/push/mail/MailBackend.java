package org.obm.push.mail;

import java.util.List;

import org.obm.push.backend.PIMBackend;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.MSEmail;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.AttachementNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.ProcessingEmailException;

public interface MailBackend extends PIMBackend {

	List<ItemChange> getHierarchyChanges(BackendSession bs) throws DaoException;

	void sendEmail(BackendSession bs, byte[] mailContent, Boolean saveInSent)
			throws ProcessingEmailException;

	void replyEmail(BackendSession bs, byte[] mailContent, Boolean saveInSent,
			Integer collectionId, String serverId)
			throws ProcessingEmailException, CollectionNotFoundException;

	void forwardEmail(BackendSession bs, byte[] mailContent,
			Boolean saveInSent, String collectionId, String serverId)
			throws ProcessingEmailException, CollectionNotFoundException;

	MSEmail getEmail(BackendSession bs, Integer collectionId, String serverId)
			throws CollectionNotFoundException, ProcessingEmailException;

	MSAttachementData getAttachment(BackendSession bs, String attachmentId)
			throws AttachementNotFoundException, CollectionNotFoundException,
			ProcessingEmailException;

	Long getEmailUidFromServerId(String serverId);

}