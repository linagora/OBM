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
package org.obm.service.solr;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.easymock.IMocksControl;
import org.hornetq.core.config.Configuration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.ConfigurationService;
import org.obm.locator.LocatorClientException;
import org.obm.service.solr.jms.ContactUpdateCommand;
import org.obm.service.solr.jms.EventUpdateCommand;
import org.obm.service.solr.jms.SolrJmsQueue;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;

import com.linagora.obm.sync.HornetQConfiguration;
import com.linagora.obm.sync.QueueManager;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.domain.ObmDomain;

public class SolrHelperFactoryTest {

	private Contact contact;
	private AccessToken accessToken;
	private SolrHelper.Factory factory;
	private SolrManager manager;
	private QueueManager queueManager;
	private ConfigurationService configurationService;
	private IMocksControl control;
	private SolrClientFactory solrClientFactory;

	private static JMSConfiguration jmsConfiguration() {
		return 
			HornetQConfiguration.jmsConfiguration()
			.connectionFactory(
					HornetQConfiguration.connectionFactoryConfigurationBuilder()
					.name("ConnectionFactory")
					.connector(HornetQConfiguration.Connector.HornetQInVMCore)
					.binding("ConnectionFactory")
					.build())
			.topic("calendarChanges", SolrJmsQueue.CALENDAR_CHANGES_QUEUE.getName())
			.topic("contactChanges", SolrJmsQueue.CONTACT_CHANGES_QUEUE.getName())
			.build();
	}
	
	public static Configuration hornetQConfiguration() {
		return HornetQConfiguration.configuration()
				.enablePersistence(false)
				.enableSecurity(false)
				.journalDirectory("target/jms-journal")
				.connector(HornetQConfiguration.Connector.HornetQInVMCore)
				.acceptor(HornetQConfiguration.Acceptor.HornetQInVMCore)
				.build();
	}
	
	@Before
	public void setUp() throws Exception {
		queueManager = new QueueManager(hornetQConfiguration(), jmsConfiguration());
		queueManager.start();
		contact = new Contact();

 		control = createControl();
 		accessToken = ToolBox.mockAccessToken(control);
 		solrClientFactory = control.createMock(SolrClientFactoryImpl.class);
 		configurationService = control.createMock(ConfigurationService.class);
 		CommonsHttpSolrServer solrClient = control.createMock(CommonsHttpSolrServer.class);
 		ContactUpdateCommand.Factory contactCommandFactory = control.createMock(ContactUpdateCommand.Factory.class);
 		EventUpdateCommand.Factory eventCommandFactory = control.createMock(EventUpdateCommand.Factory.class);
  		
 		expect(configurationService.solrCheckingInterval()).andReturn(10);
 		expect(solrClientFactory.create(SolrService.CONTACT_SERVICE, ObmDomain.builder().name("test.tlse.lng").build())).andReturn(solrClient);
 		expect(solrClient.deleteById(anyObject(String.class))).andReturn(null);
 		expect(solrClient.commit()).andReturn(null);

 		control.replay();

 		manager = new SolrManager(configurationService, queueManager, solrClientFactory);
		factory = new SolrHelper.Factory(manager, solrClientFactory, contactCommandFactory, eventCommandFactory);
  
 		contact.setUid(1);
	}

	@After
	public void tearDown() throws Exception {
		manager.stop();
		queueManager.stop();
	}

	@Test(expected = IllegalStateException.class)
	public void delete_contact_solr_remains_unavailable() throws LocatorClientException {
		delete(false);
	}

	@Test
	public void delete_contact_solr_available() throws LocatorClientException {
		try {
			delete(true);
		}
		catch (IllegalStateException e) {
			fail();
		}
	}

	@Test
	public void delete_contact_solr_becomes_available() throws  LocatorClientException {
		try {
			delete(false);
		}
		catch (IllegalStateException e) {
		}
		delete_contact_solr_available();
	}

	@Test(expected = IllegalStateException.class)
	public void delete_contact_solr_becomes_unavailable() throws LocatorClientException {
		delete_contact_solr_available();
		delete(false);
	}

	private void delete(boolean solrUp) {
		manager.setSolrAvailable(solrUp);
		factory.createClient(accessToken).delete(contact);
	}

	private void fail() {
		Assert.fail("creation should silently pass when the Indexer is available");
	}
}
