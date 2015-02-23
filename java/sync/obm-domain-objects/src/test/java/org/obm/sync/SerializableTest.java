/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.sync;

import java.util.Calendar;
import java.util.Set;

import org.joda.time.DateTime;
import org.junit.Test;
import org.obm.sync.base.DomainName;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.base.EmailLogin;
import org.obm.sync.book.Address;
import org.obm.sync.book.Contact;
import org.obm.sync.book.InstantMessagingId;
import org.obm.sync.book.Phone;
import org.obm.sync.book.Website;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventMeetingStatus;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.RecurrenceDay;
import org.obm.sync.calendar.RecurrenceDays;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.dao.EntityId;
import org.obm.sync.host.ObmHost;
import org.obm.sync.serviceproperty.ServiceProperty;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.testing.SerializableTester;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.domain.ObmDomainUuid;
import fr.aliacom.obm.common.domain.Samba;


public class SerializableTest {

	@Test
	public void testSimpleBeans() {
		Set<Object> beans = ImmutableSet.<Object>of(
			EmailAddress.loginAndDomain(new EmailLogin("login"), new DomainName("domain")),
			Samba.builder().sid("sid").profile("profile").home("home").drive("drive").build()
		);
		
		for (Object o : beans) {
			SerializableTester.reserializeAndAssert(o);
		}
	}
	
	@Test
	public void testFullEvent() {
		Event event = new Event();
		event.setTitle("my event");
		event.setDescription("description");
		event.setUid(new EventObmId(2));
		event.setExtId(new EventExtId("my_event"));
		event.setEntityId(EntityId.valueOf(5));
		event.setOwner("owner");
		event.setOwnerDisplayName("owner display name");
		event.setOwnerEmail("owner@email.com");
		event.setCreatorDisplayName("creator");
		event.setCreatorEmail("creator@email.com");
		event.setLocation("location");
		event.setStartDate(new DateTime(2012, Calendar.APRIL, 25, 14, 0).toDate());
		event.setDuration(3660);
		event.setAlert(3);
		event.setCategory("category");
		event.setPriority(2);
		event.setAllday(false);
		event.setAnonymized(false);
		event.setType(EventType.VEVENT);
		event.setOpacity(EventOpacity.TRANSPARENT);
		event.setMeetingStatus(EventMeetingStatus.IS_A_MEETING);
		event.setPrivacy(EventPrivacy.CONFIDENTIAL);
		event.setTimeCreate(new DateTime(2012, Calendar.APRIL, 24, 14, 0).toDate());
		event.setTimeUpdate(new DateTime(2012, Calendar.APRIL, 24, 18, 0).toDate());
		event.setTimezoneName("timezone");
		event.setInternalEvent(false);
		event.setSequence(2);
		event.addAttendee(UserAttendee.builder().asOrganizer().email("att1@email.com").build());
		
		EventRecurrence recurrence = new EventRecurrence();
		recurrence.setKind(RecurrenceKind.daily);
		recurrence.setDays(new RecurrenceDays(RecurrenceDay.Sunday));
		recurrence.setEnd(new DateTime("2012-02-01T14:00:00Z").toDate());
		recurrence.setFrequence(1);
		event.setRecurrence(recurrence);
		
		SerializableTester.reserializeAndAssert(event);
	}

	@Test
	public void testFullContact() {
		Contact contact = new Contact();
		contact.setLastname("name");
		contact.setFirstname("firstname");
		contact.setCommonname("common");
		contact.setCollected(true);
		contact.setTitle("title");
		contact.setSpouse("spouse");
		contact.setAssistant("assistant");
		contact.setManager("manager");
		contact.setService("service");
		contact.setAka("aka");
		contact.setCompany("company");
		contact.setMiddlename("middle name");
		contact.setSuffix("suffix");
		contact.addEmail("INTERNET;X-OBM-Ref1", EmailAddress.loginAtDomain("email@domain"));
		contact.setAddresses(ImmutableMap.of("a", new Address()));
		contact.setAnniversaryId(new EventObmId(5));
		contact.setBirthdayId(new EventObmId(5));
		contact.setEntityId(EntityId.valueOf(5));
		contact.setPhones(ImmutableMap.of("a", new Phone("number")));
		contact.setImIdentifiers(ImmutableMap.of("a", new InstantMessagingId()));
		contact.setWebsites(Sets.newHashSet(new Website()));

		SerializableTester.reserializeAndAssert(contact);
	}
	
	@Test
	public void testFullObmDomain() {
		SerializableTester.reserializeAndAssert(ObmDomain
			.builder()
			.id(1)
			.name("domain_name")
			.uuid(ObmDomainUuid.of("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6"))
			.label("domain_label")
			.alias("domain_alias")
			.global(false)
			.host(ServiceProperty.IMAP,
				ObmHost.builder().id(1).domainId(1).name("imap").ip("1.2.3.4").fqdn("imap.domain_name").build())
			.samba(Samba.builder().sid("sid").profile("profile").home("home").drive("drive").build())
			.build());
	}
}
