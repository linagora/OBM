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
package org.obm.service;

import org.hornetq.core.config.Configuration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.obm.configuration.ConfigurationService;
import org.obm.service.solr.jms.SolrJmsQueue;
import org.obm.sync.LifecycleListener;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.linagora.obm.sync.HornetQConfiguration;
import com.linagora.obm.sync.JMSServer;

public class MessageQueueServerModule extends AbstractModule {
	
	public MessageQueueServerModule() {
		super();
	}

	@Override
	protected void configure() {
		bind(JMSServer.class).asEagerSingleton();
		
		Multibinder<LifecycleListener> lifecycleListeners = Multibinder.newSetBinder(binder(), LifecycleListener.class);
		lifecycleListeners.addBinding().to(JMSServer.class);
	}
	
	@Provides @Singleton
	public static Configuration hornetQConfiguration(ConfigurationService configurationService) {
		String dataDirectory = configurationService.getDataDirectory() + "/" + "jms/data";
		return HornetQConfiguration.configuration()
				.enablePersistence(true)
				.enableSecurity(false)
				.largeMessagesDirectory(dataDirectory + "/large-messages")
				.bindingsDirectory(dataDirectory + "/bindings")
				.journalDirectory(dataDirectory + "/journal")
				.pagingDirectory(dataDirectory + "/paging")
				.connector(HornetQConfiguration.Connector.HornetQInVMCore)
				.connector(HornetQConfiguration.Connector.HornetQSocketCore)
				.acceptor(HornetQConfiguration.Acceptor.HornetQInVMCore)
				.acceptor(HornetQConfiguration.Acceptor.HornetQSocketCore)
				.acceptor(HornetQConfiguration.Acceptor.Stomp)
				.build();
	}

	@Provides @Singleton
	public static JMSConfiguration jmsConfiguration() {
		return 
			HornetQConfiguration.jmsConfiguration()
			.connectionFactory(
					HornetQConfiguration.connectionFactoryConfigurationBuilder()
					.name("ConnectionFactory")
					.connector(HornetQConfiguration.Connector.HornetQInVMCore)
					.connector(HornetQConfiguration.Connector.HornetQSocketCore)
					.binding("ConnectionFactory")
					.build())
			.topic(SolrJmsQueue.EVENT_CHANGES_QUEUE.getId(), SolrJmsQueue.EVENT_CHANGES_QUEUE.getName())
			.topic(SolrJmsQueue.CALENDAR_CHANGES_QUEUE.getId(), SolrJmsQueue.CALENDAR_CHANGES_QUEUE.getName())
			.topic(SolrJmsQueue.CONTACT_CHANGES_QUEUE.getId(), SolrJmsQueue.CONTACT_CHANGES_QUEUE.getName())
			.build();
	}
	
}
