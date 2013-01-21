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
package org.obm.sync.calendar;


import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

import com.google.common.collect.Lists;

@RunWith(SlowFilterRunner.class)
public class AllEventAttributesExceptExceptionsEquivalenceTest  {
	
	private Event getStubBeforeTimeUpdateEvent(){
		Event ev = getStubEvent();
		ev.setTimeUpdate(timeUpdateBefore());
		return ev;
	}
	
	private Event getStubAfterTimeUpdateEvent(){
		Event ev = getStubEvent();
		ev.setTimeUpdate(timeUpdateAfter());
		return ev;
	}
	
	private Event getStubBeforeSequenceEvent(){
		Event ev = getStubEvent();
		ev.setSequence(sequenceBefore());
		return ev;
	}
	
	private Event getStubAfterSequenceEvent(){
		Event ev = getStubEvent();
		ev.setSequence(sequenceAfter());
		return ev;
	}
	
	private Event getStubEvent(){
		Event ev = new Event();
		ev.setInternalEvent(true);
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(1295258400000L);
		ev.setStartDate(cal.getTime());
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
		ev.setTimeUpdate(cal.getTime());
		
		cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
		ev.setTimeCreate(cal.getTime());
		ev.setCategory("category");
		ev.setExtId(new EventExtId("2bf7db53-8820-4fe5-9a78-acc6d3262149"));
		ev.setTitle("fake rdv");
		ev.setOwner("john@do.fr");
		ev.setDuration(3600);
		ev.setDescription("description");
		ev.setLocation("tlse");
		ev.setOpacity(EventOpacity.OPAQUE);
		ev.setPriority(1);
		List<Attendee> la = new LinkedList<Attendee>();
		la.add(UserAttendee
				.builder()
				.asOrganizer()
				.displayName("John Do")
				.participation(Participation.needsAction())
				.participationRole(ParticipationRole.CHAIR)
				.email("john@do.fr")
				.build());
		la.add(ContactAttendee
				.builder()
				.displayName("noIn TheDatabase")
				.email("notin@mydb.com")
				.participation(Participation.accepted())
				.participationRole(ParticipationRole.OPT)
				.build());
		ev.setAttendees(la);
		ev.setAlert(60);
		EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.daily);
		er.setFrequence(1);
		er.addException(ev.getStartDate());
		cal.add(Calendar.MONTH, 1);
		er.addException(cal.getTime());
		er.setEnd(null);
		ev.setRecurrence(er);
		ev.setSequence(3);
		
		return ev;
	}
	
	@Test
	public void testCompareNoChange(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(true, result);
	}
	
	@Test
	public void testCompareAllDay(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setAllday(!e2.isAllday());
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAlert(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setAlert(e1.getAlert() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAlert2(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setAlert(e1.getAlert() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAddAttendees(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		
		e2.getAttendees().add(UserAttendee
				.builder()
				.displayName("User Un")
				.email("uun@mydb.com")
				.participation(Participation.accepted())
				.participationRole(ParticipationRole.OPT)
				.build());
		
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}

	@Test
	public void testCompareAttendeesInDifferentOrder() {
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubBeforeSequenceEvent();

		List<Attendee> e1Attendees = e1.getAttendees();
		LinkedList<Attendee> newE2Attendees = Lists.newLinkedList();
		newE2Attendees.add(e1Attendees.get(1));
		newE2Attendees.add(e1Attendees.get(0));

		e2.setAttendees(newE2Attendees);
		boolean result = comparator.equivalent(e1, e2);

		Assert.assertTrue(result);
	}

	@Test
	public void testCompareDeleteAttendees(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.getAttendees().remove(0);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDeleteAndAddAttendees(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.getAttendees().remove(0);
		e2.getAttendees().add(UserAttendee
				.builder()
				.displayName("User Un")
				.email("uun@mydb.com")
				.participation(Participation.accepted())
				.participationRole(ParticipationRole.OPT)
				.build());
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareCategory(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setCategory(e1.getCategory() + "Modif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyCategory(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setCategory("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDescription(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setDescription(e1.getDescription() + "Mofif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyDescription(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setDescription("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEndDateAfter(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setDuration(e1.getDuration() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEndDateBefore(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setDuration(e1.getDuration() - 1000);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareOpacity(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setOpacity(EventOpacity.TRANSPARENT);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testComparePriority(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setPriority(e1.getPriority() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLesserPriority(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setPriority(e1.getPriority() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testComparePrivacy(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setPrivacy(EventPrivacy.PRIVATE);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareTitle(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setTitle(e1.getTitle()+"Modif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyTitle(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setTitle("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLocation(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setLocation(e1.getLocation()+"Modif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyLocation(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setLocation("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDateBefore(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		Date start = new Date( e1.getStartDate().getTime() - 1000);
		e2.setStartDate(start);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDateAfter(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		Date start = new Date( e1.getStartDate().getTime() + 1000);
		e2.setStartDate(start);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDuration(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setDuration(e1.getDuration() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLesserDuration(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setDuration(e1.getDuration() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareType(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.setType(EventType.VTODO);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurDays(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.getRecurrence().setDays(
				new RecurrenceDays(RecurrenceDay.Sunday, RecurrenceDay.Tuesday,
						RecurrenceDay.Thursday, RecurrenceDay.Saturday));

		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurEndBefore(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		Date end = new Date( e1.getStartDate().getTime() - 1000);
		e2.setStartDate(end);
		e2.getRecurrence().setEnd(end);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurEndAfter(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		Date end = new Date( e1.getStartDate().getTime() + 1000);
		e2.setStartDate(end);
		e2.getRecurrence().setEnd(end);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurFrequence(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.getRecurrence().setFrequence(e1.getRecurrence().getFrequence() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurLesserFrequence(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.getRecurrence().setFrequence(e1.getRecurrence().getFrequence() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurKind(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.getRecurrence().setKind(RecurrenceKind.yearly);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareTestReflexiv(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeSequenceEvent();
		Event e2 = getStubAfterSequenceEvent();
		e2.getRecurrence().setKind(RecurrenceKind.yearly);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAllDayWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setAllday(!e2.isAllday());
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAlertWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setAlert(e1.getAlert() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAlert2WithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setAlert(e1.getAlert() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareAddAttendeesWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		
		e2.getAttendees().add(UserAttendee
				.builder()
				.displayName("User Un")
				.email("uun@mydb.com")
				.participation(Participation.accepted())
				.participationRole(ParticipationRole.OPT)
				.build());
		
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDeleteAttendeesWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.getAttendees().remove(0);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDeleteAndAddAttendeesWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.getAttendees().remove(0);
		e2.getAttendees().add(UserAttendee
				.builder()
				.displayName("User Un")
				.email("uun@mydb.com")
				.participation(Participation.accepted())
				.participationRole(ParticipationRole.OPT)
				.build());
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareCategoryWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setCategory(e1.getCategory() + "Modif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyCategoryWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setCategory("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDescriptionWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setDescription(e1.getDescription() + "Mofif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyDescriptionWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setDescription("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEndDateAfterWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setDuration(e1.getDuration() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEndDateBeforeWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setDuration(e1.getDuration() - 1000);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareOpacityWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setOpacity(EventOpacity.TRANSPARENT);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}

	@Test
	public void testComparePriorityWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setPriority(e1.getPriority() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLesserPriorityWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setPriority(e1.getPriority() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testComparePrivacyWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setPrivacy(EventPrivacy.PRIVATE);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareTitleWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setTitle(e1.getTitle()+"Modif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyTitleWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setTitle("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLocationWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setLocation(e1.getLocation()+"Modif");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareEmptyLocationWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setLocation("");
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDateBeforeWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		Date start = new Date( e1.getStartDate().getTime() - 1000);
		e2.setStartDate(start);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDateAfterWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		Date start = new Date( e1.getStartDate().getTime() + 1000);
		e2.setStartDate(start);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareDurationWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setDuration(e1.getDuration() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareLesserDurationWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setDuration(e1.getDuration() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareTypeWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.setType(EventType.VTODO);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurDaysWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.getRecurrence().setDays(
				new RecurrenceDays(RecurrenceDay.Sunday, RecurrenceDay.Tuesday,
						RecurrenceDay.Thursday, RecurrenceDay.Saturday));

		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurEndBeforeWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		Date end = new Date( e1.getStartDate().getTime() - 1000);
		e2.setStartDate(end);
		e2.getRecurrence().setEnd(end);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurEndAfterWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		Date end = new Date( e1.getStartDate().getTime() + 1000);
		e2.setStartDate(end);
		e2.getRecurrence().setEnd(end);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurFrequenceWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.getRecurrence().setFrequence(e1.getRecurrence().getFrequence() + 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurLesserFrequenceWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.getRecurrence().setFrequence(e1.getRecurrence().getFrequence() - 10);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareRecurKindWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.getRecurrence().setKind(RecurrenceKind.yearly);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	@Test
	public void testCompareTestReflexivWithDifferentTimeUpdateEvent(){
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		Event e1 = getStubBeforeTimeUpdateEvent();
		Event e2 = getStubAfterTimeUpdateEvent();
		e2.getRecurrence().setKind(RecurrenceKind.yearly);
		boolean result = comparator.equivalent(e1, e2);
		
		Assert.assertEquals(false, result);
	}
	
	private Date timeUpdateBefore(){
		Calendar cal = Calendar.getInstance();
		cal.set(2011, 9, 1);
		return cal.getTime();
	}
	
	private Date timeUpdateAfter(){
		Calendar cal = Calendar.getInstance();
		cal.set(2011, 10, 1);
		return cal.getTime();
	}
	
	private int sequenceBefore(){
		return 5;
	}
	
	private int sequenceAfter(){
		return 10;
	}
	
}
