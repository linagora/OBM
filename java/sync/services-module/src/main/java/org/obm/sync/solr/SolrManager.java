/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.solr;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.obm.configuration.ConfigurationService;
import org.obm.sync.LifecycleListener;
import org.obm.sync.solr.jms.Command;
import org.obm.sync.solr.jms.CommandConverter;
import org.obm.sync.solr.jms.SolrJmsQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linagora.obm.sync.Producer;
import com.linagora.obm.sync.QueueManager;

@Singleton
public class SolrManager implements LifecycleListener {
	private static final String CONTACT_CLIENT_ID = "solrManagerContactClientId";
	private static final String EVENT_CLIENT_ID = "solrManagerEventClientId";
	private static final String CONNECTION_CLIENT_ID = "solrManagerConnectionClientId";
	
	private boolean solrAvailable;
	private Timer checker;
	private int solrCheckingInterval;
	private CommandConverter commandConverter;
	private CommonsHttpSolrServer failingSolrServer;

	private final Connection jmsConnection;
	private final Session jmsProducerSession;
	private final Session jmsContactConsumerSession;
	private final Session jmsEventConsumerSession;
	private final MessageConsumer jmsEventConsumer;
	private final MessageConsumer jmsContactConsumer;
	private final EnumMap<SolrJmsQueue, Producer> queueNameToProducerMap;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	@VisibleForTesting
	protected SolrManager(ConfigurationService configurationService, QueueManager queueManager, CommandConverter commandConverter) throws JMSException {
		this.commandConverter = commandConverter;
		solrCheckingInterval = configurationService.solrCheckingInterval() * 1000;
		queueNameToProducerMap = new EnumMap<SolrJmsQueue, Producer>(SolrJmsQueue.class);
		
		jmsConnection = queueManager.createConnection();
		jmsConnection.setClientID(CONNECTION_CLIENT_ID);
		
		jmsProducerSession = queueManager.createSession(jmsConnection);
		jmsContactConsumerSession = jmsConnection.createSession(true, Session.SESSION_TRANSACTED);
		jmsEventConsumerSession = jmsConnection.createSession(true, Session.SESSION_TRANSACTED);
		jmsContactConsumer = queueManager.createDurableConsumerOnTopic(jmsContactConsumerSession, SolrJmsQueue.CONTACT_CHANGES_QUEUE.getName(), CONTACT_CLIENT_ID);
		jmsContactConsumer.setMessageListener(new Listener(jmsContactConsumerSession));
		jmsEventConsumer = queueManager.createDurableConsumerOnTopic(jmsEventConsumerSession, SolrJmsQueue.CALENDAR_CHANGES_QUEUE.getName(), EVENT_CLIENT_ID);
		jmsEventConsumer.setMessageListener(new Listener(jmsEventConsumerSession));
		queueNameToProducerMap.put(SolrJmsQueue.CONTACT_CHANGES_QUEUE, queueManager.createProducerOnTopic(jmsProducerSession, SolrJmsQueue.CONTACT_CHANGES_QUEUE.getName()));
		queueNameToProducerMap.put(SolrJmsQueue.CALENDAR_CHANGES_QUEUE, queueManager.createProducerOnTopic(jmsProducerSession, SolrJmsQueue.CALENDAR_CHANGES_QUEUE.getName()));

		// We are supposedly available at startup for two reasons:
		// 1. This is backwards compatible with the previous implementation
		// 2. We need a SolR server instance in order to check its status, so we need at least one request in the queue
		setSolrAvailable(true);
	}

	public synchronized boolean isSolrAvailable() {
		return solrAvailable;
	}

	@VisibleForTesting
	protected synchronized void setSolrAvailable(boolean solrAvailable) {
		if (this.solrAvailable == solrAvailable) {
			return;
		}
		
		this.solrAvailable = solrAvailable;

		try {
			if (solrAvailable) {
				if (checker != null) {
					checker.cancel();
				}

				// (Re)Starting the JMS connection will (re)start message delivery to the listeners
				jmsConnection.start();
			}
			else {
				(checker = new Timer()).scheduleAtFixedRate(new Checker(), solrCheckingInterval, solrCheckingInterval);
			}
		}
		catch (Exception e) {
			logger.error("Couldn't change state to '" + solrAvailable + "'.", e);
		}
	}

	@VisibleForTesting
	protected void setSolrCheckingInterval(int solrCheckingInterval) {
		this.solrCheckingInterval = solrCheckingInterval;
	}

	public void process(Command<?> command) {
		try {
			SolrJmsQueue queue = command.getQueue();
			Producer producer = queueNameToProducerMap.get(queue);
			
			if (producer == null) {
				throw new IllegalArgumentException("Unknown JMS queue '" + queue + "'.");
			}
			
			producer.send(jmsProducerSession.createObjectMessage(command));
		}
		catch (Exception e) {
			throw new IllegalStateException("Couldn't create JMS message for SolR request", e);
		}
	}

	private class Checker extends TimerTask {
		@Override
		public void run() {
			// We should have stored the failing SolR server in the Indexing Thread
			if (failingSolrServer != null) {
				try {
					failingSolrServer.ping();
					failingSolrServer = null;

					logger.info("SolR server is available, resuming indexing.");
					setSolrAvailable(true);
				}
				catch (Exception e) {
					// SolR is still unavailable, nothing to do
				}
			}
		}
	}
	
	private class Listener implements MessageListener {
		private final Session session;
		
		private Listener(Session session) {
			this.session = session;
		}

		@Override
		public void onMessage(Message message) {
			try {
				// SolR has been marked unavailable, we should stop message processing
				// This is handled directly in the MessageListener to not cause deadlocks if called from multiple threads 
				if (!isSolrAvailable()) {
					session.rollback();
					jmsConnection.stop();
					
					return;
				}
				
				Command<? extends Serializable> command = (Command<?>) ((ObjectMessage) message).getObject();
				SolrRequest request = commandConverter.convert(command);
				
				try {
					request.run();
					session.commit();
				}
				catch (Exception e) {
					session.rollback();
					failingSolrServer = request.getServer();
					setSolrAvailable(false);

					logger.warn("SolR server is unavailable, last request is kept in the queue.", e);
				} finally {
					request.postProcess();
				}
			}
			catch (Throwable e) {
				logger.error("Couldn't process JMS message", e);
			}
		}
		

	}
	
	public void shutdown() throws Exception {
		jmsConnection.close();
	}
}
