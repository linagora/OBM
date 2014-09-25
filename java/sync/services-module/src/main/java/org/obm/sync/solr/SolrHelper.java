/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.sync.solr;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Event;
import org.obm.sync.solr.jms.ContactDeleteCommand;
import org.obm.sync.solr.jms.ContactUpdateCommand;
import org.obm.sync.solr.jms.EventDeleteCommand;
import org.obm.sync.solr.jms.EventUpdateCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;

/**
 * Manages full-text indexing of events & contacts in a SOLR server
 */
public class SolrHelper {

	private static final Logger logger = LoggerFactory.getLogger(SolrHelper.class);
	
	@Singleton
	public static class Factory {
		private final SolrManager solrManager;
		private final ContactUpdateCommand.Factory contactCommandFactory;
		private final EventUpdateCommand.Factory eventCommandFactory;
		private final SolrClientFactory solrClientFactory;
		
		@Inject
		@VisibleForTesting
		protected Factory(SolrManager solrManager,
				SolrClientFactory solrClientFactory,
				ContactUpdateCommand.Factory contactCommandFactory, EventUpdateCommand.Factory eventCommandFactory) {
			this.solrManager = solrManager;
			this.solrClientFactory = solrClientFactory;
			this.contactCommandFactory = contactCommandFactory;
			this.eventCommandFactory = eventCommandFactory;
		}

		public SolrHelper createClient(AccessToken at) {
			if(!solrManager.isSolrAvailable()) {
				throw new IllegalStateException("SolR is unavailable");
			}
			
			return new SolrHelper(at, solrManager, solrClientFactory, eventCommandFactory, contactCommandFactory);
		}
	}
	
	private final AccessToken at;
	private final ObmDomain domain;
	private final SolrManager solrManager;
	private final EventUpdateCommand.Factory eventCommandFactory;
	private final ContactUpdateCommand.Factory contactCommandFactory;
	private final SolrClientFactory solrClientFactory;
	
	private SolrHelper(AccessToken at, SolrManager solrManager,
			SolrClientFactory solrClientFactory,
			EventUpdateCommand.Factory eventCommandFactory, 
			ContactUpdateCommand.Factory contactCommandFactory) {
		this.at = at;
		this.solrClientFactory = solrClientFactory;
		this.domain = at.getDomain();
		this.solrManager = solrManager;
		this.eventCommandFactory = eventCommandFactory;
		this.contactCommandFactory = contactCommandFactory;
		
	}

	public CommonsHttpSolrServer getSolrContact() {
		return solrClientFactory.create(SolrService.CONTACT_SERVICE, at.getUserLogin() + "@" + domain.getName());
	}
	
	public void createOrUpdate(Contact contact) {
		logger.info("[contact {}] scheduled for solr indexing", contact.getUid());
		solrManager.process(contactCommandFactory.create(domain, at.getUserLogin(), contact));
	}

	public void delete(Contact contact) {
		logger.info("[contact {} ] scheduled for solr removal", contact.getUid());
		solrManager.process(new ContactDeleteCommand(domain, at.getUserLogin(), contact));
	}

	public void delete(Event event) {
		logger.info("[event {} ] scheduled for solr removal", event.getObmId());
		solrManager.process(new EventDeleteCommand(domain, at.getUserLogin(), event));
	}

	public void createOrUpdate(Event event) {
		logger.info("[event {} ] scheduled for solr indexing", event.getObmId());
		solrManager.process(eventCommandFactory.create(domain, at.getUserLogin(), event));
	}
}
