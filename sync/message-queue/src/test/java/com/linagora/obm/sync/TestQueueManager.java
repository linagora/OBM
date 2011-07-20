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
		
		TextMessage messageReceived = (TextMessage) consumer.receive(1000);
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
		
		TextMessage messageReceived1 = (TextMessage) consumer.receive(10);
		consumerSession.close();

		writeMessageOnTopic(testText, topicName, queueManager);
		consumerSession = queueManager.createSession(connection);
		consumer = queueManager.createConsumerOnTopic(consumerSession, topicName);
		
		TextMessage messageReceived2 = (TextMessage) consumer.receive(10);
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
		
		TextMessage messageReceived = (TextMessage) consumer.receive(10);
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
		
		TextMessage messageReceived = (TextMessage) consumer.receive(10);
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
		TextMessage messageReceived = (TextMessage) consumer.receive(100);
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
		
		TextMessage messageReceived1 = (TextMessage) consumer1.receive(1000);
		TextMessage messageReceived2 = (TextMessage) consumer2.receive(1000);
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
