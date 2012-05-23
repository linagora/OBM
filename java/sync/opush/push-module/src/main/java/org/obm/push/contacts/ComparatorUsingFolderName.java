package org.obm.push.contacts;

import java.util.Comparator;

import org.obm.sync.book.Folder;

public class ComparatorUsingFolderName implements Comparator<Folder> {

	private final String defaultFolderName;

	public ComparatorUsingFolderName(String defaultFolderName) {
		this.defaultFolderName = defaultFolderName;
	}
	
	@Override
	public int compare(Folder f1, Folder f2) {
		int stringCompareTo = f1.getName().compareTo(f2.getName());
		if (stringCompareTo == 0) {
			return 0;
		} else {
			if (f1.getName().equals(defaultFolderName)) {
				return -1;
			}
			if (f2.getName().equals(defaultFolderName)) {
				return 1;
			}
			if (stringCompareTo > 0) {
				return 1;
			}
			return -1;
		}
	}

}
