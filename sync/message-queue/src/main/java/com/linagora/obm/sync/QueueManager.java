package com.linagora.obm.sync;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hornetq.jms.server.embedded.EmbeddedJMS;

public class QueueManager {

	private static final Log logger = LogFactory.getLog(QueueManager.class);
	private final EmbeddedJMS jmsServer;
	private boolean started;
	private ConnectionFactory cf;

	public QueueManager() {
		super();
		jmsServer = new EmbeddedJMS();
		started = false;
	}
		
	public synchronized void start() throws Exception {
		if (started) {
			throw new IllegalStateException(this.getClass().getName() + " can't be started twice");
		}
        jmsServer.start();
        logger.info("Embedded JMS Server Started");
        cf = (ConnectionFactory)jmsServer.lookup("ConnectionFactory");
        started = true;
	}
	
	public synchronized void stop() throws Exception {
		if (started) {
			jmsServer.stop();
			logger.info("Embedded JMS Server Stopped");
			started = false;
		}
	}

	public Connection createConnection() throws JMSException {
		Connection connection = cf.createConnection();
        return connection;
	}
	
	public Session createSession(Connection c) throws JMSException {
        Session session = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
        return session;
	}
	
	public Producer createProducerOnTopic(Session session, String topicPath) throws JMSException {
		Topic queue = (Topic)jmsServer.lookup(topicPath);
		MessageProducer producer = session.createProducer(queue);
		return new Producer(session, producer);
	}
	
	public MessageConsumer createConsumerOnTopic(Session session, String topicPath) throws JMSException {
		Topic queue = (Topic)jmsServer.lookup(topicPath);
		MessageConsumer consumer = session.createConsumer(queue);
		return consumer;
	}

	public MessageConsumer createDurableConsumerOnTopic(Session session, String topicPath, String clientId)
		throws JMSException {
		
		Topic queue = (Topic)jmsServer.lookup(topicPath);
		MessageConsumer consumer = session.createDurableSubscriber(queue, clientId);
		return consumer;
	}
	
}
