package org.obm.push.mail;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.obm.push.bean.Email;
import org.obm.push.utils.DateUtils;
import org.obm.push.utils.index.IndexUtils;

public class MailChanges {
	
	private final Set<Email> removed;
	private final Set<Email> updatedEmailsFromIMAP;
	private final Set<Email> updatedEmailsToDB;
	
	private Date lastSync;
	
	public MailChanges(Set<Email> removedEmails, Set<Email> updatedEmailsFromImap, Set<Email> updatedEmailsToDB) {
		this.removed = removedEmails;
		this.updatedEmailsFromIMAP = updatedEmailsFromImap;
		this.updatedEmailsToDB = updatedEmailsToDB;
		this.lastSync = DateUtils.getCurrentDate();
	}
	
	public Set<Email> getRemoved() {
		return removed;
	}

	public void addRemoved(Collection<Email> removed) {
		this.removed.addAll(removed);
	}

	public Set<Email> getUpdatedEmailFromImap() {
		return updatedEmailsFromIMAP;
	}

	public void addUpdated(Collection<Email> updated) {
		this.updatedEmailsFromIMAP.addAll(updated);
	}
	
	public void addUpdated(Email uid){
		this.updatedEmailsFromIMAP.add(uid);
	}

	public void addRemoved(Email uid){
		this.removed.add(uid);
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}
	
	public Collection<Long> getRemovedToLong() {
		return IndexUtils.listIndexes(getRemoved());
	}
	
	public Collection<Long> getUpdatedEmailFromImapToLong() {
		return IndexUtils.listIndexes(getUpdatedEmailFromImap());
	}
	
	public Set<Email> getUpdatedEmailToDB() {
		return updatedEmailsToDB;
	}
	
}
