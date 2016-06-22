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
package org.obm.service.solr.jms;

import static fr.aliacom.obm.ToolBox.getDefaultObmDomain;
import static fr.aliacom.obm.ToolBox.getDefaultObmUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.obm.DateUtils.date;

import org.apache.solr.common.SolrInputDocument;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.obm.domain.dao.UserDao;
import org.obm.service.solr.jms.EventUpdateCommand;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.UserAttendee;
import org.obm.utils.ObmHelper;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;


public class EventUpdateCommandTest {

	private IMocksControl mocksControl; 
	private UserDao userDao;
	private ObmUser user;
	private ObmDomain domain;
	private Event eventToIndex;
 	private ObmHelper obmHelper;
 	private EventUpdateCommand.Factory testee;
	
	@Before
	public void setUp() {
		mocksControl = createControl();
		userDao = mocksControl.createMock(UserDao.class);
		obmHelper = mocksControl.createMock(ObmHelper.class);
		user = getDefaultObmUser();
		domain = getDefaultObmDomain();
		eventToIndex = buildEvent();
		testee = new EventUpdateCommand.Factory(userDao, obmHelper);
	}
	
	@After
	public void tearDown() {
		mocksControl.verify();
	}
	
	@Test
	public void buildDocumentWithoutOwnerUsesOwnerEmail() {
		expect(userDao.findUser("owner@domain.com", domain)).andReturn(user);
		mocksControl.replay();

		eventToIndex.setOwner("");
		
		SolrInputDocument solrDocument = testee.create(domain, user.getLogin(), eventToIndex).buildDocument();
		
		assertSolrDocumentIsBuilt(solrDocument);
	}
	
	@Test
	public void buildDocumentWithoutOwnerEmailUsesOwner() {
		eventToIndex.setOwnerEmail("");
		
		expect(userDao.findUser("owner", domain)).andReturn(user);
		mocksControl.replay();
		
		SolrInputDocument solrDocument = testee.create(domain, user.getLogin(), eventToIndex).buildDocument();

		
		assertSolrDocumentIsBuilt(solrDocument);
	}
	
	@Test
	public void buildDocumentWithOwnerAndOwnerEmailUsesOwnerEmail() {
		
		expect(userDao.findUser("owner@domain.com", domain)).andReturn(user);
		mocksControl.replay();
		
		SolrInputDocument solrDocument = testee.create(domain, user.getLogin(), eventToIndex).buildDocument();

		
		assertSolrDocumentIsBuilt(solrDocument);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void buildDocumentWithoutOwnerNorOwnerEmailFails() {
		eventToIndex.setOwner("");
		eventToIndex.setOwnerEmail("");
		
		expect(userDao.findUser("", domain)).andReturn(null);
		mocksControl.replay();
		
		testee.create(domain, user.getLogin(), eventToIndex).buildDocument();
	}
	
	@Test
	public void buildDocumentOfRecurrentPrivateBusyEvent() {
		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setKind(RecurrenceKind.daily);
		eventToIndex.setRecurrence(eventRecurrence);
		eventToIndex.setPrivacy(EventPrivacy.PRIVATE);
		
		
		expect(userDao.findUser("owner@domain.com", domain)).andReturn(user);
		mocksControl.replay();
		
		SolrInputDocument solrDocument = testee.create(domain, user.getLogin(), eventToIndex).buildDocument();
		
		assertSolrDocumentIsBuilt(solrDocument);
		assertThat(solrDocument.getField("is").getValues()).containsExactly("periodic", "busy", "private");
	}
	
	@Test
	public void buildDocumentOfAlldayFreeConfidentialEvent() {
		eventToIndex.setAllday(true);
		eventToIndex.setOpacity(EventOpacity.TRANSPARENT);
		eventToIndex.setPrivacy(EventPrivacy.CONFIDENTIAL);
		
		expect(userDao.findUser("owner@domain.com", domain)).andReturn(user);
		mocksControl.replay();
		
		SolrInputDocument solrDocument = testee.create(domain, user.getLogin(), eventToIndex).buildDocument();
		
		assertSolrDocumentIsBuilt(solrDocument);
		assertThat(solrDocument.getField("duration").getValue()).isEqualTo(86400);
		assertThat(solrDocument.getField("is").getValues()).containsExactly("allday", "free", "confidential");
	}
	
	@Test
	public void buildDocumentOfSimpleFreePublicEvent() {
		eventToIndex.setOpacity(EventOpacity.TRANSPARENT);
		
		expect(userDao.findUser("owner@domain.com", domain)).andReturn(user);
		mocksControl.replay();
		
		SolrInputDocument solrDocument = testee.create(domain, user.getLogin(), eventToIndex).buildDocument();
		
		assertSolrDocumentIsBuilt(solrDocument);
		assertThat(solrDocument.getField("is").getValues()).containsExactly("free");
	}
	
	
	private void assertSolrDocumentIsBuilt(SolrInputDocument solrDocument) {
		assertThat(solrDocument.getFieldNames())
			.containsOnly(
				"id", "timecreate", "timeupdate", "domain", "title", "location", 
				"date", "duration", "owner", "ownerId", "description", "with",
				"is");
		
		assertThat(solrDocument.getField("id").getValue()).isEqualTo(1234);
		assertThat(solrDocument.getField("timecreate").getValue()).isEqualTo(date("2012-12-12T13:14:15"));
		assertThat(solrDocument.getField("timeupdate").getValue()).isEqualTo(date("2012-12-12T13:14:15"));
		assertThat(solrDocument.getField("domain").getValue()).isEqualTo(1);
		assertThat(solrDocument.getField("title").getValue()).isEqualTo("title");
		assertThat(solrDocument.getField("location").getValue()).isEqualTo("location");
		assertThat(solrDocument.getField("date").getValue()).isEqualTo(date("2012-12-13T13:14:15"));
		assertThat(solrDocument.getField("ownerId").getValue()).isEqualTo(1);
		assertThat(solrDocument.getField("description").getValue()).isEqualTo("description");
		String[] with = {"Attendee One attendee.one@domain.com", "testgt@domain.com", "Owner owner@domain.com"};
		assertThat(solrDocument.getField("with").getValues().toArray()).isEqualTo(with);
		String[] owner = {"User", "Obm", "user", "user@test"};
		assertThat(solrDocument.getField("owner").getValues().toArray()).isEqualTo(owner);
	}
	
	private Event buildEvent() {
		Event event = new Event();
		
		event.setAllday(false);
		event.setInternalEvent(true);
		event.setSequence(0);
		event.setTimeCreate(date("2012-12-12T13:14:15"));
		event.setTimeUpdate(date("2012-12-12T13:14:15"));
		event.setExtId(new EventExtId("abc"));
		event.setOpacity(EventOpacity.OPAQUE);
		event.setTitle("title");
		event.setDescription("description");
		event.setOwnerEmail("owner@domain.com");
		event.setStartDate(date("2012-12-13T13:14:15"));
		event.setDuration(3600);
		event.setLocation("location");
		event.setAlert(300);
		event.setPriority(0);
		event.setOwner("owner");
		event.setPrivacy(EventPrivacy.PUBLIC);
		event.setUid(new EventObmId(1234));
		event.addAttendee(
				UserAttendee.builder()
					.displayName("Attendee One")
					.email("attendee.one@domain.com")
					.asAttendee()
					.participationRole(ParticipationRole.REQ)
					.participation(Participation.needsAction())
					.build());
		event.addAttendee(
				UserAttendee.builder()
					.email("testgt@domain.com")
					.asAttendee()
					.participationRole(ParticipationRole.REQ)
					.participation(Participation.needsAction())
					.build());
		event.addAttendee(
				UserAttendee.builder()
					.displayName("Owner")
					.email("owner@domain.com")
					.asOrganizer()
					.participationRole(ParticipationRole.REQ)
					.participation(Participation.accepted())
					.build());
		
		return event;
	}
	
}
