package org.obm.sync.solr;

import java.net.MalformedURLException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.naming.ConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.obm.locator.LocatorClient;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.services.constant.ConstantService;

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
		private final String locatorUrl;
		private final ExecutorService executor;
		private final ContactIndexer.Factory contactIndexerFactory;
		private final org.obm.sync.solr.EventIndexer.Factory eventIndexerFactory;
		private final LinkedBlockingQueue<Runnable> workQueue;
		private final Timer debugTimer;
		
		@Inject
		private Factory(ConstantService constantService, ContactIndexer.Factory contactIndexerFactory,
				EventIndexer.Factory eventIndexerFactory) throws ConfigurationException {
			this.contactIndexerFactory = contactIndexerFactory;
			this.eventIndexerFactory = eventIndexerFactory;
			locatorUrl = constantService.getLocatorUrl();
			manager = new MultiThreadedHttpConnectionManager();
			workQueue = new LinkedBlockingQueue<Runnable>();
			executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, workQueue);
			client = new HttpClient(manager);
			debugTimer = new Timer();
			if (logger.isInfoEnabled()) {
				scheduleFixedRateDebugLog();
			}
		}

		private void scheduleFixedRateDebugLog() {
			debugTimer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					logger.info("SolR indexing queue size : " + workQueue.size());
				}
			}, 0, 10000);
		}
		
		public void shutdown() {
			executor.shutdown();
			manager.shutdown();
			debugTimer.cancel();
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
						+ at.getDomain()) + ":8080/solr/contact", client);

		sEvent = new CommonsHttpSolrServer("http://"
				+ lc.getServiceLocation("solr/event", at.getUser() + "@"
						+ at.getDomain()) + ":8080/solr/event", client);
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
