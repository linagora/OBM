/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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
package org.obm.sync.calendar;

import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.remoting.impl.netty.TransportConstants;
import org.hornetq.jms.client.HornetQConnectionFactory;
import org.hornetq.jms.client.HornetQTopic;
import org.obm.sync.MessageQueueModule;
import org.obm.sync.ServicesClientModule;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linagora.obm.sync.HornetQConfiguration;

public class ServicesClientWithJMSModule extends AbstractModule {
	
	private final HornetQConnectionFactory connectionFactory;
	
	public ServicesClientWithJMSModule() {
		connectionFactory = HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, 
				HornetQConfiguration.connectorBuilder()
					.name("jmsClient")
					.factory(NettyConnectorFactory.class)
					.param(TransportConstants.HOST_PROP_NAME, "127.0.0.1")
					.param(TransportConstants.PORT_PROP_NAME, 5446)
					.build());
	}
	
	@Override
	protected void configure() {
		install(new ServicesClientModule());
		bind(HornetQConnectionFactory.class).toInstance(connectionFactory);
		bind(MessageConsumerResourcesManager.class);
	}
	
	@Singleton
	public static class MessageConsumerResourcesManager {
		
		private final HornetQConnectionFactory connectionFactory;
		private final HornetQTopic topic;
		private MessageConsumer consumer;
		private TopicConnection connection;
		private TopicSession topicSession;

		@Inject
		private MessageConsumerResourcesManager(HornetQConnectionFactory connectionFactory) {
			this.connectionFactory = connectionFactory;
			this.topic = new HornetQTopic(MessageQueueModule.TOPIC_NAME_EVENT);
		}

		public MessageConsumer getConsumer() {
			return consumer;
		}
		
		public void start() throws Exception {
			Preconditions.checkState(topicSession == null && connection == null, "consumer is already started");
			connection = connectionFactory.createTopicConnection(); 
			connection.setClientID("durable");
			
			topicSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			connection.start();
			
			consumer = topicSession.createConsumer(topic);
		}

		public void close() throws Exception {
			Preconditions.checkState(topicSession != null && connection != null, "consumer is not started");
			topicSession.close();
			connection.close();
		}
	}
}
