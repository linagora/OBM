package org.obm.sync.items;

import java.util.List;
import java.util.Set;

import org.obm.sync.book.Folder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class FolderChanges {

	private List<Folder> updated;
	private Set<Integer> removed;

	public FolderChanges() {
		this(Lists.<Folder>newArrayList(), Sets.<Integer>newHashSet()); 
	}
	
	public FolderChanges(List<Folder> updated, Set<Integer> removed) {
		this.updated = updated;
		this.removed = removed;
	}
	
	public List<Folder> getUpdated() {
		return updated;
	}

	public void setUpdated(List<Folder> updated) {
		this.updated = updated;
	}

	public Set<Integer> getRemoved() {
		return removed;
	}

	public void setRemoved(Set<Integer> removed) {
		this.removed = removed;
	}

}
