package org.obm.push.calendar;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.obm.push.bean.AttendeeStatus;
import org.obm.push.bean.AttendeeType;
import org.obm.push.bean.BackendSession;
import org.obm.push.bean.CalendarBusyStatus;
import org.obm.push.bean.CalendarSensitivity;
import org.obm.push.bean.IApplicationData;
import org.obm.push.bean.MSAttendee;
import org.obm.push.bean.MSEvent;
import org.obm.push.bean.Recurrence;
import org.obm.push.bean.RecurrenceDayOfWeek;
import org.obm.push.bean.RecurrenceType;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.calendar.RecurrenceKind;

/**
 * Convert events between OBM-Sync object model & Microsoft object model
 */
public class EventConverter implements ObmSyncCalendarConverter{

	@Override
	public IApplicationData convert(BackendSession bs, Event e) {
		MSEvent mse = new MSEvent();

		mse.setSubject(e.getTitle());
		mse.setDescription(e.getDescription());
		mse.setLocation(e.getLocation());
		mse.setTimeZone(TimeZone.getTimeZone("Europe/Paris"));
		mse.setStartTime(e.getDate());
		mse.setExceptionStartTime(e.getRecurrenceId());

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		c.setTimeInMillis(e.getDate().getTime());
		c.add(Calendar.SECOND, e.getDuration());
		mse.setEndTime(c.getTime());
		
		appendAttendeesAndOrganizer(bs, e, mse);
		
		
		mse.setAllDayEvent(e.isAllday());
		mse.setRecurrence(getRecurrence(e.getRecurrence()));
		mse.setExceptions(getException(bs, e.getRecurrence()));

		if (e.getAlert() != null && e.getAlert() > 0) {
			mse.setReminder(e.getAlert() / 60);
		}
		mse.setUID(e.getExtId());
		mse.setObmUID(e.getUid());
		mse.setBusyStatus(busyStatus(e.getOpacity()));
		mse.setSensitivity(getSensitivity(e.getPrivacy()));
		mse.setObmSequence(e.getSequence());
		appendCreatedLastUpdate(mse, e);
		return mse;
	}

	private void appendAttendeesAndOrganizer(BackendSession bs, Event e, MSEvent mse) {
		boolean hasOrganizer = false;
		for (Attendee at : e.getAttendees()) {
			if(at.isOrganizer()){
				hasOrganizer = true;
				appendOrganizer(mse, at);
			} 
			if(!hasOrganizer && bs.getLoginAtDomain().equals(at.getEmail())){
				appendOrganizer(mse, at);
			}
			mse.addAttendee(convertAttendee(at));
		}
	}

	private void appendOrganizer(MSEvent mse, Attendee at) {
		mse.setOrganizerName(at.getDisplayName());
		mse.setOrganizerEmail(at.getEmail());		
	}

	private void appendCreatedLastUpdate(MSEvent mse, Event e) {
		mse.setCreated(e.getTimeCreate() != null ? e.getTimeCreate() : new Date());
		mse.setLastUpdate(e.getTimeUpdate() != null ? e.getTimeUpdate() : new Date());
		mse.setDtStamp(mse.getLastUpdate());
	}

	private CalendarSensitivity getSensitivity(int privacy) {
		if(privacy == 1){
			return CalendarSensitivity.PRIVATE;
		}
		return CalendarSensitivity.NORMAL;
	}

	private List<MSEvent> getException(BackendSession bs, EventRecurrence recurrence) {
		List<MSEvent> ret = new LinkedList<MSEvent>();
		if(recurrence == null){
			return ret;
		}
		
		for (Date excp : recurrence.getExceptions()) {
			MSEvent e = new MSEvent();
			e.setDeleted(true);
			e.setExceptionStartTime(excp);
			e.setStartTime(excp);
			e.setDtStamp(new Date());
			ret.add(e);
		}

		for (Event excp : recurrence.getEventExceptions()) {
			MSEvent e = (MSEvent) convert(bs, excp);
			ret.add(e);
		}
		return ret;
	}

	private CalendarBusyStatus busyStatus(EventOpacity opacity) {
		switch (opacity) {
		case TRANSPARENT:
			return CalendarBusyStatus.FREE;
		default:
			return CalendarBusyStatus.BUSY;
		}
	}

	private EventRecurrence getRecurrence(MSEvent msev) {
		Date startDate = msev.getStartTime();
		Recurrence pr = msev.getRecurrence();
		EventRecurrence or = new EventRecurrence();
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

		int multiply = 0;
		switch (pr.getType()) {
		case DAILY:
			or.setKind(RecurrenceKind.daily);
			or.setDays(getDays(pr.getDayOfWeek()));
			multiply = Calendar.DAY_OF_MONTH;
			break;
		case MONTHLY:
			or.setKind(RecurrenceKind.monthlybydate);
			multiply = Calendar.MONTH;
			break;
		case MONTHLY_NDAY:
			or.setKind(RecurrenceKind.monthlybyday);
			multiply = Calendar.MONTH;
			break;
		case WEEKLY:
			or.setKind(RecurrenceKind.weekly);
			or.setDays(getDays(pr.getDayOfWeek()));
			multiply = Calendar.WEEK_OF_YEAR;
			break;
		case YEARLY:
			or.setKind(RecurrenceKind.yearly);
			cal.setTimeInMillis(startDate.getTime());
			cal.set(Calendar.DAY_OF_MONTH, pr.getDayOfMonth());
			cal.set(Calendar.MONTH, pr.getMonthOfYear() - 1);
			msev.setStartTime(cal.getTime());
			or.setFrequence(1);
			multiply = Calendar.YEAR;
			break;
		case YEARLY_NDAY:
			or.setKind(RecurrenceKind.yearly);
			multiply = Calendar.YEAR;
			break;
		}

		// interval
		if (pr.getInterval() != null) {
			or.setFrequence(pr.getInterval());
		}

		// occurence or end date
		Date endDate = null;
		if (pr.getOccurrences() != null && pr.getOccurrences() > 0) {
			cal.setTimeInMillis(startDate.getTime());
			cal.add(multiply, pr.getOccurrences() - 1);
			endDate = new Date(cal.getTimeInMillis());
		} else {
			endDate = pr.getUntil();
		}
		or.setEnd(endDate);

		return or;
	}

	private String getDays(Set<RecurrenceDayOfWeek> dayOfWeek) {
		StringBuilder sb = new StringBuilder();
		if (dayOfWeek == null) {
			return "0000000";
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.SUNDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.MONDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.TUESDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.WEDNESDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.THURSDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.FRIDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		if (dayOfWeek.contains(RecurrenceDayOfWeek.SATURDAY)) {
			sb.append(1);
		} else {
			sb.append(0);
		}
		return sb.toString();
	}

	private Recurrence getRecurrence(EventRecurrence recurrence) {
		if (recurrence == null || recurrence.getKind() == RecurrenceKind.none) {
			return null;
		}

		Recurrence r = new Recurrence();
		switch (recurrence.getKind()) {
		case daily:
			r.setType(RecurrenceType.DAILY);
			break;
		case monthlybydate:
			r.setType(RecurrenceType.MONTHLY);
			break;
		case monthlybyday:
			r.setType(RecurrenceType.MONTHLY_NDAY);
			break;
		case weekly:
			r.setType(RecurrenceType.WEEKLY);
			r.setDayOfWeek(daysOfWeek(recurrence.getDays()));
			break;
		case yearly:
			r.setType(RecurrenceType.YEARLY);
			break;
		case none:
			r.setType(null);
			break;
		}
		r.setUntil(recurrence.getEnd());

		r.setInterval(recurrence.getFrequence());

		return r;
	}

	private Set<RecurrenceDayOfWeek> daysOfWeek(String string) {
		char[] days = string.toCharArray();
		Set<RecurrenceDayOfWeek> daysList = new HashSet<RecurrenceDayOfWeek>();
		int i = 0;
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.SUNDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.MONDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.TUESDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.WEDNESDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.THURSDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.FRIDAY);
		}
		if (days[i++] == '1') {
			daysList.add(RecurrenceDayOfWeek.SATURDAY);
		}

		return daysList;
	}

	private MSAttendee convertAttendee(Attendee at) {
		MSAttendee msa = new MSAttendee();

		msa.setAttendeeStatus(status(at.getState()));
		msa.setEmail(at.getEmail());
		msa.setName(at.getDisplayName());
		msa.setAttendeeType(type());

		return msa;
	}

	private AttendeeType type() {
		return AttendeeType.REQUIRED;
	}

	private AttendeeStatus status(ParticipationState state) {
		switch (state) {
		case COMPLETED:
		case DELEGATED:
			return AttendeeStatus.RESPONSE_UNKNOWN;
		case DECLINED:
			return AttendeeStatus.DECLINE;
		case INPROGRESS:
		case NEEDSACTION:
			return AttendeeStatus.NOT_RESPONDED;
		case TENTATIVE:
			return AttendeeStatus.TENTATIVE;
		default:
		case ACCEPTED:
			return AttendeeStatus.ACCEPT;
		}
	}
	
	@Override
	public Event convertAsInternal(BackendSession bs, Event oldEvent, IApplicationData data) {
		return convert(bs, oldEvent, data, true);
	}

	@Override
	public Event convertAsExternal(BackendSession bs, Event oldEvent, IApplicationData data) {
		return convert(bs, oldEvent, data, false);
	}
	
	@Override
	public Event convertAsInternal(BackendSession bs, IApplicationData data) {
		return convert(bs, null, data, true);
	}

	@Override
	public Event convertAsExternal(BackendSession bs, IApplicationData data) {
		return convert(bs, null, data, false);
	}
	
	
	private Event convert(BackendSession bs, Event oldEvent, IApplicationData appliData, Boolean isObmInternalEvent) {
		MSEvent data = (MSEvent) appliData;
		Event e = convertEventOne(bs, oldEvent, null, data, isObmInternalEvent);
		e.setExtId(data.getUID());
		e.setUid(data.getObmUID());
		if(data.getObmSequence() != null){
			e.setSequence(data.getObmSequence());
		}
		
		if (data.getRecurrence() != null) {
			EventRecurrence r = getRecurrence(data);
			e.setRecurrence(r);
			if (data.getExceptions() != null && !data.getExceptions().isEmpty()) {
				for (MSEvent excep : data.getExceptions()) {
					if (!excep.isDeletedException()) {
						Event obmEvent = convertEventOne(bs, oldEvent, e, excep, isObmInternalEvent);
						r.addEventException(obmEvent);
					} else {
						r.addException(excep.getExceptionStartTime());
					}
				}
			}
		}

		return e;
	}

	// Exceptions.Exception.Body (section 2.2.3.9): This element is optional.
	// Exceptions.Exception.Categories (section 2.2.3.8): This element is
	// optional.
	private Event convertEventOne(BackendSession bs, Event oldEvent, Event parentEvent,
			MSEvent data, Boolean isObmInternalEvent) {
		Event e = new Event();
		defineOwner(bs, e);
		e.setInternalEvent(isObmInternalEvent);
		e.setType(EventType.VEVENT);
		if (parentEvent != null && parentEvent.getTitle() != null
				&& !parentEvent.getTitle().isEmpty()) {
			e.setTitle(parentEvent.getTitle());
		} else {
			e.setTitle(data.getSubject());
		}
		if (parentEvent != null && parentEvent.getDescription() != null
				&& !parentEvent.getDescription().isEmpty()) {
			e.setDescription(parentEvent.getDescription());
		} else {
			e.setDescription(data.getDescription());
		}
		e.setLocation(data.getLocation());
		e.setDate(data.getStartTime());
		int duration = (int) (data.getEndTime().getTime() - data.getStartTime()
				.getTime()) / 1000;
		e.setDuration(duration);
		e.setAllday(data.getAllDayEvent() != null ? data.getAllDayEvent() : false);
		e.setRecurrenceId(data.getExceptionStartTime());
		if (data.getReminder() != null && data.getReminder() > 0) {
			e.setAlert(data.getReminder() * 60);
		}

		if (data.getBusyStatus() == null) {
			if (parentEvent != null) {
				e.setOpacity(parentEvent.getOpacity());
			}
		} else {
			e.setOpacity(opacity(data.getBusyStatus()));
		}

		if (data.getSensitivity() == null && parentEvent != null) {
			e.setPrivacy(parentEvent.getPrivacy());
		} else {
			e.setPrivacy(privacy(oldEvent, data.getSensitivity()));
		}
		
		List<Attendee> atts = getAttendees(bs, oldEvent, parentEvent, data);
		e.setAttendees(atts);
		return e;
	}

	private void defineOwner(BackendSession bs, Event e) {
		e.setOwnerEmail(bs.getLoginAtDomain());
	}
	
	private int privacy(Event oldEvent, CalendarSensitivity sensitivity) {
		if (sensitivity == null) {
			return oldEvent != null ? oldEvent.getPrivacy() : 0;
		}
		switch (sensitivity) {
		case CONFIDENTIAL:
		case PERSONAL:
		case PRIVATE:
			return 1;
		case NORMAL:
		default:
			return 0;
		}

	}

	private EventOpacity opacity(CalendarBusyStatus busyStatus) {
		switch (busyStatus) {
		case FREE:
			return EventOpacity.TRANSPARENT;
		default:
			return EventOpacity.OPAQUE;
		}
	}

	private List<Attendee> getAttendees(BackendSession bs, Event oldEvent, Event parentEvent,
			MSEvent data) {
		List<Attendee> ret = new LinkedList<Attendee>();
		if (parentEvent != null && data.getAttendees().isEmpty()) {
			// copy parent attendees. CalendarBackend ensured parentEvent has
			// attendees.
			ret.addAll(parentEvent.getAttendees());
		} else {
			if(data.getOrganizerEmail() != null){
				MSAttendee organizer = new MSAttendee();
				organizer.setName(data.getOrganizerName());
				organizer.setEmail(data.getOrganizerEmail());
				organizer.setAttendeeStatus(AttendeeStatus.ACCEPT);
				organizer.setAttendeeType(AttendeeType.REQUIRED);
				ret.add(convertAttendee(oldEvent,data, organizer));
			}
			for (MSAttendee at : data.getAttendees()) {
				ret.add(convertAttendee(oldEvent,data, at));
			}
			
		}
		defineOrganizerAndOwner(bs, ret);
		return ret;
	}
	
	private void defineOrganizerAndOwner(BackendSession bs, List<Attendee> ret) {
		Attendee owner = null;
		boolean hasOrganizer = false;
		for(Attendee att : ret){
			hasOrganizer = hasOrganizer || att.isOrganizer();
			if(bs.getLoginAtDomain().equals(att.getEmail())){
				owner = att;
			}
		}
		if(owner == null){
			owner = getOwner(bs.getLoginAtDomain(), ParticipationState.ACCEPTED);
			ret.add(owner);
		}
		owner.setOrganizer(!hasOrganizer);
	}

	private Attendee getOwner(String email, ParticipationState state){
		Attendee att = new Attendee();
		att.setEmail(email);
		att.setState(state);
		att.setRequired(ParticipationRole.REQ);
		return att;
	}
	
	
	private Attendee convertAttendee(Event oldEvent, MSEvent event, MSAttendee at) {
		ParticipationState oldState = ParticipationState.NEEDSACTION;
		if (oldEvent != null) {
			for (Attendee oldAtt : oldEvent.getAttendees()) {
				if (oldAtt.getEmail().equals(at.getEmail())) {
					oldState = oldAtt.getState();
					break;
				}
			}
		}
		Attendee ret = new Attendee();
		ret.setEmail(at.getEmail());
		ret.setDisplayName(at.getName());
		ret.setRequired(ParticipationRole.REQ);
		ret.setState(status(oldState, at.getAttendeeStatus()));
		ret.setOrganizer(isOrganizer(event, at));
		return ret;
	}

	private Boolean isOrganizer(MSEvent event, MSAttendee at) {
		
		if(at.getEmail() != null  && at.getEmail().equals(event.getOrganizerEmail())){
			return true;
		} else if(at.getName() != null  && at.getName().equals(event.getOrganizerName())){
			return true;
		}
		return false;
	}

	public static ParticipationState status(ParticipationState oldParticipationState,
			AttendeeStatus attendeeStatus) {
		if (attendeeStatus == null) {
			return oldParticipationState;
		}
		switch (attendeeStatus) {
		case DECLINE:
			return ParticipationState.DECLINED;
		case NOT_RESPONDED:
		case RESPONSE_UNKNOWN:
		case TENTATIVE:
			return ParticipationState.NEEDSACTION;
		default:
		case ACCEPT:
			return ParticipationState.ACCEPTED;
		}
	}
	
	public static boolean isInternalEvent(Event event, boolean defaultValue){
		return event != null ? event.isInternalEvent() : defaultValue;
	}
}
