/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.solr;

import static org.easymock.EasyMock.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.dbcp.ConfigurationServiceFixturePostgreSQL;


public class SolrManagerTest {

	private CommonsHttpSolrServer server;
	private SolrManager manager;
	private Lock lock;
	private Condition condition;
	private SolrRequest pingRequest;
	
	@Before
	public void setUp()
	{
		manager = new SolrManager(new ConfigurationServiceFixturePostgreSQL());
		lock = new ReentrantLock();
		condition = lock.newCondition();
		server = createMockBuilder(CommonsHttpSolrServer.class).addMockedMethod("ping").createStrictMock();
		pingRequest = new PingSolrRequest(server, lock, condition);
	}
	
	@Test
	public void solr_down_when_request_fails() throws Exception {
		expect(server.ping()).andThrow(new IOException()).anyTimes();
		replay(server);
		
		manager.process(pingRequest);
		
		Assertions.assertThat(waitForRequestProcessing()).isTrue();
		Assertions.assertThat(manager.isSolrAvailable()).isFalse();
		verify(server);
	}
	
	@Test
	public void solr_remains_up_when_request_pass() throws Exception {
		expect(server.ping()).andReturn(null).anyTimes();
		replay(server);
		
		manager.process(pingRequest);
		
		Assertions.assertThat(waitForRequestProcessing()).isTrue();
		Assertions.assertThat(manager.isSolrAvailable()).isTrue();
		verify(server);
	}

	@Test
	public void request_processed_when_solr_is_up_again() throws Exception {
		expect(server.ping()).andThrow(new IOException()); 
		expect(server.ping()).andReturn(null); // SolR should be back up at the second check
		expect(server.ping()).andReturn(null); // This one's for the actual request that must be processed once SolR is back up
		replay(server);
		
		manager.setSolrCheckingInterval(100);
		manager.setSolrAvailable(false);
		manager.process(pingRequest);
		
		Assertions.assertThat(waitForRequestProcessing()).isTrue();
		Assertions.assertThat(manager.isSolrAvailable()).isTrue();
		verify(server);
	}
	
	@Test
	public void request_not_processed_while_solr_down() throws Exception {
		expect(server.ping()).andThrow(new IOException()).anyTimes(); 
		replay(server);
		
		manager.setSolrAvailable(false);
		manager.process(pingRequest);
		
		Assertions.assertThat(waitForRequestProcessing()).isFalse();
		Assertions.assertThat(manager.isSolrAvailable()).isFalse();
		
		verify(server);
	}
	
	private boolean waitForRequestProcessing() throws InterruptedException {
		lock.lock();
		
		return condition.await(2, TimeUnit.SECONDS);
	}

}
