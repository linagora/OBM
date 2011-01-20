package org.obm.sync.solr;

import java.net.MalformedURLException;

import javax.naming.ConfigurationException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.obm.locator.LocatorClient;
import org.obm.sync.auth.AccessToken;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.services.constant.ConstantService;

/**
 * Manages full-text indexing of events & contacts in a SOLR server
 */
public class SolrHelper {

	@Singleton
	public static class Factory {
		private HttpClient client;
		private MultiThreadedHttpConnectionManager manager;
		private String locatorUrl;
		
		@Inject
		private Factory(ConstantService constantService) throws ConfigurationException {
			locatorUrl = constantService.getLocatorUrl();
			manager = new MultiThreadedHttpConnectionManager();
			client = new HttpClient(manager);
		}
		
		public void shutdown() {
			manager.shutdown();
		}

		public SolrHelper createClient(AccessToken at) throws MalformedURLException {
			return new SolrHelper(at, locatorUrl, client);
		}
	}
	
	private CommonsHttpSolrServer sContact;
	private CommonsHttpSolrServer sEvent;

	private SolrHelper(AccessToken at, String locatorUrl, HttpClient client) throws MalformedURLException {
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
}
