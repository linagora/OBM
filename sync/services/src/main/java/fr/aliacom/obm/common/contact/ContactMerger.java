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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.sync.book.Contact;
import org.obm.sync.book.IMergeable;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.utils.ObmHelper;

/**
 * Merges contact data sent by sync client into the contact data store in the
 * database. This is necessary to avoid deleting data not handled by a given
 * sync client.
 */
@Singleton
public class ContactMerger {

	private final ObmHelper obmHelper;

	@Inject
	private ContactMerger(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}
	
	public void merge(Contact previous, Contact c) throws SQLException {
		// TODO fix merge to not delete values not provided by sync client
		if (previous.getEntityId() == null) {
			Integer entityId = obmHelper.fetchEntityId("Contact", previous
					.getUid());
			previous.setEntityId(entityId);
		}

		c.setEntityId(previous.getEntityId());
		c.setFolderId(previous.getFolderId());

		if (c.getFirstname() == null && previous.getFirstname() != null) {
			c.setFirstname(previous.getFirstname());
		}
		if (c.getCompany() == null && previous.getCompany() != null) {
			c.setCompany(previous.getCompany());
		}
		if (c.getAka() == null && previous.getAka() != null) {
			c.setAka(previous.getAka());
		}
		if (c.getService() == null && previous.getService() != null) {
			c.setService(previous.getService());
		}
		if (c.getTitle() == null && previous.getTitle() != null) {
			c.setTitle(previous.getTitle());
		}

		if (c.getSuffix() == null && previous.getSuffix() != null) {
			c.setSuffix(previous.getSuffix());
		}
		if (c.getMiddlename() == null && previous.getMiddlename() != null) {
			c.setMiddlename(previous.getMiddlename());
		}
		if (c.getManager() == null && previous.getManager() != null) {
			c.setManager(previous.getManager());
		}
		if (c.getAssistant() == null && previous.getAssistant() != null) {
			c.setAssistant(previous.getAssistant());
		}
		if (c.getSpouse() == null && previous.getSpouse() != null) {
			c.setSpouse(previous.getSpouse());
		}

		mergeMap(previous.getPhones(), c.getPhones());
		mergeMap(previous.getEmails(), c.getEmails());
		mergeMap(previous.getAddresses(), c.getAddresses());
		mergeMap(previous.getImIdentifiers(), c.getImIdentifiers());
		mergeMap(previous.getWebsites(), c.getWebsites());
	}

	private <T extends IMergeable> void mergeMap(Map<String, T> old,
			Map<String, T> recent) {
		Set<String> oldLabels = old.keySet();
		Set<String> newLabels = recent.keySet();
		List<String> newPrefLabels = new ArrayList<String>();
		for (String key : newLabels) {
			newPrefLabels.add(key);
		}

		for (String ol : oldLabels) {
			if (!newLabels.contains(ol)) {
				if (!newPrefLabels.contains(ol)) {
					recent.put(ol, old.get(ol));
				} else {
					String shortLbl = ol.replace("PREF;", "");
					IMergeable newM = recent.get(shortLbl);
					IMergeable oldM = old.get(ol);
					newM.merge(oldM);
					recent.put(ol, recent.get(shortLbl));
					recent.remove(shortLbl);
				}
			} else {
				IMergeable newM = recent.get(ol);
				IMergeable oldM = old.get(ol);
				newM.merge(oldM);
			}
		}
	}

}
