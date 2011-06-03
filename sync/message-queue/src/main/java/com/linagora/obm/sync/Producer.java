package com.linagora.obm.sync;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

public class Producer {

	private final Session session;
	private final MessageProducer producer;

	public Producer(Session session, MessageProducer producer) {
		this.session = session;
		this.producer = producer;
	}
	
	public void write(String message) throws JMSException {
		TextMessage messageSent = session.createTextMessage(message);
		producer.send(messageSent);
	}
	
	public void close() throws JMSException {
		producer.close();
	}
}
