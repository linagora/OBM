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

import java.util.Date;
import java.util.LinkedList;

import org.obm.sync.book.Contact;

public class ContactUpdates extends LinkedList<Contact> {

	private static final long serialVersionUID = -61848546135095989L;
	private Date lastSync;

	private LinkedList<Integer> archived;

	public ContactUpdates() {
		archived = new LinkedList<Integer>();
	}

	public Date getLastSync() {
		return lastSync;
	}

	public void setLastSync(Date lastSync) {
		this.lastSync = lastSync;
	}

	public LinkedList<Integer> getArchived() {
		return archived;
	}

	public void addArchived(Contact c) {
		archived.add(c.getUid());
	}

}
