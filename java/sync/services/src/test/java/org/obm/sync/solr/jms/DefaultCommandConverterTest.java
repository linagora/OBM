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
package org.obm.sync.solr.jms;

import static org.easymock.EasyMock.*;

import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.obm.locator.store.LocatorService;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.solr.ContactIndexer;
import org.obm.sync.solr.EventIndexer;
import org.obm.sync.solr.IndexerFactory;
import org.obm.sync.solr.Remover;
import org.obm.sync.solr.SolrRequest;
import org.obm.sync.solr.jms.Command.Type;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.domain.ObmDomain;


public class DefaultCommandConverterTest {
	private DefaultCommandConverter converter;
	private LocatorService locatorClient;
	private ContactIndexer.Factory contactIndexerFactory;
	private EventIndexer.Factory eventIndexerFactory;
	private ContactIndexer contactIndexer;
	private EventIndexer eventIndexer;
	private Contact contact;
	private Event event;
	private ObmDomain domain;
	
	@Before
	public void setUp() {
		contact = new Contact();
		contact.setUid(1);
		
		event = new Event();
		event.setUid(new EventObmId(1));
		
		domain = ToolBox.getDefaultObmDomain();
		locatorClient = createMock(LocatorService.class);
		
		contactIndexer = createMockBuilder(ContactIndexer.class).createStrictMock();
		contactIndexerFactory = createMockBuilder(ContactIndexer.Factory.class).addMockedMethod("createIndexer", CommonsHttpSolrServer.class, ObmDomain.class, Contact.class).createMock();
		expect(contactIndexerFactory.createIndexer(isA(CommonsHttpSolrServer.class), eq(domain), eq(contact))).andReturn(contactIndexer).anyTimes();
		
		eventIndexer = createMockBuilder(EventIndexer.class).createStrictMock();
		eventIndexerFactory = createMockBuilder(EventIndexer.Factory.class).addMockedMethod("createIndexer", CommonsHttpSolrServer.class, ObmDomain.class, Event.class).createMock();
		expect(eventIndexerFactory.createIndexer(isA(CommonsHttpSolrServer.class), eq(domain), eq(event))).andReturn(eventIndexer).anyTimes();
		
		replay(eventIndexerFactory, contactIndexerFactory);
		
		converter = new DefaultCommandConverter(locatorClient, contactIndexerFactory, eventIndexerFactory);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testConvertDeleteFailsIfSolrServiceUnknown() throws Exception {
		converter.convert(new UnkownCommand(Type.DELETE));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testConvertCreateOrUpdateFailsIfSolrServiceUnknown() throws Exception {
		converter.convert(new UnkownCommand(Type.CREATE_OR_UPDATE));
	}
	
	@Test
	public void testConvertDeleteContactCommand() throws Exception {
		SolrRequest request = converter.convert(new ContactCommand(domain, contact, Type.DELETE));
		
		Assertions.assertThat(request).isInstanceOf(Remover.class);
	}
	
	@Test
	public void testConvertDeleteEventCommand() throws Exception {
		SolrRequest request = converter.convert(new EventCommand(domain, event, Type.DELETE));
		
		Assertions.assertThat(request).isInstanceOf(Remover.class);
	}
	
	@Test
	public void testConvertCreateOrUpdateContactCommand() throws Exception {
		SolrRequest request = converter.convert(new ContactCommand(domain, contact, Type.CREATE_OR_UPDATE));
		
		Assertions.assertThat(request).isInstanceOf(ContactIndexer.class);
	}
	
	@Test
	public void testConvertCreateOrUpdateEventCommand() throws Exception {
		SolrRequest request = converter.convert(new EventCommand(domain, event, Type.CREATE_OR_UPDATE));
		
		Assertions.assertThat(request).isInstanceOf(EventIndexer.class);
	}
	
	private static class UnkownCommand extends Command<Integer> {
		private UnkownCommand(Type type) {
			super(null, 0, type);
		}

		@Override
		public String getQueueName() {
			return null;
		}

		@Override
		public String getSolrServiceName() {
			return "unknown/service";
		}

		@Override
		public SolrRequest asSolrRequest(CommonsHttpSolrServer server, IndexerFactory<Integer> factory) {
			return null;
		}
	}

}
