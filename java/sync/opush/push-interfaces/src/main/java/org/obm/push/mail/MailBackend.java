package org.obm.push.mail;

import java.util.Collection;
import java.util.List;

import org.obm.push.backend.DataDelta;
import org.obm.push.backend.PIMBackend;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.ItemChange;
import org.obm.push.bean.MSAttachementData;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.AttachementNotFoundException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.exception.activesync.NotAllowedException;
import org.obm.push.exception.activesync.ProcessingEmailException;

public interface MailBackend extends PIMBackend {

	List<ItemChange> getHierarchyChanges(BackendSession bs) throws DaoException;

	DataDelta getMailChanges(BackendSession bs, SyncState state,
			Integer collectionId, FilterType filterType)
			throws ProcessingEmailException, CollectionNotFoundException;

	List<ItemChange> fetchItems(BackendSession bs, Integer collectionId,
			Collection<Long> uids) throws CollectionNotFoundException,
			ProcessingEmailException;

	void delete(BackendSession bs, String serverId, Boolean moveToTrash)
			throws CollectionNotFoundException, ProcessingEmailException;

	String createOrUpdate(BackendSession bs, Integer collectionId,
			String serverId, String clientId, MSEmail data)
			throws CollectionNotFoundException, ProcessingEmailException;

	String move(BackendSession bs, String srcFolder, String dstFolder,
			String messageId) throws CollectionNotFoundException,
			ProcessingEmailException;

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

	void purgeFolder(BackendSession bs, String collectionPath,
			boolean deleteSubFolder) throws NotAllowedException,
			CollectionNotFoundException, ProcessingEmailException;

	Long getEmailUidFromServerId(String serverId);

}