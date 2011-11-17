package org.obm.push.backend;

import java.util.TreeSet;

import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.obm.push.contacts.ComparatorUsingFolderName;
import org.obm.sync.book.Folder;

import com.google.common.collect.ImmutableList;

public class ContactsBackendTest {

	@Test
	public void sortedByDefaultFolderName() {
		final String defaultFolderName = "contacts";
		
		Folder f1 = createFolder("users", -1);
		Folder f2 = createFolder("collected_contacts", 2);
		Folder f3 = createFolder(defaultFolderName, 3);
		Folder f4 = createFolder("my address book", 4);
		
		ImmutableList<Folder> immutableList = ImmutableList.of(f1, f2, f3, f4);
		TreeSet<Folder> treeset = new TreeSet<Folder>(
				new ComparatorUsingFolderName(defaultFolderName));
		treeset.addAll(immutableList);
		
		Assert.assertNotNull(treeset);
		Assertions.assertThat(treeset).hasSize(4);
		Assertions.assertThat(treeset).contains(immutableList.toArray());
		Assertions.assertThat(treeset.first().getName()).isEqualTo(defaultFolderName);
		Assertions.assertThat(treeset.last().getName()).isEqualTo("users");
	}

	private Folder createFolder(String name, int uid) {
		Folder folder = new Folder();
		folder.setName(name);
		folder.setUid(uid);
		return folder;
	}
	
}
