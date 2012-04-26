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
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.obm.push.utils.collection.Sets;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class EventRecurrence implements Anonymizable<EventRecurrence> {

	private static final int UNSPECIFIED_FREQUENCY_VALUE = 0;
	private RecurrenceDays days;
	private Date end;
	private int frequence;
	private RecurrenceKind kind;
	private List<Date> exceptions;
	private List<Event> eventExceptions;

	public EventRecurrence(RecurrenceKind kind) {
		this.exceptions = new LinkedList<Date>();
		this.eventExceptions = new LinkedList<Event>();
		this.kind = kind;
		this.days = new RecurrenceDays();
	}

	public EventRecurrence() {
		this(RecurrenceKind.none);
	}

	public RecurrenceDays getDays() {
		return days;
	}

	public void setDays(RecurrenceDays days) {
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

	public List<Date> getExceptions() {
		return exceptions;
	}

	public void setExceptions(Iterable<Date> exceptions) {
		this.exceptions = Lists.newArrayList(exceptions);
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
		eventRecurrence.setExceptions(exceptions);
		eventRecurrence.setFrequence(this.frequence);
		eventRecurrence.setKind(this.kind);
		return eventRecurrence;
	}

	public boolean hasImportantChanges(EventRecurrence recurrence) {
		boolean hasImportantChangesExceptedEventException = this.hasImportantChangesExceptedEventException(recurrence);
		if (hasImportantChangesExceptedEventException) {
			return true;
		}

		return false;
	}
	
	private boolean hasImportantChangesExceptedEventException(EventRecurrence recurrence) {
		if (recurrence == null) {
			return true;
		}
		if ( !(Objects.equal(this.end, recurrence.end)
				&& Objects.equal(this.kind, recurrence.kind)
				&& Objects.equal(this.frequence, recurrence.frequence)) ) {
			return true;
		}
		
		return false;
	}
	
	public Event getEventExceptionWithRecurrenceId(Date recurrenceId) {
		for(Event event : this.eventExceptions){
			if(recurrenceId.equals(event.getRecurrenceId())) {
				return event;
			}
		}
		return null;
	}

	public boolean isRecurrent() {
		return (this.kind != RecurrenceKind.none);
	}

	public void replaceUnattendedEventExceptionByException(String attendeeEmail) {
		List<Event> eventExceptionsCopy = Lists.newArrayList(eventExceptions);
		for (Event eexp : eventExceptions) {
			Attendee attendee = eexp.findAttendeeFromEmail(attendeeEmail);
			boolean willAttend = attendee != null
					&& attendee.getState() != ParticipationState.DECLINED;
			if (!willAttend) {
				exceptions.add(eexp.getRecurrenceId());
				eventExceptionsCopy.remove(eexp);
			}
		}
		eventExceptions = eventExceptionsCopy;
	}

	public boolean hasAnyExceptionAtDate(Date exceptionDateToFind) {
		return hasEventExceptionAtDate(exceptionDateToFind) ||
				hasDeletedExceptionAtDate(exceptionDateToFind);
	}

	private boolean hasEventExceptionAtDate(Date exceptionDateToFind) {
		for (Event eventException : eventExceptions) {
			if (eventException.getRecurrenceId().equals(exceptionDateToFind)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hasDeletedExceptionAtDate(Date exceptionDateToFind) {
		return exceptions.contains(exceptionDateToFind);
	}

	public boolean hasException() {
		return exceptions != null && !exceptions.isEmpty();
	}
	
	public boolean hasEventException() {
		return eventExceptions != null && !eventExceptions.isEmpty();
	}
	
	public boolean frequencyIsSpecified() {
		return frequence != UNSPECIFIED_FREQUENCY_VALUE;
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

	public Collection<Date> getNegativeExceptionsChanges(EventRecurrence recurrence) {
		Collection<Date> changes = Sets.difference(this.exceptions, recurrence.exceptions,
					new Comparator<Date>() {
						@Override
						public int compare(Date d1, Date d2) {
							return d1.compareTo(d2);
						}
					});
		return changes;
	}

	public boolean hasNegativeExceptions() {
		return !this.exceptions.isEmpty();
	}

	protected void setExtIdOnEventExceptions(EventExtId extId) {
		for (Event exception: eventExceptions) {
			exception.setExtId(extId);
		}
	}

	@Override
	public EventRecurrence anonymizePrivateItems() {
		EventRecurrence anonymizedRecurrence = new EventRecurrence();
		anonymizedRecurrence.days = this.days;
		anonymizedRecurrence.end = this.end;
		anonymizedRecurrence.exceptions = this.exceptions;
		anonymizedRecurrence.frequence = frequence;
		anonymizedRecurrence.kind = kind;
		anonymizedRecurrence.eventExceptions = Lists.transform(this.eventExceptions,
				new Function<Event, Event>() {
					@Override
					public Event apply(Event event) {
						return event.anonymizePrivateItems();
					}
				});
		return anonymizedRecurrence;
	}
}
