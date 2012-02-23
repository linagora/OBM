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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.obm.push.utils.collection.Sets;

import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Lists;
import com.google.common.base.Preconditions;

public class EventRecurrence {

	private String days;
	private Date end;
	private int frequence;
	private RecurrenceKind kind;
	private List<Date> exceptions;
	private List<Event> eventExceptions;

	public EventRecurrence() {
		exceptions = new LinkedList<Date>();
		eventExceptions = new LinkedList<Event>();
	}

	public String getDays() {
		return days;
	}

	public void setDays(String days) {
		if(!Strings.isNullOrEmpty(days)) {
			int size = days.length();
			Preconditions.checkArgument(size == RecurrenceDay.RECURRENCE_DAY_COUNT, "The length of repeat days must be 7: %s", size);
			for (char c : days.toCharArray()) {
				Preconditions.checkArgument(c == '0' || c == '1', "Illegal char in repeat days: %s", c);
			}
		}

		this.days = days;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public int getFrequence() {
		return frequence;
	}

	public void setFrequence(int frequence) {
		this.frequence = frequence;
	}

	public RecurrenceKind getKind() {
		return kind;
	}

	public void setKind(RecurrenceKind kind) {
		this.kind = kind;
	}

	public Date[] getExceptions() {
		return exceptions.toArray(new Date[exceptions.size()]);
	}

	public List<Date> getListExceptions() {
		return exceptions;
	}

	public void setExceptions(Date[] exceptions) {
		this.exceptions = Arrays.asList(exceptions);
	}

	public void addException(Date d) {
		exceptions.add(d);
	}

	public List<Event> getEventExceptions() {
		return eventExceptions;
	}

	public void setEventExceptions(List<Event> eventsExceptions) {
		Preconditions.checkNotNull(eventsExceptions);
		this.eventExceptions = eventsExceptions;
	}

	public void addEventException(Event eventException) {
		this.eventExceptions.add(eventException);
	}

	public EventRecurrence clone() {
		EventRecurrence eventRecurrence = new EventRecurrence();
		eventRecurrence.setDays(this.days);
		eventRecurrence.setEnd(this.end);
		List<Event> eventExceptions = new ArrayList<Event>();
		for (Event event: this.eventExceptions) {
			eventExceptions.add(event.clone());
		}
		eventRecurrence.setEventExceptions(eventExceptions);
		eventRecurrence.setExceptions(getExceptions());
		eventRecurrence.setFrequence(this.frequence);
		eventRecurrence.setKind(this.kind);
		return eventRecurrence;
	}
	
	public boolean hasImportantChanges(EventRecurrence recurrence) {
		boolean hasImportantChangesExceptedEventException = hasImportantChangesExceptedEventException(recurrence);
		if(hasImportantChangesExceptedEventException) {
			return true;
		}
		
		if (recurrence != null && !(this.eventExceptions.size() == recurrence.eventExceptions.size())) {
			return true;
		}
		
		Collection<Event> difference = getExceptionsWithImportantChanges(recurrence);
		return !difference.isEmpty();
	}

	public List<Event> getExceptionsWithImportantChanges(EventRecurrence recurrence) {
		Set<Event> difference = Sets.difference(this.getEventExceptions(),
				recurrence.getEventExceptions(),
				new ComparatorUsingEventHasImportantChanges());
		List<Event> exceptionsWithImportantChanges = Lists.newArrayList(difference);
		return exceptionsWithImportantChanges;
	}
	
	public boolean hasImportantChangesExceptedEventException(EventRecurrence recurrence) {
		if (recurrence == null) {
			return true;
		}
		if ( !(Objects.equal(this.end, recurrence.end)
				&& Objects.equal(this.kind, recurrence.kind)
				&& Objects.equal(this.frequence, recurrence.frequence)
				&& (this.exceptions.size() == recurrence.exceptions.size())) ) {
			return true;
		}
		
		return false;
	}
	
	public List<Event> getEventExceptionWithChangesExceptedOnException(EventRecurrence recurrence) {
		if (recurrence == null) {
			return ImmutableList.copyOf(this.getEventExceptions());
		}
		Builder<Event> eventExceptionWithChanges = ImmutableList.builder();
		
		final AllEventAttributesExceptExceptionsEquivalence equivalence = new AllEventAttributesExceptExceptionsEquivalence();
		Collection<Wrapper<Event>> recurrenceEquivalenceWrappers = transformToEquivalenceWrapper(recurrence, equivalence);
		
		for(Event exp : eventExceptions){
			Wrapper<Event> r = equivalence.wrap(exp);
			if(!recurrenceEquivalenceWrappers.contains(r)) {
				eventExceptionWithChanges.add(exp);
			}
		}
		return eventExceptionWithChanges.build();
	}
	
	private Collection<Wrapper<Event>> transformToEquivalenceWrapper(EventRecurrence recurrence, 
			final AllEventAttributesExceptExceptionsEquivalence equivalence) {
		return Collections2.transform(recurrence.getEventExceptions(), new Function<Event, Equivalence.Wrapper<Event>>() {
			@Override
			public Equivalence.Wrapper<Event> apply(Event input) {
				Wrapper<Event> wrapper = equivalence.wrap(input);
				return wrapper;
			}
		});
	}

	public Event getEventExceptionWithRecurrenceId(Date recurrenceId) {
		for(Event event : this.eventExceptions){
			if(recurrenceId.equals(event.getRecurrenceId())) {
				return event;
			}
		}
		return null;
	}
	
	public EnumSet<RecurrenceDay> getReadableRepeatDays() {	
		EnumSet<RecurrenceDay> recurrenceDaysSet = EnumSet.noneOf(RecurrenceDay.class);
		if (!Strings.isNullOrEmpty(days) && !days.equals("0000000")) {
			char[] daysToCharArray = days.toCharArray();
			
			for (int i = 0; i < daysToCharArray.length; i++) {
				if (daysToCharArray[i] == '1') {
					recurrenceDaysSet.add(RecurrenceDay.getByIndex(i));
				}
			}
		}
		return recurrenceDaysSet;
	}

	public boolean isRecurrent() {
		return (this.kind != RecurrenceKind.none);
	}
	
	public void replaceDeclinedEventExceptionByException(String attendeeEmail) {
		List<Event> eventExceptionsCopy = Lists.newArrayList(eventExceptions);
		for (Event eexp : eventExceptions) {
			Attendee attendee = eexp.findAttendeeFromEmail(attendeeEmail);
			if (attendee != null && attendee.getState() == ParticipationState.DECLINED) {
				exceptions.add(eexp.getRecurrenceId());
				eventExceptionsCopy.remove(eexp);
			}
		}
		eventExceptions = eventExceptionsCopy;
	}
	
	@Override
	public final int hashCode() {
		return Objects.hashCode(days, end, frequence, kind, exceptions,
				eventExceptions);
	}

	@Override
	public final boolean equals(Object object){
		if (object instanceof EventRecurrence) {
			EventRecurrence that = (EventRecurrence) object;
			return Objects.equal(this.days, that.days)
				&& Objects.equal(this.end, that.end)
				&& Objects.equal(this.frequence, that.frequence)
				&& Objects.equal(this.kind, that.kind)
				&& Objects.equal(this.exceptions, that.exceptions)
				&& Objects.equal(this.eventExceptions, that.eventExceptions);
		}
		return false;
	}

}
