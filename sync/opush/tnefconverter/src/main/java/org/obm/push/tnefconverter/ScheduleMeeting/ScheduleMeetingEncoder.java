package org.obm.push.tnefconverter.ScheduleMeeting;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.Set;

import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.Attendee;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;

import org.columba.ristretto.message.Address;
import org.obm.push.tnefconverter.helper.ICSHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduleMeetingEncoder {

	private Logger logger = LoggerFactory.getLogger(getClass());

	protected String obmSyncHost;

	private ScheduleMeeting meeting;
	private Calendar ics;
	private VEvent ntefEvent;
	private String title;
	private Address owner;
	private Set<Address> attRequired;
	private Set<Address> attOptional;

	public ScheduleMeetingEncoder(ScheduleMeeting meeting, String title,
			Address owner, Set<Address> attRequired, Set<Address> attOptional) {
		this.meeting = meeting;
		this.title = title;
		this.owner = owner;
		this.attRequired = attRequired;
		this.attOptional = attOptional;
	}

	
//	METHOD:REPLY
//	BEGIN:VEVENT
//	LOCATION:Tlse
//	STATUS:CONFIRMED
//	END:VEVENT
//	END:VCALENDAR

	public String encodeToIcs() {
		ics = ICSHelper.initCalendar();
		appendMethod();
		ntefEvent = new VEvent();
		ics.getComponents().add(ntefEvent);
		appendUID();
		appendSummary();
		appendLocation();
		appendDescription();
		appendClass();
		appendDtStart();
		appendDtEnd();
		appendOrganizer();
		appendAttendee();
		appendRRule();
		appendRecurrenceId();
		appendStatus();
		return ics.toString();
	}

	private void appendStatus() {
		switch (meeting.getMethod()) {
		case ScheduleMeetingRespPos:
			ntefEvent.getProperties().add(Status.VEVENT_CONFIRMED);
			break;
		case ScheduleMeetingRespNeg:
			ntefEvent.getProperties().add(Status.VEVENT_CANCELLED);
			break;
		case ScheduleMeetingRespTent:
			ntefEvent.getProperties().add(Status.VEVENT_TENTATIVE);
			break;
		case ScheduleMeetingCanceled:
			break;
		case ScheduleMeetingRequest:
			break;
		}
	}

	private void appendRecurrenceId() {
		Date val = meeting.getRecurrenceId();
		if (val != null) {
			DateTime dt = new DateTime(val);
			RecurrenceId recId = new RecurrenceId(dt);
			ntefEvent.getProperties().add(recId);
		}
	}

	// TODO IMPLEMENTS MONTHLY_NDAY and YEARLY_NDAY
	// (recur.setWeekStartDay(weekStartDay))
	private void appendRRule() {

		if (meeting.isRecurring() != null && meeting.isRecurring()) {
			String frequency = "";
			OldRecurrenceType kindRecur = meeting.getOldRecurrenceType();
			if (OldRecurrenceType.DAILY.equals(kindRecur)) {
				frequency = Recur.DAILY;
			} else if (OldRecurrenceType.WEEKLY.equals(kindRecur)) {
				frequency = Recur.WEEKLY;
			} else if (OldRecurrenceType.MONTHLY.equals(kindRecur)) {
				frequency = Recur.MONTHLY;
			} else if (OldRecurrenceType.MONTHLY.equals(kindRecur)
					|| OldRecurrenceType.MONTHLY_NDAY.equals(kindRecur)) {
				frequency = Recur.MONTHLY;
			} else if (OldRecurrenceType.YEARLY.equals(kindRecur)
					|| OldRecurrenceType.YEARLY_NDAY.equals(kindRecur)) {
				frequency = Recur.YEARLY;
			} else {
				frequency = "";
			}
			
			Recur recur = new Recur(frequency, null);
			recur.setInterval(meeting.getInterval());
			RRule rrule = new RRule(recur);
			ntefEvent.getProperties().add(rrule);
		}
	}

	private void appendAttendee() {
		for (Address add : attRequired) {
			Attendee att = createAttendee(add, Role.REQ_PARTICIPANT);
			ntefEvent.getProperties().add(att);
		}

		for (Address add : attOptional) {
			Attendee att = createAttendee(add, Role.OPT_PARTICIPANT);
			ntefEvent.getProperties().add(att);
		}
	}

	private Attendee createAttendee(Address add, Role role) {
		Attendee att = new Attendee();
		att.getParameters().add(CuType.INDIVIDUAL);
		att.getParameters().add(PartStat.NEEDS_ACTION);
		if (isNotEmpty(add.getDisplayName())) {
			att.getParameters().add(new Cn(add.getDisplayName()));
		} else {
			att.getParameters().add(new Cn(add.getMailAddress()));
		}
		att.getParameters().add(role);
		try {
			att.setValue("mailto:" + add.getMailAddress());
		} catch (URISyntaxException e) {
			logger.error("Error while parsing mail address "
					+ add.getMailAddress());
		}
		return att;
	}

	private void appendOrganizer() {
		if (owner != null) {
			Organizer orga = new Organizer();
			try {
				if (isNotEmpty(owner.getDisplayName())) {
					orga.getParameters().add(new Cn(owner.getDisplayName()));
				}
				if (isNotEmpty(owner.getMailAddress())) {
					orga.setValue("mailto:" + owner.getMailAddress());
				}
				ntefEvent.getProperties().add(orga);
			} catch (URISyntaxException e) {
				logger.error("Error while parsing mail address "
						+ owner.getMailAddress());
			}
		}
	}

	private void appendDtStart() {
		if (meeting.getStartDate() != null) {
			net.fortuna.ical4j.model.Date dt = null;
			if (meeting.isAllDay()) {
				dt = new net.fortuna.ical4j.model.Date(meeting.getStartDate()
						.getTime() + 43200000);
			} else {
				dt = new DateTime(meeting.getStartDate());
			}
			ntefEvent.getProperties().add(new DtStart(dt));
		}
	}

	private void appendDtEnd() {
		if (!meeting.isAllDay()) {
			DtEnd dtEnd = new DtEnd(new DateTime(meeting.getEndDate()));
			ntefEvent.getProperties().add(dtEnd);
		}
	}

	private void appendClass() {
		if (meeting.getClazz().equals(1)) {
			ntefEvent.getProperties().add(Clazz.PRIVATE);
		} else {
			ntefEvent.getProperties().add(Clazz.PUBLIC);
		}
	}

	private void appendDescription() {
		if (isNotEmpty(meeting.getDescription())) {
			ntefEvent.getProperties().add(
					new Description(meeting.getDescription()));
		}
	}

	private void appendLocation() {
		if (isNotEmpty(meeting.getLocation())) {
			ntefEvent.getProperties().add(new Location(meeting.getLocation()));
		}
	}

	private void appendSummary() {
		if (isNotEmpty(this.title)) {
			ntefEvent.getProperties().add(new Summary(this.title.trim()));
		}
	}

	private void appendMethod() {
		PidTagMessageClass method = meeting.getMethod();
		switch (method) {
		case ScheduleMeetingRequest:
			ics.getProperties().add(Method.REQUEST);
			break;
		case ScheduleMeetingCanceled:
			ics.getProperties().add(Method.CANCEL);
			break;
		case ScheduleMeetingRespPos:
		case ScheduleMeetingRespNeg:
		case ScheduleMeetingRespTent:
			ics.getProperties().add(Method.REPLY);
			break;
		}
	}

	private void appendUID() {
		ntefEvent.getProperties().add(new Uid(meeting.getUID()));
	}

	private boolean isNotEmpty(String val) {
		return val != null && !"".equals(val);
	}
}
