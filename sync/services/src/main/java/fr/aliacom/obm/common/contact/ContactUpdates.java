/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.common.contact;

import java.util.List;
import java.util.Set;

import org.obm.sync.book.Contact;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class ContactUpdates {

	private Set<Integer> archived;
	private List<Contact> contacts;
	
	public ContactUpdates() {
		archived = ImmutableSet.of();
		contacts = ImmutableList.of();
	}
	
	public Set<Integer> getArchived() {
		return archived;
	}

	public void setArchived(Set<Integer> archivedContactIds) {
		this.archived = archivedContactIds;
	}
	
	public List<Contact> getContacts() {
		return contacts;
	}
	
	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}
	
}
