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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.push.utils.collection.Sets;
import org.obm.push.utils.index.Indexed;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Event implements Indexed<Integer>, Anonymizable<Event>, Serializable {

	public static final int SECONDS_IN_A_DAY = 3600 * 24;
	public static final int DATABASE_TITLE_MAX_LENGTH = 255;
	
	private String title;
	private String description;
	private EventObmId uid;
	private EventExtId extId;
	private EventPrivacy privacy;
	private EventMeetingStatus meetingStatus;
	private String owner;
	private String ownerDisplayName;
	private String ownerEmail;
	private String creatorDisplayName;
	private String creatorEmail;
	private String location;
	private Date startDate;
	private int duration;
	private Integer alert;
	private String category;
	private Integer priority;

	private boolean allday;
	private List<Attendee> attendees;
	private EventRecurrence recurrence;
	private EventType type;
	private Date completion;
	private Integer percent;
	private EventOpacity opacity;

	private Integer entityId;
	private Date timeUpdate;
	private Date timeCreate;

	private String timezoneName;

	private Date recurrenceId;
	private boolean internalEvent;
	
	private int sequence;
	
	public Event() {
		attendees = new LinkedList<Attendee>();
		type = EventType.VEVENT;
		timezoneName = "Europe/Paris";
		sequence = 0;
		recurrence = new EventRecurrence();
		privacy = EventPrivacy.PUBLIC;
		opacity = EventOpacity.OPAQUE;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		if ( title != null && title.length() > DATABASE_TITLE_MAX_LENGTH ) {
			this.title = title.substring(0, DATABASE_TITLE_MAX_LENGTH);
			return ;
		}
		this.title = title;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return true if duration is different from zero and is a multiple of SECONDS_IN_A_DAY
	 */
	@VisibleForTesting boolean lastsFullDays() {
	    return duration != 0 && (duration % SECONDS_IN_A_DAY == 0);
	}

	/**
	 * @return duration in seconds rounded up to the nearest multiple of SECONDS_IN_A_DAY if necessary.
	 */
	@VisibleForTesting int durationInFullDays() {
	    int truncatedNumberOfDays = duration / SECONDS_IN_A_DAY;
	    return (truncatedNumberOfDays + 1) * SECONDS_IN_A_DAY;
	}

	/**
	 * @return duration in second
	 */
	public int getDuration() {
		if (allday && !lastsFullDays()) {
			return durationInFullDays();
		}
		return duration;
	}

	public void setDuration(int duration) {
		Preconditions.checkArgument(duration >= 0, "Duration must be a positive integer value");
		this.duration = duration;
	}

	public boolean isAllday() {
		return allday;
	}

	public void setAllday(boolean allDay) {
		this.allday = allDay;
	}
	
	public Collection<Attendee> getUserAttendees() {
		return Collections2.filter(attendees, Predicates.instanceOf(UserAttendee.class));
	}

	public Collection<Attendee> getContactAttendees() {
		return Collections2.filter(attendees, Predicates.instanceOf(ContactAttendee.class));
	}

	public Collection<Attendee> getResourceAttendees() {
		return Collections2.filter(attendees, Predicates.instanceOf(ResourceAttendee.class));
	}

	public List<Attendee> getAttendees() {
		return attendees;
	}

	public void addAttendees(Collection<? extends Attendee> attendees) {
		if(this.attendees == null){
			this.attendees = new LinkedList<Attendee>(); 
		}
		this.attendees.addAll(attendees);
	}
	
	public void setAttendees(List<? extends Attendee> attendees) {
		this.attendees.clear();
		
		if (attendees != null) {
			addAttendees(attendees);
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public EventPrivacy getPrivacy() {
		return privacy;
	}

	public void setPrivacy(EventPrivacy privacy) {
		Preconditions.checkNotNull(privacy);
		this.privacy = privacy;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getOwnerEmail() {
		return ownerEmail;
	}

	public void setOwnerEmail(String ownerEmail) {
		this.ownerEmail = ownerEmail;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public EventObmId getObmId() {
		return uid;
	}

	public void setUid(EventObmId uid) {
		this.uid = uid;
	}

	public Integer getAlert() {
		return alert;
	}

	public void setAlert(Integer alert) {
		if (alert != null && alert < 0) {
			throw new IllegalArgumentException("alert must be a positive value");
		}
		this.alert = alert;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public EventRecurrence getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(EventRecurrence recurrence) {
		this.recurrence = recurrence;
	}

	public EventType getType() {
		return type;
	}

	public void setType(EventType type) {
		this.type = type;
	}

	public void addAttendee(Attendee att) {
		attendees.add(att);
	}

	public EventOpacity getOpacity() {
		return opacity;
	}

	public void setOpacity(EventOpacity opacity) {
		this.opacity = opacity;
	}

	public EventExtId getExtId() {
		return extId;
	}

	public void setExtId(EventExtId extId) {
		this.extId = extId;
		this.recurrence.setExtIdOnEventExceptions(extId);
	}

	public Date getTimeUpdate() {
		return timeUpdate;
	}

	public void setTimeUpdate(Date timeUpdate) {
		this.timeUpdate = timeUpdate;
	}

	public EventMeetingStatus getMeetingStatus() {
		return meetingStatus;
	}

	public void setMeetingStatus(EventMeetingStatus meetingStatus) {
		this.meetingStatus = meetingStatus;
	}

	@Override
	public Event clone() {
		Event event = new Event();
		event.alert = alert;
		event.allday = allday;
		event.attendees = copyAttendees();
		event.category = category;
		event.startDate = startDate;
		event.description = description;
		event.duration = duration;
		event.extId = extId;
		event.location = location;
		event.opacity = opacity;
		event.owner = owner;
		event.ownerDisplayName = ownerDisplayName;
		event.ownerEmail = ownerEmail;
		event.creatorDisplayName = creatorDisplayName;
		event.creatorEmail = creatorEmail;
		event.priority = priority;
		event.privacy = privacy;
		event.recurrence = recurrence.clone();
		event.timeUpdate = timeUpdate;
		event.timezoneName = timezoneName;
		event.title = title;
		event.type = type;
		event.uid = uid;
		event.recurrenceId = recurrenceId;
		event.internalEvent = internalEvent;
		event.sequence = sequence;
		event.timeCreate = timeCreate;
		event.timeUpdate = timeUpdate;
		return event;
	}

	private LinkedList<Attendee> copyAttendees() {
		LinkedList<Attendee> copyOfAttendees = Lists.newLinkedList();
		for(Attendee attendee: attendees) {
			copyOfAttendees.add(attendee.clone());
		}

		return copyOfAttendees;
	}

	public String getTimezoneName() {
		return timezoneName;
	}

	public void setTimezoneName(String timezoneName) {
		this.timezoneName = timezoneName;
	}

	public void setRecurrenceId(Date recurrenceId) {
		this.recurrenceId = recurrenceId;
	}

	public Date getRecurrenceId() {
		return recurrenceId;
	}

	public void setTimeCreate(Date timeCreate) {
		this.timeCreate = timeCreate;
	}

	public Date getTimeCreate() {
		return timeCreate;
	}
	
	public boolean isEventInThePast() {
		Date end = getEndDate();
		Date now = new Date();
		if (end != null && end.before(now) == true) {
			if (getRecurrence().getEnd() != null && getRecurrence().getKind() != RecurrenceKind.none ) {
				return getRecurrence().getEnd().before(now);
			}
			return true;
		}
		return false;
	}

	public Date getEndDate() {
		if (getStartDate() == null) {
			return null;
		} else {
			return getEndDateByDuration();
		}
	}

	private Date getEndDateByDuration() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getStartDate());
		calendar.add(Calendar.SECOND, getDuration());
		Date end = calendar.getTime();
		return end;
	}

	public boolean isInternalEvent() {
		return internalEvent;
	}

	public void setInternalEvent(boolean internalEvent) {
		this.internalEvent = internalEvent;
	}

	public String getOwnerDisplayName() {
		return ownerDisplayName;
	}

	public void setOwnerDisplayName(String ownerDisplayName) {
		this.ownerDisplayName = ownerDisplayName;
	}

	public String getCreatorDisplayName() {
		return creatorDisplayName;
	}

	public void setCreatorDisplayName(String creatorDisplayName) {
		this.creatorDisplayName = creatorDisplayName;
	}

	public String getCreatorEmail() {
		return creatorEmail;
	}

	public void setCreatorEmail(String creatorEmail) {
		this.creatorEmail = creatorEmail;
	}

	public boolean modifiedSince(Date reference) {
		if (reference == null) {
			return true;
		}
		if (timeCreate.compareTo(reference) >= 0) {
			return true;
		}
		if (timeUpdate != null && timeUpdate.compareTo(reference) >= 0) {
			return true;
		}
		return false;
	}
	
	public boolean exceptionModifiedSince(Date reference){
		boolean exceptionModified = false;
		for (Event event: recurrence.getEventExceptions()) {
			if (event.modifiedSince(reference)) {
				exceptionModified = true;
				break;
			}
		}
		return exceptionModified;
	}
	
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	
	public int getSequence() {
		return sequence;
	}

	public Attendee findAttendeeFromEmail(String userEmail) {
		for (Attendee at: attendees) {
			if (at.getEmail().equalsIgnoreCase(userEmail)) {
				return at;
			}
		}
		return null;
	}
	
	public Attendee findOrganizer() {
		for (Attendee att : attendees) {
			if (att.isOrganizer()) {
				return  att;
			}
		}
		return null;
	}
	
	public Attendee findOwner() {
		return findAttendeeFromEmail(ownerEmail);
	}

	@Override
	public Integer getIndex() {
		return getObmId().getIndex();
	}
	
	public boolean hasImportantChanges(Event event) {
		if (hasImportantChangesExceptedEventException(event)) {
			return true;
		}
		if (getEventExceptionsWithImportantChanges(event).size() > 0) {
			return true;
		}
		return false;
	}

	public boolean hasImportantChangesExceptedEventException(Event event) {
		ComparatorUsingEventHasImportantChanges comparator = new ComparatorUsingEventHasImportantChanges();
		if (!comparator.equals(this, event)) {
			return true;
		}
		if (this.isRecurrent() != event.isRecurrent()) {
			return true;
		}
		if (this.isRecurrent()) {
			if (recurrence.hasImportantChanges(event.getRecurrence())) {
				return true;
			}
		}
		return false;
	}

	public List<Event> getDeletedEventExceptions(Event before) {
		return before.getAddedEventExceptions(this);
	}

	public List<Event> getAddedEventExceptions(Event before) {
		Set<Event> afterOccurrences = recurrence.getEventExceptions();
		Set<Event> beforeOccurrences = before.getEventsExceptions();
		Set<Event> addedEventExceptions = Sets.difference(afterOccurrences, beforeOccurrences, new Comparator<Event>() {

			@Override
			public int compare(Event e1, Event e2) {
				return e1.getRecurrenceId().compareTo(e2.getRecurrenceId());
			}
		});

		return Lists.newArrayList(addedEventExceptions);
	}

	public List<Event> getModifiedEventExceptions(Event before) {
		final Map<Date, Event> beforeOccurrencesByReccurrenceId = indexEventExceptionsByRecurrenceId(before.getEventsExceptions());
		Collection<Event> modifiedOcurrences = listModifiedEventExceptions(beforeOccurrencesByReccurrenceId);
		return Lists.newArrayList(modifiedOcurrences);
	}

	private Collection<Event> listModifiedEventExceptions(final Map<Date, Event> beforeOccurrencesByReccurrenceId) {
		final AllEventAttributesExceptExceptionsEquivalence equiv = new AllEventAttributesExceptExceptionsEquivalence();
		Collection<Event> modifiedOcurrences = com.google.common.collect.Collections2.filter(recurrence.getEventExceptions(), new Predicate<Event>() {

			@Override
			public boolean apply(Event afterEventException) {
				Event beforeEventException = beforeOccurrencesByReccurrenceId.get(afterEventException.getRecurrenceId());
				return beforeEventException != null && !equiv.doEquivalent(beforeEventException, afterEventException);
			}
		});
		return modifiedOcurrences;
	}

	private Map<Date, Event> indexEventExceptionsByRecurrenceId(
			Iterable<Event> afterOccurrences) {
		Map<Date, Event> afterOccurrencesByReccurrenceId = Maps.uniqueIndex(afterOccurrences, new Function<Event, Date>() {

			@Override
			public Date apply(Event input) {
				return input.getRecurrenceId();
		}});
		return afterOccurrencesByReccurrenceId;
	}

	public List<Event> getEventExceptionsWithImportantChanges(Event before) {
		Set<Event> ownOccurrences = generateOccurrencesMatchingEventExceptions(before.recurrence);
		Set<Event> otherOccurrences = before.generateOccurrencesMatchingEventExceptions(this.recurrence);
		Set<Event> differences = Sets.difference(ownOccurrences, otherOccurrences, new ComparatorUsingEventHasImportantChanges());
		return Lists.newArrayList(differences);
	}

	private HashSet<Event> generateOccurrencesMatchingEventExceptions(EventRecurrence recurrence) {
		HashSet<Event> occurrences = com.google.common.collect.Sets.newHashSet(this.getEventsExceptions());
		for (Event exception: recurrence.getEventExceptions()) {
			if (!occurrences.contains(exception)) {
				Event occurrence = getOccurrence(exception.recurrenceId);
				occurrences.add(occurrence);
			}
		}
		return occurrences;
	}

	public Collection<Date> getNegativeExceptionsChanges(Event before) {
		if (!isRecurrent()) {
			return ImmutableSet.of();
		}
		if (before == null) {
			return ImmutableSet.copyOf(recurrence.getExceptions());
		}
		Collection<Date> changes = recurrence.getNegativeExceptionsChanges(before.getRecurrence());
		final Map<Date, Event> recurrenceIds = indexEventExceptionsByRecurrenceId(getDeletedEventExceptions(before));

		return filterOutExceptionsFromDeletedEventExceptions(changes, recurrenceIds);
	}

	private Collection<Date> filterOutExceptionsFromDeletedEventExceptions(
			Collection<Date> changes, final Map<Date, Event> recurrenceIds) {
		return Collections2.filter(changes, new Predicate<Date>() {
			@Override
			public boolean apply(Date input) {
				return !recurrenceIds.containsKey(input);
		}});
	}

	public boolean hasChangesExceptedEventException(Event event) {
		if(event == null){
			return true;
		}
		AllEventAttributesExceptExceptionsEquivalence comparator = new AllEventAttributesExceptExceptionsEquivalence();
		return !comparator.doEquivalent(this, event);
	}

	public Event getOccurrence(Date recurrenceId) {
		Event occurrence = recurrence.getEventExceptionWithRecurrenceId(recurrenceId);
		if (occurrence == null) {
			occurrence = buildOccurrence(recurrenceId);
		}
		return occurrence;
	}

	private Event buildOccurrence(Date recurrenceId) {
		Event occurrence = clone();
		occurrence.startDate = recurrenceId;
		occurrence.recurrenceId = recurrenceId;
		occurrence.recurrence = new EventRecurrence();
		occurrence.recurrence.setKind(RecurrenceKind.none);
		return occurrence;
	}

	public void addEventException(Event eventException) {
		this.recurrence.addEventException(eventException);
	}

	public boolean isRecurrent() {
		return getRecurrence().isRecurrent();
	}

	public Set<Event> getEventsExceptions() {
		return recurrence.getEventExceptions();
	}

	public void addException(Date recurrenceId) {
		recurrence.addException(recurrenceId);
	}
	
	public void updateParticipation() {
		changeAttendeesParticipation();
		Set<Event> eventsExceptions = getEventsExceptions();
		for (Event event: eventsExceptions) {
			event.changeAttendeesParticipation();
		}
	}
	
	private void changeAttendeesParticipation() {
		for (Attendee att: getAttendees()) {
			if (att.isCanWriteOnCalendar()) {
				att.setParticipation(Participation.accepted());
			} else {
				att.setParticipation(Participation.needsAction());
			}
			att.getParticipation().setComment(Comment.EMPTY);
		}
	}
	

	public boolean hasAnyExceptionAtDate(Date exceptionDateToFind) {
		return recurrence.hasAnyExceptionAtDate(exceptionDateToFind);
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(title, description, uid, extId, privacy, owner,
				ownerDisplayName, ownerEmail, creatorDisplayName, creatorEmail, location,
				startDate, duration, alert, category, priority, allday, attendees, recurrence, type,
				completion, percent, opacity, entityId, timeUpdate, timeCreate, timezoneName,
				recurrenceId, internalEvent, sequence, meetingStatus);
	}
	
	@Override
	public final boolean equals(Object object) {
		if (object instanceof Event) {
			Event that = (Event) object;
			return Objects.equal(this.title, that.title)
				&& Objects.equal(this.description, that.description)
				&& Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.extId, that.extId)
				&& Objects.equal(this.privacy, that.privacy)
				&& Objects.equal(this.meetingStatus, that.meetingStatus)
				&& Objects.equal(this.owner, that.owner)
				&& Objects.equal(this.ownerDisplayName, that.ownerDisplayName)
				&& Objects.equal(this.ownerEmail, that.ownerEmail)
				&& Objects.equal(this.creatorDisplayName, that.creatorDisplayName)
				&& Objects.equal(this.creatorEmail, that.creatorEmail)
				&& Objects.equal(this.location, that.location)
				&& Objects.equal(this.startDate, that.startDate)
				&& Objects.equal(this.duration, that.duration)
				&& Objects.equal(this.alert, that.alert)
				&& Objects.equal(this.category, that.category)
				&& Objects.equal(this.priority, that.priority)
				&& Objects.equal(this.allday, that.allday)
				&& Objects.equal(this.attendees, that.attendees)
				&& Objects.equal(this.recurrence, that.recurrence)
				&& Objects.equal(this.type, that.type)
				&& Objects.equal(this.completion, that.completion)
				&& Objects.equal(this.percent, that.percent)
				&& Objects.equal(this.opacity, that.opacity)
				&& Objects.equal(this.entityId, that.entityId)
				&& Objects.equal(this.timeUpdate, that.timeUpdate)
				&& Objects.equal(this.timeCreate, that.timeCreate)
				&& Objects.equal(this.timezoneName, that.timezoneName)
				&& Objects.equal(this.recurrenceId, that.recurrenceId)
				&& Objects.equal(this.internalEvent, that.internalEvent)
				&& Objects.equal(this.sequence, that.sequence);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("super", super.toString())
			.add("title", title)
			.add("uid", uid)
			.add("date", startDate)
			.toString();
	}

	@Override
	public Event anonymizePrivateItems() {
		Preconditions.checkState(this.privacy == EventPrivacy.PRIVATE
				|| this.privacy == EventPrivacy.PUBLIC, "Cannot handle the privacy level "
				+ this.privacy);
		if (this.privacy == EventPrivacy.PRIVATE) {
			return this.anonymize();
		} else {
			return this.clone();
		}
	}

	private Event anonymize() {
		Event anonymizedEvent = new Event();

		anonymizedEvent.owner = this.owner;
		anonymizedEvent.ownerDisplayName = this.ownerDisplayName;
		anonymizedEvent.ownerEmail = this.ownerEmail;
		anonymizedEvent.creatorDisplayName = this.creatorDisplayName;
		anonymizedEvent.creatorEmail = this.creatorEmail;

		anonymizedEvent.uid = this.uid;
		anonymizedEvent.extId = this.extId;
		anonymizedEvent.entityId = this.entityId;

		anonymizedEvent.type = this.type;
		anonymizedEvent.internalEvent = this.internalEvent;
		anonymizedEvent.sequence = this.sequence;

		anonymizedEvent.allday = this.allday;
		anonymizedEvent.startDate = this.startDate;
		anonymizedEvent.duration = this.duration;

		anonymizedEvent.timeCreate = this.timeCreate;
		anonymizedEvent.timeUpdate = this.timeUpdate;
		anonymizedEvent.timezoneName = this.timezoneName;

		anonymizedEvent.privacy = this.privacy;
		anonymizedEvent.priority = this.priority;
		anonymizedEvent.completion = this.completion;
		anonymizedEvent.opacity = this.opacity;

		anonymizedEvent.percent = this.percent;

		anonymizedEvent.recurrenceId = this.recurrenceId;
		anonymizedEvent.recurrence = this.recurrence.anonymizePrivateItems();

		return anonymizedEvent;
	}

	public void addOrReplaceAttendee(String emailOfAttendeeToReplace, Attendee newAttendee) {
		Preconditions.checkNotNull(newAttendee);
		for (Attendee attendee : attendees) {
			if (emailOfAttendeeToReplace.equalsIgnoreCase(attendee.getEmail())) {
				attendees.remove(attendee);
				break;
			}
		}
		attendees.add(newAttendee);
	}

	public boolean belongsToCalendar(String calendarName) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(calendarName));
		return calendarName.equalsIgnoreCase(ownerEmail);
	}
	
}
