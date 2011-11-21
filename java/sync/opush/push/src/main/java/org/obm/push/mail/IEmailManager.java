package org.obm.push.mail;

import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.columba.ristretto.message.Address;
import org.minig.imap.IMAPException;
import org.obm.locator.LocatorClientException;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Email;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.SyncState;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.SendEmailException;
import org.obm.push.exception.SmtpInvalidRcptException;
import org.obm.push.exception.activesync.ProcessingEmailException;
import org.obm.push.exception.activesync.StoreEmailException;
import org.obm.sync.client.calendar.AbstractEventSyncClient;

public interface IEmailManager {

	MailChanges getSync(BackendSession bs, SyncState state, Integer devId, Integer collectionId, String collectionName)
			throws IMAPException, DaoException, LocatorClientException;

	List<MSEmail> fetchMails(BackendSession bs, AbstractEventSyncClient calendarClient, Integer collectionId, String collectionName, 
			Collection<Long> uids) throws IMAPException, LocatorClientException;

	void updateReadFlag(BackendSession bs, String collectionName, Long uid, boolean read) throws IMAPException, LocatorClientException;

	String parseMailBoxName(BackendSession bs, String collectionName) throws IMAPException, LocatorClientException;

	void delete(BackendSession bs, Integer devId, String collectionPath, Integer collectionId, Long uid) throws IMAPException, DaoException, LocatorClientException;

	Long moveItem(BackendSession bs, Integer devId, String srcFolder, Integer srcFolderId, String dstFolder, Integer dstFolderId, 
			Long uid) throws IMAPException, DaoException, LocatorClientException;

	List<InputStream> fetchMIMEMails(BackendSession bs, AbstractEventSyncClient calendarClient, String collectionName, 
			Set<Long> uids) throws IMAPException, LocatorClientException;

	void setAnsweredFlag(BackendSession bs, String collectionName, Long uid) throws IMAPException, LocatorClientException;

	void sendEmail(BackendSession bs, Address from, Set<Address> setTo, Set<Address> setCc, Set<Address> setCci, InputStream mimeMail,
			Boolean saveInSent) throws SendEmailException, ProcessingEmailException, SmtpInvalidRcptException, StoreEmailException;

	InputStream findAttachment(BackendSession bs, String collectionName, Long mailUid, String mimePartAddress) throws IMAPException, LocatorClientException;

	void purgeFolder(BackendSession bs, Integer devId, String collectionPath, Integer collectionId) throws IMAPException, DaoException, LocatorClientException;

	Long storeInInbox(BackendSession bs, InputStream mailContent, boolean isRead) throws StoreEmailException, LocatorClientException;

	boolean getLoginWithDomain();

	boolean getActivateTLS();
	
	String locateImap(BackendSession bs) throws LocatorClientException;

	void updateData(Integer devId, Integer collectionId, Date lastSync, Collection<Long> removedEmailsIds, Collection<Email> updated)
			throws DaoException;

}
