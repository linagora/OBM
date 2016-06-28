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
package com.linagora.obm.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.io.FileUtils;
import org.hornetq.core.config.Configuration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestQueueManager {

	private static final String TOPIC_ID = "eventChanges";
	private static final String TOPIC_NAME = "/topic/eventChanges";
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private JMSServer jmsServer;
	private JMSClient jmsClient;
	private List<Connection> connections;
	private List<Session> sessions;
	private final static long TIMEOUT = 1000;

	
	@Before
	public void setUp() {
		startQueueManager();
		jmsClient = new JMSClient(false);
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
			.topic(TOPIC_ID, TOPIC_NAME)
			.build();
	}
	
	public static Configuration hornetQConfiguration() {
		return HornetQConfiguration.configuration()
				.enablePersistence(true)
				.enableSecurity(false)
				.connector(HornetQConfiguration.Connector.HornetQInVMCore)
				.acceptor(HornetQConfiguration.Acceptor.HornetQInVMCore)
				.build();
	}
	
	private void startQueueManager() {
		connections = new ArrayList<Connection>();
		sessions = new ArrayList<Session>();
		jmsServer = new JMSServer(hornetQConfiguration(), jmsConfiguration());
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
			jmsServer.stop();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private Connection createManagedConnection() throws JMSException {
		Connection connection = jmsClient.createConnection();
		connections.add(connection);
		return connection;
	}

	private Session createManagedSession(Connection connection)
			throws JMSException {
		Session consumerSession = jmsClient.createSession(connection);
		sessions.add(consumerSession);
		return consumerSession;
	}
	
	@Test
	public void testSimple() throws Exception {
		String testText = "test text";

		Connection connection = createManagedConnection();
		connection.start();
		Session consumerSession = createManagedSession(connection);
		MessageConsumer consumer = jmsClient.createConsumerOnTopic(consumerSession, TOPIC_ID);
		
		writeMessageOnTopic(testText, TOPIC_ID, jmsClient);
		
	
		TextMessage messageReceived = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived.getText());
	}

	@Test
	public void testClosedSession() throws Exception {
		String testText = "test text";

		Connection connection = createManagedConnection();
		connection.start();
		Session consumerSession = jmsClient.createSession(connection);
		MessageConsumer consumer = jmsClient.createConsumerOnTopic(consumerSession, TOPIC_ID);
		
		writeMessageOnTopic(testText, TOPIC_ID, jmsClient);
		
		TextMessage messageReceived1 = (TextMessage) consumer.receive(TIMEOUT);
		consumerSession.close();

		writeMessageOnTopic(testText, TOPIC_ID, jmsClient);
		consumerSession = createManagedSession(connection);
		consumer = jmsClient.createConsumerOnTopic(consumerSession, TOPIC_ID);
		
		TextMessage messageReceived2 = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived1.getText());
		Assert.assertNull(messageReceived2);
	}
	
	@Test
	public void testDurableSubscription() throws Exception {
		String testText = "test text";
		String clientId = "c1";

		Connection connection = jmsClient.createConnection();
		connection.setClientID(clientId);
		connection.start();
		Session consumerSession = jmsClient.createSession(connection);
		MessageConsumer consumer = jmsClient.createDurableConsumerOnTopic(consumerSession, TOPIC_ID, clientId);
		consumerSession.close();
		connection.close();
		
		writeMessageOnTopic(testText, TOPIC_ID, jmsClient);
		
		Connection connection2 = createManagedConnection();
		connection2.setClientID(clientId);
		connection2.start();

		consumerSession = jmsClient.createSession(connection2);
		consumer = jmsClient.createDurableConsumerOnTopic(consumerSession, TOPIC_ID, clientId);
		
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
		MessageConsumer consumer = jmsClient.createDurableConsumerOnTopic(consumerSession, TOPIC_ID, clientId);
		
		writeMessageOnTopic(testText, TOPIC_ID, jmsClient);
		shutdownQueueManager();
		startQueueManager();
		
		Connection connection2 = createManagedConnection();
		connection2.setClientID(clientId);
		connection2.start();

		consumerSession = createManagedSession(connection2);
		consumer = jmsClient.createDurableConsumerOnTopic(consumerSession, TOPIC_ID, clientId);
		
		TextMessage messageReceived = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived.getText());
	}
	
	@Test
	public void testConsumeLater() throws Exception {
		String testText = "test text";

		writeMessageOnTopic(testText, TOPIC_ID, jmsClient);

		Connection connection = createManagedConnection();
		connection.start();
		Session consumerSession = createManagedSession(connection);
		MessageConsumer consumer = jmsClient.createConsumerOnTopic(consumerSession, TOPIC_ID);
		TextMessage messageReceived = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertNull(messageReceived);
	}
	
	@Test
	public void testTwoConsumers() throws Exception {
		String testText = "test text";

		Connection connection = createManagedConnection();
		connection.start();
		Session consumerSession1 = createManagedSession(connection);
		MessageConsumer consumer1 = jmsClient.createConsumerOnTopic(consumerSession1, TOPIC_ID);
		
		Session consumerSession2 = createManagedSession(connection);
		MessageConsumer consumer2 = jmsClient.createConsumerOnTopic(consumerSession2, TOPIC_ID);
		
		writeMessageOnTopic(testText, TOPIC_ID, jmsClient);
		
		TextMessage messageReceived1 = (TextMessage) consumer1.receive(TIMEOUT);
		TextMessage messageReceived2 = (TextMessage) consumer2.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived1.getText());
		Assert.assertEquals(testText, messageReceived2.getText());
	}

	private void writeMessageOnTopic(String testText, String topicId,
			JMSClient queueManager) throws JMSException, TimeoutException {
		
		Connection connection = createManagedConnection();
		Session producerSession = queueManager.createSession(connection);
		Producer producer = queueManager.createProducerOnTopic(producerSession, topicId);
		producer.write(testText);
	}

}
