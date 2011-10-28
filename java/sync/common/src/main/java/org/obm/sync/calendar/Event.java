package org.obm.sync.calendar;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.obm.push.utils.index.Indexed;
import com.google.common.base.Objects;

public class Event implements Indexed<Integer> {

	private String title;
	private String domain;
	private String description;
	private EventObmId uid;
	private EventExtId extId;
	private int privacy;
	private String owner;
	private String ownerDisplayName;
	private String ownerEmail;
	private String location;
	private Date date;
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
	private EventOpacity opacity = EventOpacity.OPAQUE;

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
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return duration in second
	 */
	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public boolean isAllday() {
		return allday;
	}

	public void setAllday(boolean allDay) {
		this.allday = allDay;
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
	
	public void setAttendees(List<Attendee> attendees) {
		this.attendees = attendees;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/*
	 * public=0 et privé=1
	 */
	public int getPrivacy() {
		return privacy;
	}

	public void setPrivacy(int privacy) {
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

	public EventObmId getUid() {
		return uid;
	}

	public void setUid(EventObmId uid) {
		this.uid = uid;
	}

	public Integer getAlert() {
		return alert;
	}

	public void setAlert(Integer alert) {
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

	public Date getCompletion() {
		return completion;
	}

	public void setCompletion(Date completion) {
		this.completion = completion;
	}

	public Integer getPercent() {
		return percent;
	}

	public void setPercent(Integer percent) {
		this.percent = percent;
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

	public Integer getEntityId() {
		return entityId;
	}

	public void setEntityId(Integer entityId) {
		this.entityId = entityId;
	}

	public EventExtId getExtId() {
		return extId;
	}

	public void setExtId(EventExtId extId) {
		this.extId = extId;
	}

	public Date getTimeUpdate() {
		return timeUpdate;
	}

	public void setTimeUpdate(Date timeUpdate) {
		this.timeUpdate = timeUpdate;
	}

	public Event clone() {
		Event event = new Event();
		event.setAlert(alert);
		event.setAllday(allday);
		event.addAttendees(new LinkedList<Attendee>(attendees));
		event.setCategory(category);
		event.setCompletion(completion);
		event.setDate(date);
		event.setDescription(description);
		event.setDomain(domain);
		event.setDuration(duration);
		event.setEntityId(entityId);
		event.setExtId(extId);
		event.setLocation(location);
		event.setOpacity(opacity);
		event.setOwner(owner);
		event.setOwnerDisplayName(ownerDisplayName);
		event.setOwnerEmail(ownerEmail);
		event.setPercent(percent);
		event.setPriority(priority);
		event.setPrivacy(privacy);
		if (recurrence != null) {
			event.setRecurrence(recurrence.clone());
		}
		event.setTimeUpdate(timeUpdate);
		event.setTimezoneName(timezoneName);
		event.setTitle(title);
		event.setType(type);
		event.setUid(uid);
		event.setRecurrenceId(recurrenceId);
		event.setInternalEvent(internalEvent);
		event.setSequence(sequence);
		
		event.setTimeCreate(timeCreate);
		event.setTimeUpdate(timeUpdate);
		return event;
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
			if (getRecurrence() != null && getRecurrence().getEnd() != null && getRecurrence().getKind() != RecurrenceKind.none ) {
				return getRecurrence().getEnd().before(now);
			}
			return true;
		}
		return false;
	}

	public Date getEndDate() {
		if (getDate() == null) {
			return null;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getDate());
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

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getOwnerDisplayName() {
		return ownerDisplayName;
	}

	public void setOwnerDisplayName(String ownerDisplayName) {
		this.ownerDisplayName = ownerDisplayName;
	}

	public boolean modifiedSince(Date reference) {
		if (reference == null) {
			return true;
		}
		if (timeCreate.after(reference)) {
			return true;
		}
		if (timeUpdate != null && timeUpdate.after(reference)) {
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

	@Override
	public Integer getIndex() {
		return getUid().getIndex();
	}
	
	public boolean hasImportantChanges(Event event) {
		if ( !( Objects.equal(this.location, event.location)
				&& Objects.equal(this.date, event.date)
				&& Objects.equal(this.duration, event.duration)
				&& Objects.equal(this.recurrenceId, event.recurrenceId)
				&& Objects.equal(this.type, event.type)) ) {
			return true;
		}
		
		if (this.recurrence != null) {
			return this.recurrence.hasImportantChanges(event.getRecurrence());
		}
		
		return false;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(title, domain, description,
				uid, extId, privacy, owner, ownerDisplayName, ownerEmail,
				location, date, duration, alert, category, priority, allday,
				attendees, recurrence, type, completion, percent, opacity,
				entityId, timeUpdate, timeCreate, timezoneName, recurrenceId,
				internalEvent, sequence);
	}
	
	@Override
	public final boolean equals(Object object) {
		if (object instanceof Event) {
			Event that = (Event) object;
			return Objects.equal(this.title, that.title)
				&& Objects.equal(this.domain, that.domain)
				&& Objects.equal(this.description, that.description)
				&& Objects.equal(this.uid, that.uid)
				&& Objects.equal(this.extId, that.extId)
				&& Objects.equal(this.privacy, that.privacy)
				&& Objects.equal(this.owner, that.owner)
				&& Objects.equal(this.ownerDisplayName, that.ownerDisplayName)
				&& Objects.equal(this.ownerEmail, that.ownerEmail)
				&& Objects.equal(this.location, that.location)
				&& Objects.equal(this.date, that.date)
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
			.add("date", date)
			.toString();
	}
	
}