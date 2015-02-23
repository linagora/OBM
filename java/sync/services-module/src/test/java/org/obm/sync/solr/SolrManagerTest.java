/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.JMSException;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.easymock.IMocksControl;
import org.hornetq.core.config.Configuration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;
import org.obm.sync.solr.jms.Command;
import org.obm.sync.solr.jms.SolrJmsQueue;

import com.linagora.obm.sync.HornetQConfiguration;
import com.linagora.obm.sync.QueueManager;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.domain.Samba;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;


public class SolrManagerTest {

	private CommonsHttpSolrServer server;
	private SolrManager manager;
	private PingSolrRequest pingRequest;
	private Command<Integer> pingCommand;
	private QueueManager queueManager;
	private ObmSyncConfigurationService configurationService;
	private SolrClientFactory solrClientFactory;
	private IMocksControl control;
	
	public static Configuration hornetQConfiguration() {
		return HornetQConfiguration.configuration()
				.enablePersistence(false)
				.enableSecurity(false)
				.journalDirectory("target/jms-journal")
				.connector(HornetQConfiguration.Connector.HornetQInVMCore)
				.acceptor(HornetQConfiguration.Acceptor.HornetQInVMCore)
				.build();
	}
	
	private static JMSConfiguration jmsConfiguration() {
		return 
			HornetQConfiguration.jmsConfiguration()
			.connectionFactory(
					HornetQConfiguration.connectionFactoryConfigurationBuilder()
					.name("ConnectionFactory")
					.connector(HornetQConfiguration.Connector.HornetQInVMCore)
					.binding("ConnectionFactory")
					.build())
			.topic("eventChanges", "/topic/eventChanges")
			.topic("calendarChanges", SolrJmsQueue.CALENDAR_CHANGES_QUEUE.getName())
			.topic("contactChanges", SolrJmsQueue.CONTACT_CHANGES_QUEUE.getName())
			.build();
	}
	
	@Before
	public void setUp() throws Exception {
		control = createControl();
		ObmDomain domain = ObmDomain.builder().name("d").build();
		queueManager = new QueueManager(hornetQConfiguration(), jmsConfiguration());
		queueManager.start();
		
		configurationService = control.createMock(ObmSyncConfigurationService.class);
		solrClientFactory = control.createMock(SolrClientFactoryImpl.class);
		server = control.createMock(CommonsHttpSolrServer.class);
		
		expect(configurationService.solrCheckingInterval()).andReturn(10).anyTimes();
		expect(solrClientFactory.create(SolrService.CONTACT_SERVICE, domain)).andReturn(server).anyTimes();

		PingSolrRequest.lock = new ReentrantLock();
		PingSolrRequest.condition = PingSolrRequest.lock.newCondition();
		PingSolrRequest.error = null;
		pingRequest = new PingSolrRequest(domain, SolrService.CONTACT_SERVICE);
		pingCommand = new PingCommand(pingRequest);
	}
	
	@After
	public void tearDown() throws Exception {
		if (manager != null) {
			manager.stop();
		}
		queueManager.stop();
	}
	
	@Test(expected=IllegalStateException.class)
	public void requestShouldBerejectedWhenQueueUnknown() throws JMSException {
		control.replay();
		manager = new SolrManager(configurationService, queueManager, solrClientFactory);
		manager.process(new PingCommand(pingRequest) {

			@Override
			public SolrJmsQueue getQueue() {
				return null;
			}

		});
	}
	
	@Test
 	public void removerRequestShouldBeSerializable() throws Exception {
 		Remover remover = new Remover(ObmDomain.builder().name("d").build(), SolrService.CONTACT_SERVICE, "id");
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(remover);
		oos.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object object = ois.readObject();

		ois.close();
  		
  		assertThat(object).isInstanceOf(Remover.class).isEqualToComparingFieldByField(remover);
  }
	@Test
 	public void solrDocumentIndexerShouldBeSerializable() throws Exception {
		ObmDomain domain = ObmDomain
			.builder()
			.id(1)
			.name("domain_name")
			.uuid(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6"))
			.label("domain_label")
			.alias("domain_alias")
			.global(false)
			.host(ServiceProperty.IMAP,
				ObmHost.builder().id(1).domainId(1).name("imap").ip("1.2.3.4").fqdn("imap.domain_name").build())
			.samba(Samba.builder().sid("sid").profile("profile").home("home").drive("drive").build())
			.build();
		
		SolrDocumentIndexer indexer = new SolrDocumentIndexer(domain, SolrService.CONTACT_SERVICE, new SolrInputDocument());
  		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(indexer);
		oos.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object object = ois.readObject();

		ois.close();
  		
  		assertThat(object).isInstanceOf(SolrDocumentIndexer.class).isEqualToComparingFieldByField(indexer);
  }
	
	@Test
	public void solrShouldBeMarkedAsDownWhenRequestFails() throws Exception {        
		expect(server.ping()).andThrow(new IOException()).anyTimes();
		control.replay();
		
		manager = new SolrManager(configurationService, queueManager, solrClientFactory);
		manager.process(pingCommand);
		
		assertThat(waitForRequestProcessing()).isTrue();
		assertThat(pingRequest.getError()).isInstanceOf(IOException.class);
		assertThat(manager.isSolrAvailable()).isFalse();
		control.verify();
	}
	
	@Test
	public void solrShouldRemainsUpWhenRequestPass() throws Exception {
		expect(server.ping()).andReturn(null).anyTimes();
		control.replay();
		
		manager = new SolrManager(configurationService, queueManager, solrClientFactory);
		manager.process(pingCommand);
		
		assertThat(waitForRequestProcessing()).isTrue();
		assertThat(pingRequest.getError()).isNull();
		assertThat(manager.isSolrAvailable()).isTrue();
		control.verify();
	}

	@Test
	public void requestShouldBeProcessedWhenSolrIsUpAgain() throws Exception {
		expect(server.ping()).andThrow(new IOException()); // This will make SolR unavailable at first request
		expect(server.ping()).andReturn(null); // SolR should be back up at the second check
		expect(server.ping()).andReturn(null); // This one's for the actual request that must be processed once SolR is back up
		control.replay();
		
		manager = new SolrManager(configurationService, queueManager, solrClientFactory);
		manager.setSolrCheckingInterval(100);
		manager.process(pingCommand);
		
		assertThat(waitForRequestProcessing()).isTrue();
		assertThat(waitForRequestProcessing()).isTrue(); // Request will be processed a second time once SolR is back
		assertThat(manager.isSolrAvailable()).isTrue();
		control.verify();
	}
	
	@Test
	public void requestShouldNotProcessedWhileSolrDown() throws Exception {
		expect(server.ping()).andThrow(new IOException()).anyTimes(); 
		control.replay();
		
		manager = new SolrManager(configurationService, queueManager, solrClientFactory);
		manager.setSolrAvailable(false);
		manager.process(pingCommand);
		
		assertThat(waitForRequestProcessing()).isFalse();
		assertThat(pingRequest.getError()).isNull();
		assertThat(manager.isSolrAvailable()).isFalse();
		
		control.verify();
	}
	
	private boolean waitForRequestProcessing() throws InterruptedException {
		PingSolrRequest.lock.lock();
		
		return PingSolrRequest.condition.await(2, TimeUnit.SECONDS);
	}

	private static class PingCommand extends Command<Integer> {

		private SolrJmsQueue queue;
		private PingSolrRequest request;

		public PingCommand(PingSolrRequest request) {
			this(SolrJmsQueue.CONTACT_CHANGES_QUEUE, request);
		}
		
		public PingCommand(SolrJmsQueue queue, PingSolrRequest request) {
			super(null, "l", 0);
			
			this.queue = queue;
			this.request = request;
		}

		@Override
		public SolrJmsQueue getQueue() {
			return queue;
		}

		@Override
		public SolrService getSolrService() {
			return null;
		}

		@Override
		public SolrRequest asSolrRequest() {
			return request;
		}

	}
}
