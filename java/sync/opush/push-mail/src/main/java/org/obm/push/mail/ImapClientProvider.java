package org.obm.push.mail;

import org.minig.imap.IdleClient;
import org.minig.imap.StoreClient;
import org.obm.locator.LocatorClientException;
import org.obm.push.bean.BackendSession;

public interface ImapClientProvider {

	String locateImap(BackendSession bs) throws LocatorClientException;
	StoreClient getImapClient(BackendSession bs) throws LocatorClientException;
	IdleClient getImapIdleClient(BackendSession bs) throws LocatorClientException;
}