/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013  Linagora
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

import javax.jms.JMSException;
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

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.linagora.obm.sync.HornetQConfiguration;

public class ServicesClientWithJMSModule extends AbstractModule {
	
	private HornetQConnectionFactory connectionFactory;
	
	@Override
	protected void configure() {
		install(new ServicesClientModule());
		connectionFactory = HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, 
			HornetQConfiguration.connectorBuilder()
				.name("jmsClient")
				.factory(NettyConnectorFactory.class)
				.param(TransportConstants.HOST_PROP_NAME, "127.0.0.1")
				.param(TransportConstants.PORT_PROP_NAME, 5446)
				.build());
	}
	
	@Provides @Singleton
	MessageConsumerWithResourcesCloser consumer() {
		try {
			HornetQTopic topic = new HornetQTopic(MessageQueueModule.TOPIC_NAME_EVENT);
			final TopicConnection connection = connectionFactory.createTopicConnection(); 
			connection.setClientID("durable");
			
			final TopicSession topicSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			connection.start();
			
			return new MessageConsumerWithResourcesCloser(topicSession.createConsumer(topic)) {

				@Override
				public void closeResources() throws Exception {
					topicSession.close();
					connection.close();
				}
				
			};
		} catch (JMSException e) {
			Throwables.propagate(e);
		}
		throw new IllegalStateException("No consumer can be provided");
	}
	
	public static abstract class MessageConsumerWithResourcesCloser {
		
		private final MessageConsumer consumer;

		public MessageConsumerWithResourcesCloser(MessageConsumer consumer) {
			this.consumer = consumer;
		}

		public MessageConsumer getConsumer() {
			return consumer;
		}
		
		public abstract void closeResources() throws Exception;
	}
}
