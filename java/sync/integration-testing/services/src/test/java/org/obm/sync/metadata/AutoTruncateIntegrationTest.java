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
package org.obm.sync.metadata;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.util.UUID;

import javax.naming.NoPermissionException;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.sync.NotAllowedException;
import org.obm.sync.ObmSyncIntegrationTest;
import org.obm.sync.arquillian.ManagedTomcatSlowGuiceArquillianRunner;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.EventAlreadyExistException;
import org.obm.sync.auth.EventNotFoundException;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.UnidentifiedAttendee;
import org.obm.sync.exception.ContactNotFoundException;

@Slow
@RunWith(ManagedTomcatSlowGuiceArquillianRunner.class)
public class AutoTruncateIntegrationTest extends ObmSyncIntegrationTest {
	
	private static final int ADDRESSBOOK_ID = 1;
	
	private static final int MAX_BYTES_256 = 256;
	private static final int MAX_BYTES_255 = 255;
	private static final int MAX_BYTES_64 = 64;
	private static final int MAX_BYTES_32 = 32;
	private static final int MAX_BYTES_16 = 16;
	
	private static final String BIGFIELD = 
			"Lorem ipsum dolor sit amet, nonummy ligula volutpat hac integer nonummy. Suspendisse ultricies, " +
			"congue etiam tellus, erat libero, nulla eleifend, mauris pellentesque. Suspendisse integer praesent vel, " +
			"integer gravida mauris, fringilla vehicula lacinia non Lorem ipsum dolor sit amet, nonummy ligula volutpat " +
			"hac integer nonummy. Suspendisse ultricies, congue etiam tellus, erat libero, nulla eleifend, mauris " +
			"pellentesque. Suspendisse integer praesent vel, integer gravida mauris, fringilla vehicula lacinia non";

	private AccessToken token;
	private String calendar;
	
	@Test
	@RunAsClient
	public void testAutoTruncateOnEventCreation()
			throws ServerFault, EventAlreadyExistException, NotAllowedException, EventNotFoundException, AuthFault {
		calendar = "user1@domain.org";
		token = loginClient.login(calendar, "user1");
		
		EventObmId eventObmId = calendarClient.createEvent(token, calendar, getFakeBigFieldsEvent(calendar), false, null);
		Event eventFromServer = calendarClient.getEventFromId(token, calendar, eventObmId);
		
		assertFieldsOfEventAreTruncated(eventFromServer);
	}
	
	@Test
	@RunAsClient
	public void testAutoTruncateOnEventModification()
			throws ServerFault, NotAllowedException, EventNotFoundException, AuthFault, EventAlreadyExistException {
		calendar = "user1@domain.org";
		token = loginClient.login(calendar, "user1");
		
		final Event event = getFakeBigFieldsEvent(calendar);
		EventObmId eventObmId = calendarClient.createEvent(token, calendar, event, false, null);
		calendarClient.modifyEvent(token, calendar, event, false, false);
		Event eventFromServer = calendarClient.getEventFromId(token, calendar, eventObmId);
		
		assertFieldsOfEventAreTruncated(eventFromServer);
	}
	
	private void assertFieldsOfEventAreTruncated(Event event) {
		assertThat(event.getTitle().length()).isEqualTo(MAX_BYTES_255);
		assertThat(event.getLocation().length()).isEqualTo(MAX_BYTES_255);
	}
	
	private Event getFakeBigFieldsEvent(String calendar) {
		Event event = new Event();
		EventExtId extId = new EventExtId(UUID.randomUUID().toString());

		event.setOwnerEmail(calendar);
		event.setExtId(extId);
		event.setTitle(BIGFIELD);
		event.setLocation(BIGFIELD);
		event.setStartDate(date("2013-06-01T12:00:00"));
		event.setDuration(3600);
		event.addAttendee(UnidentifiedAttendee.builder().email(calendar).asOrganizer().build());

		return event;
	}
	
	@Test
	@RunAsClient
	public void testAutoTruncateOnContactCreation()
			throws NoPermissionException, ServerFault, ContactNotFoundException, AuthFault {
		calendar = "user1@domain.org";
		token = loginClient.login(calendar, "user1");
		
		Contact contact = bookClient.createContact(token, ADDRESSBOOK_ID, getFakeBigFieldsContact(), null);
		Contact contactFromServer = bookClient.getContactFromId(token, ADDRESSBOOK_ID, contact.getUid());
		
		assertFieldsOfContactAreTruncated(contactFromServer);
	}
	
	@Test
	@RunAsClient
	public void testAutoTruncateOnContactModification()
			throws NoPermissionException, ServerFault, ContactNotFoundException, AuthFault {
		calendar = "user1@domain.org";
		token = loginClient.login(calendar, "user1");
		
		final Contact contact = getFakeBigFieldsContact();
		Contact createdContact = bookClient.createContact(token, ADDRESSBOOK_ID, contact, null);
		bookClient.modifyContact(token, ADDRESSBOOK_ID, createdContact);
		Contact contactFromServer = bookClient.getContactFromId(token, ADDRESSBOOK_ID, createdContact.getUid());
		
		assertFieldsOfContactAreTruncated(contactFromServer);
	}
	
	private void assertFieldsOfContactAreTruncated(Contact contact) {
		assertThat(contact.getTitle().length()).isEqualTo(MAX_BYTES_64);
		assertThat(contact.getSpouse().length()).isEqualTo(MAX_BYTES_64);
		assertThat(contact.getAssistant().length()).isEqualTo(MAX_BYTES_64);
		assertThat(contact.getCommonname().length()).isEqualTo(MAX_BYTES_256);
		assertThat(contact.getFirstname().length()).isEqualTo(MAX_BYTES_64);
		assertThat(contact.getLastname().length()).isEqualTo(MAX_BYTES_64);
		assertThat(contact.getManager().length()).isEqualTo(MAX_BYTES_64);
		assertThat(contact.getService().length()).isEqualTo(MAX_BYTES_64);
		assertThat(contact.getAka().length()).isEqualTo(MAX_BYTES_255);
		assertThat(contact.getCompany().length()).isEqualTo(MAX_BYTES_64);
		assertThat(contact.getMiddlename().length()).isEqualTo(MAX_BYTES_32);
		assertThat(contact.getSuffix().length()).isEqualTo(MAX_BYTES_16);
	}
	
	private Contact getFakeBigFieldsContact() {
		Contact contact = new Contact();
		
		contact.setTitle(BIGFIELD);
		contact.setSpouse(BIGFIELD);
		contact.setAssistant(BIGFIELD);
		contact.setCommonname(BIGFIELD);
		contact.setFirstname(BIGFIELD);
		contact.setLastname(BIGFIELD);
		contact.setManager(BIGFIELD);
		contact.setService(BIGFIELD);
		contact.setAka(BIGFIELD);
		contact.setCompany(BIGFIELD);
		contact.setMiddlename(BIGFIELD);
		contact.setSuffix(BIGFIELD);
		
		return contact;
	}
	
}
