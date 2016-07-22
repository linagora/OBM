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
package org.obm;

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;
import org.obm.service.solr.SolrClientFactory;
import org.obm.service.solr.SolrService;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Module;

import fr.aliacom.obm.common.domain.ObmDomain;

public class SolrModuleUtils {

	private static class DummyCommonsHttpSolrServer extends CommonsHttpSolrServer {
		public DummyCommonsHttpSolrServer() throws MalformedURLException {
			super("http://localhost:8983/solr/");
		}
		
		@Override
		public UpdateResponse deleteById(String id) throws SolrServerException, IOException {
			return null;
		}
		
		@Override
		public UpdateResponse add(SolrInputDocument doc) throws SolrServerException, IOException {
			return null;
		}
		
		@Override
		public UpdateResponse commit() throws SolrServerException, IOException {
			return null;
		}
	}
	
	public static Module buildDummySolrModule() {
		return new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(SolrClientFactory.class).toInstance(new SolrClientFactory() {
					
					@Override
					public CommonsHttpSolrServer create(SolrService service, ObmDomain domain) {
						try {
							return new DummyCommonsHttpSolrServer();
						} catch (MalformedURLException e) {
							throw Throwables.propagate(e);
						}
					}
				});
			}
		};
	}
	
}