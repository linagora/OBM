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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

public class TestQueueManager {

	private QueueManager queueManager;
	private List<Connection> connections;
	private final static long TIMEOUT = 1000;
	
	@Before
	public void setUp() throws Exception {
		connections = new ArrayList<Connection>();
		queueManager = new QueueManager();
		queueManager.start();
	}

	@After
	public void cleanUp() throws Exception {
		for (Connection c: connections) {
			c.stop();
		}
		queueManager.stop();
		Files.deleteRecursively(new File("data"));
	}

	private Connection createManagedConnection() throws JMSException {
		Connection connection = queueManager.createConnection();
		connections.add(connection);
		return connection;
	}
	
	@Test
	public void testSimple() throws Exception {
		String testText = "test text";
		String topicName = "/topic/eventChanges";

		Connection connection = createManagedConnection();
		connection.start();
		Session consumerSession = queueManager.createSession(connection);
		MessageConsumer consumer = queueManager.createConsumerOnTopic(consumerSession, topicName);
		
		writeMessageOnTopic(testText, topicName, queueManager);
		
	
		TextMessage messageReceived = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived.getText());
	}

	@Test
	public void testClosedSession() throws Exception {
		String testText = "test text";
		String topicName = "/topic/eventChanges";

		Connection connection = createManagedConnection();
		connection.start();
		Session consumerSession = queueManager.createSession(connection);
		MessageConsumer consumer = queueManager.createConsumerOnTopic(consumerSession, topicName);
		
		writeMessageOnTopic(testText, topicName, queueManager);
		
		TextMessage messageReceived1 = (TextMessage) consumer.receive(TIMEOUT);
		consumerSession.close();

		writeMessageOnTopic(testText, topicName, queueManager);
		consumerSession = queueManager.createSession(connection);
		consumer = queueManager.createConsumerOnTopic(consumerSession, topicName);
		
		TextMessage messageReceived2 = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived1.getText());
		Assert.assertNull(messageReceived2);
	}
	
	@Test
	public void testDurableSubscription() throws Exception {
		String testText = "test text";
		String topicName = "/topic/eventChanges";
		String clientId = "c1";

		Connection connection = queueManager.createConnection();
		connection.setClientID(clientId);
		connection.start();
		Session consumerSession = queueManager.createSession(connection);
		MessageConsumer consumer = queueManager.createDurableConsumerOnTopic(consumerSession, topicName, clientId);
		consumerSession.close();
		connection.close();
		
		writeMessageOnTopic(testText, topicName, queueManager);
		
		Connection connection2 = createManagedConnection();
		connection2.setClientID(clientId);
		connection2.start();

		consumerSession = queueManager.createSession(connection2);
		consumer = queueManager.createDurableConsumerOnTopic(consumerSession, topicName, clientId);
		
		TextMessage messageReceived = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived.getText());
	}
	
	@Test
	public void testDurableSubscription2() throws Exception {
		String testText = "test text";
		String topicName = "/topic/eventChanges";
		String clientId = "c1";

		Connection connection = queueManager.createConnection();
		connection.setClientID(clientId);
		connection.start();
		Session consumerSession = queueManager.createSession(connection);
		MessageConsumer consumer = queueManager.createDurableConsumerOnTopic(consumerSession, topicName, clientId);
		consumerSession.close();
		connection.close();
		
		writeMessageOnTopic(testText, topicName, queueManager);
		queueManager.stop();
		queueManager.start();
		
		Connection connection2 = createManagedConnection();
		connection2.setClientID(clientId);
		connection2.start();

		consumerSession = queueManager.createSession(connection2);
		consumer = queueManager.createDurableConsumerOnTopic(consumerSession, topicName, clientId);
		
		TextMessage messageReceived = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived.getText());
	}
	
	@Test
	public void testConsumeLater() throws Exception {
		String testText = "test text";
		String topicName = "/topic/eventChanges";

		writeMessageOnTopic(testText, topicName, queueManager);

		Connection connection = createManagedConnection();
		connection.start();
		Session consumerSession = queueManager.createSession(connection);
		MessageConsumer consumer = queueManager.createConsumerOnTopic(consumerSession, topicName);
		TextMessage messageReceived = (TextMessage) consumer.receive(TIMEOUT);
		Assert.assertNull(messageReceived);
	}
	
	@Test
	public void testTwoConsumers() throws Exception {
		String testText = "test text";
		String topicName = "/topic/eventChanges";

		Connection connection = createManagedConnection();
		connection.start();
		Session consumerSession1 = queueManager.createSession(connection);
		MessageConsumer consumer1 = queueManager.createConsumerOnTopic(consumerSession1, topicName);
		
		Session consumerSession2 = queueManager.createSession(connection);
		MessageConsumer consumer2 = queueManager.createConsumerOnTopic(consumerSession2, topicName);
		
		writeMessageOnTopic(testText, topicName, queueManager);
		
		TextMessage messageReceived1 = (TextMessage) consumer1.receive(TIMEOUT);
		TextMessage messageReceived2 = (TextMessage) consumer2.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived1.getText());
		Assert.assertEquals(testText, messageReceived2.getText());
	}

	private void writeMessageOnTopic(String testText, String topicName,
			QueueManager queueManager) throws JMSException {
		
		Connection connection = queueManager.createConnection();
		Session producerSession = queueManager.createSession(connection);
		Producer producer = queueManager.createProducerOnTopic(producerSession, topicName);
		producer.write(testText);
	}

}
