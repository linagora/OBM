/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.ConfigurationService;
import org.obm.sync.book.Address;
import org.obm.sync.book.Contact;
import org.obm.sync.book.Email;
import org.obm.sync.book.InstantMessagingId;
import org.obm.sync.book.Phone;
import org.obm.sync.book.Website;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.RecurrenceDay;
import org.obm.sync.calendar.RecurrenceDays;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.solr.jms.Command;
import org.obm.sync.solr.jms.CommandConverter;
import org.obm.sync.solr.jms.SolrJmsQueue;

import com.linagora.obm.sync.QueueManager;


public class SolrManagerTest {

	private CommonsHttpSolrServer server;
	private SolrManager manager;
	private Lock lock;
	private Condition condition;
	private SolrRequest pingRequest;
	private Command<Integer> pingCommand;
	private QueueManager queueManager;
	private ConfigurationService configurationService;
	private CommandConverter converter = new CommandConverter() {
		@Override
		public <T extends Serializable> SolrRequest convert(Command<T> command) throws Exception {
			return pingRequest;
		}
	};
	
	@Before
	public void setUp() throws Exception {
		queueManager = new QueueManager();
		queueManager.start();
		
		configurationService = createMock(ConfigurationService.class);
		server = createMockBuilder(CommonsHttpSolrServer.class).addMockedMethod("ping").createStrictMock();
		
		expect(configurationService.solrCheckingInterval()).andReturn(10);
		replay(configurationService);
		
		pingCommand = new PingCommand();
		manager = new SolrManager(configurationService, queueManager, converter);
		lock = new ReentrantLock();
		condition = lock.newCondition();
		pingRequest = new PingSolrRequest(server, lock, condition);
	}
	
	@After
	public void tearDown() throws Exception {
		queueManager.stop();
	}
	
	@Test
	public void test_event_serialization() throws Exception {
		Event event = new Event();
		EventRecurrence recurrence = new EventRecurrence();
		Attendee attendee = UserAttendee.builder().email("Test").participationRole(ParticipationRole.REQ).build();
		
		// We only set the fields that aren't simple types to verify that they're all Serializable
		event.setUid(new EventObmId(1));
		event.setExtId(new EventExtId("1"));
		recurrence.setDays(new RecurrenceDays(RecurrenceDay.Monday));
		event.setRecurrence(recurrence);
		event.addAttendee(attendee);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		
		oos.writeObject(event);
		oos.close();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object object = ois.readObject();
		
		ois.close();
		
		Assertions.assertThat(object).isInstanceOf(Event.class);
	}
	
	@Test
	public void test_contact_serialization() throws Exception {
		Contact contact = new Contact();
		
		// We only set the fields that aren't simple types to verify that they're all Serializable
		contact.setUid(1);
		contact.setBirthdayId(new EventObmId(1));
		contact.addAddress("Test", new Address("", "", "", "", "", ""));
		contact.addEmail("Test", new Email(""));
		contact.addIMIdentifier("Test", new InstantMessagingId("", ""));
		contact.addWebsite(new Website("Test", ""));
		contact.addPhone("Test", new Phone(""));
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		
		oos.writeObject(contact);
		oos.close();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object object = ois.readObject();
		
		ois.close();
		
		Assertions.assertThat(object).isInstanceOf(Contact.class);
	}
	
	@Test(expected=IllegalStateException.class)
	public void request_rejected_if_queue_unknown() {
		manager.process(new PingCommand(null));
	}
	
	@Test
	public void solr_down_when_request_fails() throws Exception {        
		expect(server.ping()).andThrow(new IOException()).anyTimes();
		replay(server);
		
		manager.process(pingCommand);
		
		Assertions.assertThat(waitForRequestProcessing()).isTrue();
		Assertions.assertThat(manager.isSolrAvailable()).isFalse();
		verify(server);
	}
	
	@Test
	public void solr_remains_up_when_request_pass() throws Exception {
		expect(server.ping()).andReturn(null).anyTimes();
		replay(server);
		
		manager.process(pingCommand);
		
		Assertions.assertThat(waitForRequestProcessing()).isTrue();
		Assertions.assertThat(manager.isSolrAvailable()).isTrue();
		verify(server);
	}

	@Test
	public void request_processed_when_solr_is_up_again() throws Exception {
		expect(server.ping()).andThrow(new IOException()); // This will make SolR unavailable at first request
		expect(server.ping()).andReturn(null); // SolR should be back up at the second check
		expect(server.ping()).andReturn(null); // This one's for the actual request that must be processed once SolR is back up
		replay(server);
		
		manager.setSolrCheckingInterval(100);
		manager.process(pingCommand);
		
		Assertions.assertThat(waitForRequestProcessing()).isTrue();
		Assertions.assertThat(waitForRequestProcessing()).isTrue(); // Request will be processed a second time once SolR is back
		Assertions.assertThat(manager.isSolrAvailable()).isTrue();
		verify(server);
	}
	
	@Test
	public void request_not_processed_while_solr_down() throws Exception {
		expect(server.ping()).andThrow(new IOException()).anyTimes(); 
		replay(server);
		
		manager.setSolrAvailable(false);
		manager.process(pingCommand);
		
		Assertions.assertThat(waitForRequestProcessing()).isFalse();
		Assertions.assertThat(manager.isSolrAvailable()).isFalse();
		
		verify(server);
	}
	
	private boolean waitForRequestProcessing() throws InterruptedException {
		lock.lock();
		
		return condition.await(2, TimeUnit.SECONDS);
	}

	private static class PingCommand extends Command<Integer> {

		private SolrJmsQueue queue;

		public PingCommand() {
			this(SolrJmsQueue.CONTACT_CHANGES_QUEUE);
		}
		
		public PingCommand(SolrJmsQueue queue) {
			super(null, 0);
			
			this.queue = queue;
		}

		@Override
		public SolrJmsQueue getQueue() {
			return queue;
		}

		@Override
		public SolrService getSolrService() {
			return null;
		}

		@Override
		public SolrRequest asSolrRequest(CommonsHttpSolrServer server, IndexerFactory<Integer> factory) {
			return null;
		}

	}
}
