package org.obm.annotations.transactional;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.linagora.obm.sync.Producer;
import com.linagora.obm.sync.QueueManager;

public class MessageQueueModule extends AbstractModule {

	private final String TOPICPATH = "/topic/test";
	private final QueueManager queueManager;
	
	public MessageQueueModule() throws Exception {
		super();
		this.queueManager = constructQueueManager();
	}

	@Override
	protected void configure() {
		bind(QueueManager.class).toInstance(queueManager);
	}
	
	private QueueManager constructQueueManager() throws Exception {
		QueueManager queueManager = new QueueManager();
		queueManager.start();
		return queueManager;
	}
	
	@Provides @Singleton
	MessageConsumer provideMessageConsumer(QueueManager queueManager) throws JMSException {
		Connection connection = queueManager.createConnection();
		connection.start();
		Session consumerSession = queueManager.createSession(connection);
		return queueManager.createConsumerOnTopic(consumerSession, TOPICPATH);
	}
	
	@Provides @Singleton
	Producer provideMessageProducer(QueueManager queueManager) throws JMSException {
		Connection connection = queueManager.createConnection();
		Session session = queueManager.createSession(connection);
		return queueManager.createProducerOnTopic(session, TOPICPATH);
	}

	
}
