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
package org.obm.sync.solr;

import java.net.MalformedURLException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Event;
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

	private static final Logger logger = LoggerFactory
			.getLogger(SolrHelper.class);
	
	@Singleton
	public static class Factory {
		private final HttpClient client;
		private final MultiThreadedHttpConnectionManager manager;
		private final ContactIndexer.Factory contactIndexerFactory;
		private final org.obm.sync.solr.EventIndexer.Factory eventIndexerFactory;
		private final LocatorService locatorService;
		private final SolrManager solrManager;
		
		@Inject
		@VisibleForTesting
		protected Factory(ContactIndexer.Factory contactIndexerFactory, EventIndexer.Factory eventIndexerFactory, 
				LocatorService locatorService, SolrManager solrManager) {
			
			this.contactIndexerFactory = contactIndexerFactory;
			this.eventIndexerFactory = eventIndexerFactory;
			this.locatorService = locatorService;
			this.solrManager = solrManager;
			manager = new MultiThreadedHttpConnectionManager();
			client = new HttpClient(manager);
		}

		public void shutdown() {
			manager.shutdown();
		}

		public SolrHelper createClient(AccessToken at) throws MalformedURLException, LocatorClientException {
			if(!solrManager.isSolrAvailable()) {
				throw new IllegalStateException("SolR is unavailable");
			}
			
			return new SolrHelper(at, locatorService, client, contactIndexerFactory, eventIndexerFactory, solrManager);
		}
	}
	
	@VisibleForTesting
	protected CommonsHttpSolrServer sContact;
	
	private final CommonsHttpSolrServer sEvent;
	private final ContactIndexer.Factory contactIndexerFactory;
	private final ObmDomain domain;
	private final org.obm.sync.solr.EventIndexer.Factory eventIndexerFactory;
	private final SolrManager solrManager;

	private SolrHelper(AccessToken at, LocatorService locatorClient, HttpClient client, 
			ContactIndexer.Factory contactIndexerFactory, EventIndexer.Factory eventIndexerFactory, SolrManager solrManager) 
					throws MalformedURLException, LocatorClientException {
		
		this.eventIndexerFactory = eventIndexerFactory;
		this.domain = at.getDomain();
		this.contactIndexerFactory = contactIndexerFactory;
		this.solrManager = solrManager;

		sContact = new CommonsHttpSolrServer("http://"
				+ locatorClient.getServiceLocation("solr/contact", at.getUserLogin() + "@"
						+ at.getDomain().getName()) + ":8080/solr/contact", client);

		sEvent = new CommonsHttpSolrServer("http://"
				+ locatorClient.getServiceLocation("solr/event", at.getUserLogin() + "@"
						+ at.getDomain().getName()) + ":8080/solr/event", client);
	}
	
	public CommonsHttpSolrServer getSolrContact(){
		return sContact;
	}
	
	public CommonsHttpSolrServer getSolrEvent(){
		return sEvent;
	}
	
	public void createOrUpdate(Contact c) {
		logger.info("[contact " + c.getUid() + "] scheduled for solr indexing");
		ContactIndexer ci = contactIndexerFactory.createIndexer(sContact, c);
		solrManager.process(ci);
	}

	public void delete(Contact c) {
		logger.info("[contact " + c.getUid() + "] scheduled for solr removal");
		Remover rm = new Remover(sContact, c.getUid().toString());
		solrManager.process(rm);
	}

	public void delete(Event e) {
		logger.info("[event " + e.getObmId() + "] scheduled for solr removal");
		Remover rm = new Remover(sEvent, Integer.toString(e.getObmId().getObmId()));
		solrManager.process(rm);
	}

	public void createOrUpdate(Event ev) {
		logger.info("[event " + ev.getObmId() + "] scheduled for solr indexing");
		EventIndexer ci = eventIndexerFactory.createIndexer(sEvent, domain, ev);
		solrManager.process(ci);
	}
}
