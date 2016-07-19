/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2016 Linagora
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
package org.obm.service.solr;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Event;


public class SolrEntityDaoListenerTest {

	private SolrEntityDaoListener testee;
	private IMocksControl control;
	private SolrHelper.Factory solrHelperFactory;
	private SolrHelper solrHelper;
	private AccessToken token;
	private Event event;
	private Contact contact;

	@Before
	public void setUp() {
		control = createControl();

		solrHelperFactory = control.createMock(SolrHelper.Factory.class);
		solrHelper = control.createMock(SolrHelper.class);
		token = control.createMock(AccessToken.class);
		event = control.createMock(Event.class);
		contact = control.createMock(Contact.class);
		
		expect(solrHelperFactory.createClient(token)).andReturn(solrHelper);
		
		testee = new SolrEntityDaoListener(solrHelperFactory);
	}
	
	@Test
	public void eventHasBeenCreatedCallShouldIndexEvent() {
		solrHelper.createOrUpdate(event);
		expectLastCall();
		
		control.replay();
		testee.eventHasBeenCreated(token, event);
		control.verify();
	}
	
	@Test
	public void eventHasBeenCreatedCallShouldFailWithoutPropagatingExpection() {
		solrHelper.createOrUpdate(event);
		expectLastCall().andThrow(new RuntimeException("unexpected"));
		
		control.replay();
		testee.eventHasBeenCreated(token, event);
		control.verify();
	}
	
	@Test
	public void eventHasBeenRemovedCallShouldIndexEvent() {
		solrHelper.delete(event);
		expectLastCall();
		
		control.replay();
		testee.eventHasBeenRemoved(token, event);
		control.verify();
	}
	
	@Test
	public void eventHasBeenRemovedCallShouldFailWithoutPropagatingExpection() {
		solrHelper.delete(event);
		expectLastCall().andThrow(new RuntimeException("unexpected"));
		
		control.replay();
		testee.eventHasBeenRemoved(token, event);
		control.verify();
	}
	
	@Test
	public void contactHasBeenCreatedCallShouldIndexEvent() {
		solrHelper.createOrUpdate(contact);
		expectLastCall();
		
		control.replay();
		testee.contactHasBeenCreated(token, contact);
		control.verify();
	}
	
	@Test
	public void contactHasBeenCreatedCallShouldFailWithoutPropagatingExpection() {
		solrHelper.createOrUpdate(contact);
		expectLastCall().andThrow(new RuntimeException("unexpected"));
		
		control.replay();
		testee.contactHasBeenCreated(token, contact);
		control.verify();
	}
	
	@Test
	public void contactHasBeenRemovedCallShouldIndexEvent() {
		solrHelper.delete(contact);
		expectLastCall();
		
		control.replay();
		testee.contactHasBeenRemoved(token, contact);
		control.verify();
	}
	
	@Test
	public void contactHasBeenRemovedCallShouldFailWithoutPropagatingExpection() {
		solrHelper.delete(contact);
		expectLastCall().andThrow(new RuntimeException("unexpected"));
		
		control.replay();
		testee.contactHasBeenRemoved(token, contact);
		control.verify();
	}
	
}
