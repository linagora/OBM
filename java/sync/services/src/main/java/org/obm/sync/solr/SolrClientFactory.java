/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014  Linagora
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
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.http.client.utils.URIBuilder;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class SolrClientFactory {

	private HttpClient httpClient;
	private LocatorService locatorService;
	
	@Inject
	@VisibleForTesting
	SolrClientFactory(LocatorService locatorService) {
		this.locatorService = locatorService;
		this.httpClient = buildHttpClient();
	}

	private HttpClient buildHttpClient() {
		MultiThreadedHttpConnectionManager cnxManager = new MultiThreadedHttpConnectionManager();
		cnxManager.getParams().setDefaultMaxConnectionsPerHost(10);
		cnxManager.getParams().setMaxTotalConnections(100);
		return new HttpClient(cnxManager);
	}
	
	public CommonsHttpSolrServer create(SolrService service, ObmDomain domain) {
		try {
			URI uri = new URIBuilder()
				.setScheme("http")
				.setHost(locatorService.getServiceLocation(service.getName(), domain.getName()))
				.setPort(8080)
				.setPath('/' + service.getName())
				.build();
			return new CommonsHttpSolrServer(uri.toURL(), httpClient);
		} catch (MalformedURLException e) {
			throw new LocatorClientException(e);
		} catch (URISyntaxException e) {
			throw new LocatorClientException(e);
		}
	}
	
}
