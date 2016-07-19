/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2016 Linagora
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
package org.obm.service.solr;

import org.obm.domain.dao.EntityDaoListener;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SolrEntityDaoListener implements EntityDaoListener {

	private static final Logger logger = LoggerFactory.getLogger(EntityDaoListener.class);
	
	private final SolrHelper.Factory solrHelperFactory;

	@Inject
	public SolrEntityDaoListener(SolrHelper.Factory solrHelperFactory) {
		this.solrHelperFactory = solrHelperFactory;
	}
	
	@Override
	public void eventHasBeenCreated(AccessToken editor, Event event) {
		indexEvent(editor, event);
	}

	@Override
	public void eventHasBeenRemoved(AccessToken editor, Event event) {
		removeEventFromSolr(editor, event);
	}
	
	@Override
	public void contactHasBeenCreated(AccessToken editor, Contact contact) {
		indexContact(editor, contact);
	}
	
	@Override
	public void contactHasBeenRemoved(AccessToken editor, Contact contact) {
		removeContactFromSolr(editor, contact);
	}

	private void indexEvent(AccessToken editor, Event ev) {
		try {
			solrHelperFactory.createClient(editor).createOrUpdate(ev);
		} catch (Throwable t) {
			logger.error("indexing error " + t.getMessage(), t);
		}
	}

	private void removeEventFromSolr(AccessToken token, Event ev) {
		try {
			solrHelperFactory.createClient(token).delete(ev);
		} catch (Throwable t) {
			logger.error("indexing error " + t.getMessage(), t);
		}
	}

	private void indexContact(AccessToken editor, Contact contact) {
		try {
			solrHelperFactory.createClient(editor).createOrUpdate(contact);
		} catch (Throwable t) {
			logger.error("indexing error " + t.getMessage(), t);
		}
	}

	private void removeContactFromSolr(AccessToken token, Contact contact) {
		try {
			solrHelperFactory.createClient(token).delete(contact);
		} catch (Throwable t) {
			logger.error("indexing error " + t.getMessage(), t);
		}
	}

}
