package org.obm.push.mail;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.obm.push.bean.Email;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.index.IndexUtils;

public class MailChanges {
	
	private final Set<Email> removed;
	private final Set<Email> newAndUpdatedEmails;
	
	private Date lastSync;
	
	public MailChanges(Set<Email> removedEmails, Set<Email> newAndUpdatedEmails) {
		this.removed = removedEmails;
		this.newAndUpdatedEmails = newAndUpdatedEmails;
		this.lastSync = DateUtils.getCurrentDate();
	}
	
	public Set<Email> getRemoved() {
		return removed;
	}

	public Set<Email> getNewAndUpdatedEmails() {
		return newAndUpdatedEmails;
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}
	
	public Collection<Long> getRemovedEmailsUids() {
		return IndexUtils.listIndexes(getRemoved());
	}
	
	public Collection<Long> getNewEmailsUids() {
		return IndexUtils.listIndexes(getNewAndUpdatedEmails());
	}
	
}
