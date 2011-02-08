package org.obm.sync.solr;

import java.net.MalformedURLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.ConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.obm.locator.LocatorClient;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Event;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.services.constant.ConstantService;

/**
 * Manages full-text indexing of events & contacts in a SOLR server
 */
public class SolrHelper {

	private static final Log logger = LogFactory.getLog(SolrHelper.class);
	
	@Singleton
	public static class Factory {
		private HttpClient client;
		private MultiThreadedHttpConnectionManager manager;
		private String locatorUrl;
		private ExecutorService executor;
		private final ContactIndexer.Factory contactIndexerFactory;
		private final org.obm.sync.solr.EventIndexer.Factory eventIndexerFactory;
		
		@Inject
		private Factory(ConstantService constantService, ContactIndexer.Factory contactIndexerFactory,
				EventIndexer.Factory eventIndexerFactory) throws ConfigurationException {
			this.contactIndexerFactory = contactIndexerFactory;
			this.eventIndexerFactory = eventIndexerFactory;
			locatorUrl = constantService.getLocatorUrl();
			manager = new MultiThreadedHttpConnectionManager();
			executor = Executors.newSingleThreadExecutor();
			client = new HttpClient(manager);
		}
		
		public void shutdown() {
			executor.shutdown();
			manager.shutdown();
		}

		public SolrHelper createClient(AccessToken at) throws MalformedURLException {
			return new SolrHelper(at, locatorUrl, client, executor, contactIndexerFactory, eventIndexerFactory);
		}
	}
	
	private CommonsHttpSolrServer sContact;
	private CommonsHttpSolrServer sEvent;
	private final ExecutorService executor;
	private final ContactIndexer.Factory contactIndexerFactory;
	private final int domain;
	private final org.obm.sync.solr.EventIndexer.Factory eventIndexerFactory;

	private SolrHelper(AccessToken at, String locatorUrl, HttpClient client, ExecutorService executor, 
			ContactIndexer.Factory contactIndexerFactory, EventIndexer.Factory eventIndexerFactory) throws MalformedURLException {
		this.eventIndexerFactory = eventIndexerFactory;
		this.domain = at.getDomainId();
		this.executor = executor;
		this.contactIndexerFactory = contactIndexerFactory;
		LocatorClient lc = new LocatorClient(locatorUrl);

		sContact = new CommonsHttpSolrServer("http://"
				+ lc.getServiceLocation("solr/contact", at.getUser() + "@"
						+ domain) + ":8080/solr/contact", client);

		sEvent = new CommonsHttpSolrServer("http://"
				+ lc.getServiceLocation("solr/event", at.getUser() + "@"
						+ domain) + ":8080/solr/event", client);
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
		executor.execute(ci);
	}

	public void delete(Contact c) {
		logger.info("[contact " + c.getUid() + "] scheduled for solr removal");
		Remover rm = new Remover(sContact, c.getUid().toString());
		executor.execute(rm);
	}

	public void delete(Event e) {
		logger.info("[event " + e.getUid() + "] scheduled for solr removal");
		Remover rm = new Remover(sEvent, e.getUid().toString());
		executor.execute(rm);
	}

	public void createOrUpdate(Event ev) {
		logger.info("[event " + ev.getUid() + "] scheduled for solr indexing");
		EventIndexer ci = eventIndexerFactory.createIndexer(sEvent, domain, ev);
		executor.execute(ci);
	}
}
