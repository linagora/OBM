package org.obm.sync.calendar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import com.google.common.base.Objects;

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

	public void setEventExceptions(List<Event> eventExceptions) {
		this.eventExceptions = eventExceptions;
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
		if (recurrence == null) {
			return true;
		}
		if ( !(Objects.equal(this.end, recurrence.end)
				&& Objects.equal(this.kind, recurrence.kind)
				&& Objects.equal(this.frequence, recurrence.frequence)
				&& (this.eventExceptions.size() == recurrence.eventExceptions.size())
				&& (this.exceptions.size() == recurrence.exceptions.size())) ) {
			return true;
		}
		return compareEventExceptionsChanges(recurrence);
	}

	private boolean compareEventExceptionsChanges(EventRecurrence recurrence) {
		Iterator<Event> from = createEventIteratorSortedFromDate(this.eventExceptions);
		Iterator<Event> to = createEventIteratorSortedFromDate(recurrence.getEventExceptions());
		while (from.hasNext()) {
			if (to.hasNext()) {
				if (from.next().hasImportantChanges(to.next())) {
					return true;
				}
			}
		}
		return false;
	}
	
	private Iterator<Event> createEventIteratorSortedFromDate(List<Event> events) {
		TreeSet<Event> set = new TreeSet<Event>(new Comparator<Event>() {
			@Override
			public int compare(Event event0, Event event1) {
				return event0.getDate().compareTo(event1.getDate());
			}
		});
		set.addAll(events);
		return set.iterator();
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