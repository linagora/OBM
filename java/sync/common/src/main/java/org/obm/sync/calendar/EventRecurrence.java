package org.obm.sync.calendar;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
}