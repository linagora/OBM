package org.obm.cyrus.imap.admin;

import java.util.List;

public interface Connection {
	
	void createUserMailboxes(Partition partition, ImapPath... paths) 
			throws ImapOperationException, ConnectionException;
	
	List<Acl> getAcl(ImapPath path) throws ImapOperationException, ConnectionException;
	
	void setAcl(ImapPath path, Acl... acls) throws ImapOperationException, ConnectionException;
	
	List<ImapPath> listMailboxes(String user) throws ImapOperationException, ConnectionException;
	
	void delete(ImapPath path) throws ImapOperationException, ConnectionException;
	
	void rename(ImapPath source, ImapPath target, Partition partition) throws ImapOperationException, ConnectionException;
	
	Quota getQuota(ImapPath path);
	
	void setQuota(ImapPath path, Quota quota);
	
	void removeQuota(ImapPath path);
	
	void shutdown();
	
}
