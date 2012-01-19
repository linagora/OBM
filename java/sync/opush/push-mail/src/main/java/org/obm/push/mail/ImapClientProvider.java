package org.obm.push.mail;

import org.minig.imap.IdleClient;
import org.minig.imap.StoreClient;
import org.obm.locator.LocatorClientException;
import org.obm.push.bean.BackendSession;
import org.obm.push.exception.NoImapClientAvailableException;

import com.sun.mail.imap.IMAPStore;

public interface ImapClientProvider {

	String locateImap(BackendSession bs) throws LocatorClientException;
	StoreClient getImapClient(BackendSession bs) throws LocatorClientException;
	ImapStore getImapClientWithJM(BackendSession bs) throws LocatorClientException, NoImapClientAvailableException;
	IdleClient getImapIdleClient(BackendSession bs) throws LocatorClientException;
	
	IMAPStore getJavaxMailImapClient(BackendSession bs) throws MailException;
}