package org.obm.push.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.columba.ristretto.message.Address;
import org.minig.imap.IMAPException;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.FilterType;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.ProcessingEmailException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.ServerErrorException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.StoreEmailException;
import org.obm.sync.client.calendar.AbstractEventSyncClient;

public interface IEmailManager {

	MailChanges getSync(BackendSession bs, SyncState state, Integer devId,
			Integer collectionId, String collectionName, FilterType filter)
			throws ServerErrorException;

	List<MSEmail> fetchMails(BackendSession bs, AbstractEventSyncClient calendarClient,
			Integer collectionId, String collectionName, Collection<Long> uids)
			throws IOException, IMAPException;

	void updateReadFlag(BackendSession bs, String collectionName, Long uid,
			boolean read) throws IMAPException;

	String parseMailBoxName(BackendSession bs, String collectionName)
			throws IMAPException;

	void delete(BackendSession bs, Integer devId, String collectionPath,
			Integer collectionId, Long uid) throws IMAPException;

	Long moveItem(BackendSession bs, Integer devId, String srcFolder,
			Integer srcFolderId, String dstFolder, Integer dstFolderId, Long uid)
			throws IMAPException;

	List<InputStream> fetchMIMEMails(BackendSession bs,
			AbstractEventSyncClient calendarClient, String collectionName, Set<Long> uids)
			throws IOException, IMAPException;

	void setAnsweredFlag(BackendSession bs, String collectionName, Long uid)
			throws IMAPException;

	void sendEmail(BackendSession bs, Address from, Set<Address> setTo,
			Set<Address> setCc, Set<Address> setCci, InputStream mimeMail,
			Boolean saveInSent) throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException;

	InputStream findAttachment(BackendSession bs, String collectionName,
			Long mailUid, String mimePartAddress) throws IMAPException;

	void purgeFolder(BackendSession bs, Integer devId, String collectionPath,
			Integer collectionId) throws IMAPException;

	Long storeInInbox(BackendSession bs, InputStream mailContent, boolean isRead)
			throws StoreEmailException;

	boolean getLoginWithDomain();

	boolean getActivateTLS();
	
	String locateImap(BackendSession bs);

}
