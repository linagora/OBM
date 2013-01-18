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
package org.obm.sync.solr;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import org.apache.solr.common.SolrInputDocument;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.DateUtils;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.ParticipationRole;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;


public class EventIndexerTest {

	private IMocksControl mocksControl; 
	private EventIndexer eventIndexer;
	private UserDao userDao;
	private ObmUser user;
	private ObmDomain domain;
	
	@Before
	public void setUp() {
		mocksControl = createControl();
		userDao = mocksControl.createMock(UserDao.class);
		user = ToolBox.getDefaultObmUser();
		domain = ToolBox.getDefaultObmDomain();
	}
	
	@After
	public void tearDown() {
		mocksControl.verify();
	}
	
	@Test
	public void buildDocumentWithoutOwnerUsesOwnerEmail() {
		Event event = buildEvent("", "owner@domain.com");
		
		expect(userDao.findUser(eq("owner@domain.com"), eq(domain))).andReturn(user);
		mocksControl.replay();
		
		assertSolrDocumentIsBuilt(domain, event);
	}
	
	@Test
	public void buildDocumentWithoutOwnerEmailUsesOwner() {
		Event event = buildEvent("owner", "");
		
		expect(userDao.findUser(eq("owner"), eq(domain))).andReturn(user);
		mocksControl.replay();
		
		assertSolrDocumentIsBuilt(domain, event);
	}
	
	@Test
	public void buildDocumentWithOwnerAndOwnerEmailUsesOwnerEmail() {
		Event event = buildEvent("owner", "owner@domain.com");
		
		expect(userDao.findUser(eq("owner@domain.com"), eq(domain))).andReturn(user);
		mocksControl.replay();
		
		assertSolrDocumentIsBuilt(domain, event);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void buildDocumentWithoutOwnerNorOwnerEmailFails() {
		Event event = buildEvent("", "");
		
		expect(userDao.findUser(eq(""), eq(domain))).andReturn(null);
		mocksControl.replay();
		
		assertSolrDocumentIsBuilt(domain, event);
	}
	
	@Test
	public void indexEventWithoutOwnerNorOwnerEmailSucceeds() throws Exception {
		Event event = buildEvent("", "");
		
		expect(userDao.findUser(eq(""), eq(domain))).andReturn(null);
		mocksControl.replay();
		
		eventIndexer = new EventIndexer(null, null, userDao, domain, event);
		
		assertThat(eventIndexer.doIndex()).isTrue();
	}
	
	private void assertSolrDocumentIsBuilt(ObmDomain domain, Event event) {
		eventIndexer = new EventIndexer(null, null, userDao, domain, event);
		
		SolrInputDocument solrDocument = eventIndexer.buildDocument();
		
		assertThat(solrDocument.getFieldNames()).containsOnly(
				"id", "timecreate", "timeupdate", "domain", "title", "location", 
				"date", "duration", "owner", "ownerId", "description", "with",
				"is");
	}
	
	private Event buildEvent(String owner, String ownerEmail) {
		Event event = new Event();
		
		event.setAllday(false);
		event.setInternalEvent(true);
		event.setSequence(0);
		event.setTimeCreate(DateUtils.date("2012-12-12T13:14:15"));
		event.setTimeUpdate(DateUtils.date("2012-12-12T13:14:15"));
		event.setExtId(new EventExtId(EventExtId.generateUid().toString()));
		event.setOpacity(EventOpacity.OPAQUE);
		event.setTitle("Grand saut ?");
		event.setDescription("A lot of fun");
		event.setOwnerEmail(ownerEmail);
		event.setStartDate(DateUtils.date("2012-12-13T13:14:15"));
		event.setDuration(3600);
		event.setLocation("Mare Asthme");
		event.setAlert(300);
		event.setPriority(0);
		event.setOwner(owner);
		event.setPrivacy(EventPrivacy.PUBLIC);
		event.setUid(new EventObmId(1234));
		event.addAttendee(
				Attendee.builder().displayName("Attendee One")
					.email("attendee.one@domain.com").asAttendee()
					.participationRole(ParticipationRole.REQ).participation(Participation.needsAction()).build());
		event.addAttendee(
				Attendee.builder().displayName("Test GT")
					.email("testgt@domain.com").asAttendee()
					.participationRole(ParticipationRole.REQ).participation(Participation.needsAction()).build());
		event.addAttendee(
				Attendee.builder().displayName("Owner")
					.email("owner@domain.com").asOrganizer()
					.participationRole(ParticipationRole.REQ).participation(Participation.accepted()).build());
		
		return event;
	}
	
}
