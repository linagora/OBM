package org.obm.annotations.transactional;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.TextMessage;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import com.google.guiceberry.GuiceBerryModule;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.linagora.obm.sync.Producer;

public class HornetQTransactionalModeTest {

	public static class Module extends AbstractModule {
		
		private MessageQueueModule messageQueueModule;

		public Module() throws Exception {
			messageQueueModule = new MessageQueueModule();
		}
		
		@Override
		protected void configure() {
			install(new TransactionalModule());
			install(messageQueueModule);
			install(new GuiceBerryModule());
		}
	}
	
	private final static long TIMEOUT = 1000;
	
	@Rule public final GuiceBerryRule guiceBerry =
			new GuiceBerryRule(Module.class);

	
	@Inject private TestClass xaMessageQueueInstance;
	@Inject private MessageConsumer consumer;	
	
	public static class TestClass {

		@Inject private Producer producer;
		
		@Transactional
		public void put(String text) throws JMSException {
			producer.write(text);
		}
		
		@Transactional
		public void putAndthrowException(String text) throws TestRollbackException, JMSException {
			put(text);
			throw new TestRollbackException();
		}
		
		
	}
	
	@Test
	public void testSimple() throws Exception {
		String testText = "test text";
		xaMessageQueueInstance.put(testText);
		TextMessage messageReceived = (TextMessage)consumer.receive(TIMEOUT);
		Assert.assertEquals(testText, messageReceived.getText());
	}
	
	@Ignore("OBMFULL-2887 : hornetq is not registered in our transaction manager")
	@Test(expected=TestRollbackException.class)
	public void testRollbackOnException() throws Exception {
		String testText = "rollback";
		try {
			xaMessageQueueInstance.putAndthrowException(testText);
		} catch (TestRollbackException e) {
			TextMessage messageReceived = (TextMessage)consumer.receive(TIMEOUT);
			Assert.assertNull(messageReceived.getText());
			throw e;
		}
	}
}
