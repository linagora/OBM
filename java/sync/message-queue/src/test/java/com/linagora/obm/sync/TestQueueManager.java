/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.io.FileUtils;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.remoting.impl.invm.InVMAcceptorFactory;
import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(SlowFilterRunner.class) @Slow
public class TestQueueManager {

	private static final String TOPIC = "/topic/eventChanges";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private QueueManager queueManager;
	private List<Connection> connections;
	private List<Session> sessions;
	private final static long TIMEOUT = 1000;
	
	@Before
	public void setUp() throws Exception {
		startQueueManager();
	}

	private static JMSConfiguration jmsConfiguration() {
		return 
			HornetQConfigurationBuilder.jmsConfiguration()
			.connectionFactory(
					HornetQConfigurationBuilder.connectionFactoryConfigurationBuilder()
					.name("ConnectionFactory")
					.connector("in-vm")
					.binding("ConnectionFactory")
					.build())
			.topic("eventChanges", TOPIC)
			.build();
	}
	
	public static Configuration hornetQConfiguration() {
		return HornetQConfigurationBuilder.configuration()
				.enablePersistence(true)
				.enableSecurity(false)
				.connector(HornetQConfigurationBuilder.connectorBuilder()
						.factory(InVMConnectorFactory.class)
						.name("in-vm")
						.build())
				.connector(HornetQConfigurationBuilder.connectorBuilder()
						.factory(NettyConnectorFactory.class)
						.name("netty")
						.build())
				.acceptor(HornetQConfigurationBuilder.acceptorBuilder()
						.factory(InVMAcceptorFactory.class)
						.name("in-vm")
						.build())
				.acceptor(HornetQConfigurationBuilder.acceptorBuilder()
						.factory(NettyAcceptorFactory.class)
						.name("netty")
						.param("protocol", "stomp")
						.param("port", 61613)
						.build())
				.build();
	}
	
	private void startQueueManager() throws Exception {
		connections = new ArrayList<Connection>();
		sessions = new ArrayList<Session>();
		queueManager = new QueueManager(hornetQConfiguration(), jmsConfiguration());
		queueManager.start();
	}

	@After
	public void cleanUp() throws Exception {
		shutdownQueueManager();
		FileUtils.deleteDirectory(new File("data"));
	}

	private void shutdownQueueManager() {
		for (Session s: sessions) {
			try {
				s.close();
			} catch (JMSException e) {
				logger.error(e.getMessage(), e);
			}
		}
		for (Connection c: connections) {
			try {
				c.stop();
			} catch (JMSException e) {
				logger.error(e.getMessage(), e);
			}
			try {
				c.close();
			} catch (JMSException e) {
				logger.error(e.getMessage(), e);
			}
		}
		try {
			queueManager.stop();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Connection createManagedConnection() throws JMSException {
		Connection connection = queueManager.createConnection();
		connections.add(connection);
		return connection;
	}

	private Session createManagedSession(Connection connection)
			throws JMSException {
		Session consumerSession = queueManager.createSession(connection);
		sessions.add(consumerSession);
		return consumerSession;
	}
	
	@Test
	public void testSimple() throws Exception {
		String testText = "test text";

		Connection connection = createManagedConnection();
		connection.start();
		Session consumerSession = createManagedSession(connection);
		MessageConsumer consumer = queueManager.createConsumerOnTopic(consumerSession, TOPIC);
		
		writeMessageOnTopic(testText, TOPIC, queueManager);
		
	
		TextMessage messageReceived = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived.getText());
	}

	@Test
	public void testClosedSession() throws Exception {
		String testText = "test text";

		Connection connection = createManagedConnection();
		connection.start();
		Session consumerSession = queueManager.createSession(connection);
		MessageConsumer consumer = queueManager.createConsumerOnTopic(consumerSession, TOPIC);
		
		writeMessageOnTopic(testText, TOPIC, queueManager);
		
		TextMessage messageReceived1 = (TextMessage) consumer.receive(TIMEOUT);
		consumerSession.close();

		writeMessageOnTopic(testText, TOPIC, queueManager);
		consumerSession = createManagedSession(connection);
		consumer = queueManager.createConsumerOnTopic(consumerSession, TOPIC);
		
		TextMessage messageReceived2 = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived1.getText());
		Assert.assertNull(messageReceived2);
	}
	
	@Test
	public void testDurableSubscription() throws Exception {
		String testText = "test text";
		String clientId = "c1";

		Connection connection = queueManager.createConnection();
		connection.setClientID(clientId);
		connection.start();
		Session consumerSession = queueManager.createSession(connection);
		MessageConsumer consumer = queueManager.createDurableConsumerOnTopic(consumerSession, TOPIC, clientId);
		consumerSession.close();
		connection.close();
		
		writeMessageOnTopic(testText, TOPIC, queueManager);
		
		Connection connection2 = createManagedConnection();
		connection2.setClientID(clientId);
		connection2.start();

		consumerSession = queueManager.createSession(connection2);
		consumer = queueManager.createDurableConsumerOnTopic(consumerSession, TOPIC, clientId);
		
		TextMessage messageReceived = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived.getText());
	}
	
	@Test
	public void testDurableSubscription2() throws Exception {
		String testText = "test text";
		String clientId = "c1";

		Connection connection = createManagedConnection();
		connection.setClientID(clientId);
		connection.start();
		Session consumerSession = createManagedSession(connection);
		MessageConsumer consumer = queueManager.createDurableConsumerOnTopic(consumerSession, TOPIC, clientId);
		
		writeMessageOnTopic(testText, TOPIC, queueManager);
		shutdownQueueManager();
		startQueueManager();
		
		Connection connection2 = createManagedConnection();
		connection2.setClientID(clientId);
		connection2.start();

		consumerSession = createManagedSession(connection2);
		consumer = queueManager.createDurableConsumerOnTopic(consumerSession, TOPIC, clientId);
		
		TextMessage messageReceived = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived.getText());
	}
	
	@Test
	public void testConsumeLater() throws Exception {
		String testText = "test text";

		writeMessageOnTopic(testText, TOPIC, queueManager);

		Connection connection = createManagedConnection();
		connection.start();
		Session consumerSession = createManagedSession(connection);
		MessageConsumer consumer = queueManager.createConsumerOnTopic(consumerSession, TOPIC);
		TextMessage messageReceived = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertNull(messageReceived);
	}
	
	@Test
	public void testTwoConsumers() throws Exception {
		String testText = "test text";

		Connection connection = createManagedConnection();
		connection.start();
		Session consumerSession1 = createManagedSession(connection);
		MessageConsumer consumer1 = queueManager.createConsumerOnTopic(consumerSession1, TOPIC);
		
		Session consumerSession2 = createManagedSession(connection);
		MessageConsumer consumer2 = queueManager.createConsumerOnTopic(consumerSession2, TOPIC);
		
		writeMessageOnTopic(testText, TOPIC, queueManager);
		
		TextMessage messageReceived1 = (TextMessage) consumer1.receive(TIMEOUT);
		TextMessage messageReceived2 = (TextMessage) consumer2.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived1.getText());
		Assert.assertEquals(testText, messageReceived2.getText());
	}

	private void writeMessageOnTopic(String testText, String topicName,
			QueueManager queueManager) throws JMSException {
		
		Connection connection = createManagedConnection();
		Session producerSession = queueManager.createSession(connection);
		Producer producer = queueManager.createProducerOnTopic(producerSession, topicName);
		producer.write(testText);
	}

}
