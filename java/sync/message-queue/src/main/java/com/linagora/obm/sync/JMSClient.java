/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2016 Linagora
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
package com.linagora.obm.sync;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.api.jms.HornetQJMSClient;
import org.hornetq.api.jms.JMSFactoryType;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.jms.client.HornetQConnectionFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

@Singleton
public class JMSClient {

	private final HornetQConnectionFactory cf;

	@Inject
	public JMSClient(@Named("queueIsRemote") boolean queueIsRemote) {
		TransportConfiguration transportConfiguration = new TransportConfiguration(getConnectorClass(queueIsRemote));
		cf = HornetQJMSClient.createConnectionFactoryWithoutHA(JMSFactoryType.CF, transportConfiguration);
	}

	private String getConnectorClass(boolean queueIsRemote) {
		if (queueIsRemote) {
			return NettyConnectorFactory.class.getName();
		}
		return InVMConnectorFactory.class.getName();
	}

	public Connection createConnection() throws JMSException {
		return cf.createConnection();
	}
	
	public Session createSession(Connection c) throws JMSException {
        return c.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}
	
	public Producer createProducerOnTopic(Session session, String topicId)
			throws JMSException {
		
		Topic topic = HornetQJMSClient.createTopic(topicId);
		MessageProducer producer = session.createProducer(topic);
		return new Producer(session, producer);
	}
	
	public MessageConsumer createConsumerOnTopic(Session session, String topicId)
			throws JMSException {
		
		Topic topic = HornetQJMSClient.createTopic(topicId);
		return session.createConsumer(topic);
	}

	public MessageConsumer createDurableConsumerOnTopic(Session session, String topicId, String clientId)
		throws JMSException {
		
		Topic topic = HornetQJMSClient.createTopic(topicId);
		return session.createDurableSubscriber(topic, clientId);
	}
	
}
