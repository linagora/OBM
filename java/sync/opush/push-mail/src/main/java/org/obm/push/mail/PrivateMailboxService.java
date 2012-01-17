package org.obm.push.mail;

import org.minig.imap.MailboxFolder;
import org.minig.imap.MailboxFolders;
import org.obm.push.bean.BackendSession;

public interface PrivateMailboxService {

	MailboxFolders listAllFolders(BackendSession bs) throws MailException;
	
	boolean createFolder(BackendSession bs, MailboxFolder folder) throws MailException;
}
