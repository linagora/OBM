/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.common.contact;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.sync.book.Contact;
import org.obm.sync.book.IMergeable;
import org.obm.sync.book.Website;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.utils.ObmHelper;

/**
 * Merges contact data sent by sync client into the contact data store in the
 * database. This is necessary to avoid deleting data not handled by a given
 * sync client.
 * 
 */
@Singleton
public class ContactMerger {

	private final ObmHelper obmHelper;

	@Inject
	private ContactMerger(ObmHelper obmHelper) {
		this.obmHelper = obmHelper;
	}
	
	public void merge(Contact actualC, Contact updateC) throws SQLException {
		// TODO fix merge to not delete values not provided by sync client
		if (actualC.getEntityId() == null) {
			Integer entityId = obmHelper.fetchEntityId("Contact", actualC
					.getUid());
			actualC.setEntityId(entityId);
		}

		updateC.setEntityId(actualC.getEntityId());
		updateC.setFolderId(actualC.getFolderId());

		if (updateC.getFirstname() == null && actualC.getFirstname() != null) {
			updateC.setFirstname(actualC.getFirstname());
		}
		if (updateC.getCompany() == null && actualC.getCompany() != null) {
			updateC.setCompany(actualC.getCompany());
		}
		if (updateC.getAka() == null && actualC.getAka() != null) {
			updateC.setAka(actualC.getAka());
		}
		if (updateC.getService() == null && actualC.getService() != null) {
			updateC.setService(actualC.getService());
		}
		if (updateC.getTitle() == null && actualC.getTitle() != null) {
			updateC.setTitle(actualC.getTitle());
		}

		if (updateC.getSuffix() == null && actualC.getSuffix() != null) {
			updateC.setSuffix(actualC.getSuffix());
		}
		if (updateC.getMiddlename() == null && actualC.getMiddlename() != null) {
			updateC.setMiddlename(actualC.getMiddlename());
		}
		if (updateC.getManager() == null && actualC.getManager() != null) {
			updateC.setManager(actualC.getManager());
		}
		if (updateC.getAssistant() == null && actualC.getAssistant() != null) {
			updateC.setAssistant(actualC.getAssistant());
		}
		if (updateC.getSpouse() == null && actualC.getSpouse() != null) {
			updateC.setSpouse(actualC.getSpouse());
		}

		mergeMap(actualC.getPhones(), updateC.getPhones());
		mergeMap(actualC.getEmails(), updateC.getEmails());
		mergeMap(actualC.getAddresses(), updateC.getAddresses());
		mergeMap(actualC.getImIdentifiers(), updateC.getImIdentifiers());
		
		HashSet<Website> websites = mergeWebSite(actualC, updateC);
		updateC.updateWebSites(websites);
	}	
	
	private HashSet<Website> mergeWebSite(Contact actualC, Contact updateC) {
		HashSet<Website> websites = new HashSet<Website>();
		websites.addAll(updateC.getWebsites());
		for (Website website: actualC.getWebsites()) {
			if (!updateC.getWebsites().contains(website)) {				
				websites.add(website);
			}
		}
		return websites;
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
