package org.obm.sync.items;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.sync.book.Contact;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class ContactChanges {
	
	private final List<Contact> updated;
	private final Set<Integer> removed;
	private final Date lastSync;
	
	public ContactChanges() {
		this(Lists.<Contact>newArrayList(), Sets.<Integer>newHashSet(), null);
	}
	
	public ContactChanges(List<Contact> updated, Set<Integer> removed, Date lastSync) {
		this.updated = updated;
		this.removed = removed;
		this.lastSync = lastSync;
	}

	public List<Contact> getUpdated() {
		return updated;
	}
	
	public Set<Integer> getRemoved() {
		return removed;
	}

	public Date getLastSync() {
		return lastSync;
	}
	
}