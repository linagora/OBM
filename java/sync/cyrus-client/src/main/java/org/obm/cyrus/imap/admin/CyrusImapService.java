package org.obm.cyrus.imap.admin;

public interface CyrusImapService {

	Connection login(String login, String password);
	
}
