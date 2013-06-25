package org.obm.cyrus.imap.admin;

import java.util.Set;

public interface Acl {

	enum Rights {
		Lookup, Read, PersistSeenStatus, Write, Insert, Post, Create, DeleteMailbox, DeleteMessage, PerformExpunge, Administer}
	
	String getUser();
	Set<Rights> getRights();
	
}
