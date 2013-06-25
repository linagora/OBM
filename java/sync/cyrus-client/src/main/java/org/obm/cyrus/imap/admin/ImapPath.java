package org.obm.cyrus.imap.admin;

public interface ImapPath {

	interface Builder {
		
		Builder user(String user);
		
		Builder path(String path);
		
		ImapPath build();
		
	}
	
}
