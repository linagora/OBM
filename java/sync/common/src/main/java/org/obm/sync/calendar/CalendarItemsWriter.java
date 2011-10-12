package org.obm.sync.calendar;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.obm.sync.base.Category;
import org.obm.sync.items.AbstractItemsWriter;
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.ParticipationChanges;
import org.obm.sync.utils.DOMUtils;
import org.obm.sync.utils.DateHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Serializes calendar related items to XML
 * 
 * @author tom
 * 
 */
public class CalendarItemsWriter extends AbstractItemsWriter {

	public Document writeChanges(EventChanges cc) {
		Document doc = null;
		try {
			doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/calendar-changes.xsd",
					"calendar-changes");
			Element root = doc.getDocumentElement();
			root
					.setAttribute("lastSync", DateHelper.asString(cc
							.getLastSync()));

			Element removed = DOMUtils.createElement(root, "removed");
			EventObmId[] rmIds = cc.getRemoved();
			EventExtId[] rmExtIds = cc.getRemovedExtIds();
			for (int i = 0; i < rmIds.length; i++) {
				Element e = DOMUtils.createElement(removed, "event");
				e.setAttribute("id", rmIds[i].serializeToString());
				e.setAttribute("extId", rmExtIds[i].serializeToString());
			}

			Element updated = DOMUtils.createElement(root, "updated");
			for (Event ev : cc.getUpdated()) {
				appendEvent(updated, ev);
			}
			Element participation = DOMUtils.createElement(root, "participationChanges");
			for (ParticipationChanges changes : cc.getParticipationUpdated()) {
				appendParticipationChanges(participation, changes);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return doc;
	}

	private void appendParticipationChanges(Element parent,	ParticipationChanges changes) {
		Element participation = DOMUtils.createElement(parent, "participation");
		participation.setAttribute("id", changes.getEventId().serializeToString());
		participation.setAttribute("extId", changes.getEventExtId().serializeToString());
		Element attendees = DOMUtils.createElement(participation, "attendees");
		for (Attendee a: changes.getAttendees()) {
			appendAttendeeForParticipation(attendees, a);
		}
	}

	private void appendAttendeeForParticipation(Element attendees, Attendee a) {
		Element at = DOMUtils.createElement(attendees, "attendee");
		at.setAttribute("email", a.getEmail());
		at.setAttribute("state", (a.getState() != null ? a.getState().toString() : ParticipationState.NEEDSACTION.toString()));
	}

	public void appendEvent(Element parent, Event ev) {
		appendEvent(parent, ev, "event");
	}

	private void appendEvent(Element parent, Event ev, String eventNodeName) {
		Element e = parent;
		if (!eventNodeName.equals(parent.getNodeName())) {
			e = DOMUtils.createElement(parent, eventNodeName);
		}
		e.setAttribute("type", ev.getType().toString());
		e.setAttribute("allDay", "" + ev.isAllday());
		EventObmId eventId = ev.getUid();
		if (eventId != null) {
			e.setAttribute("id", eventId.serializeToString());
		}
		e.setAttribute("sequence", String.valueOf(ev.getSequence()));
		e.setAttribute("isInternal", ""+ev.isInternalEvent());
		if (ev.getTimeUpdate() != null) {
			createIfNotNull(e, "timeupdate", ""
					+ DateHelper.asString(ev.getTimeUpdate()));
		}
		if (ev.getTimeCreate() != null) {
			createIfNotNull(e, "timecreate", ""
					+ DateHelper.asString(ev.getTimeCreate()));
		}
		if (ev.getRecurrenceId() != null) {
			createIfNotNull(e, "recurrenceId", DateHelper.asString(ev
					.getRecurrenceId()));
		}
		if (ev.getExtId() != null) {
			createIfNotNull(e, "extId", ev.getExtId().serializeToString());
		}
		if(ev.getOpacity() == null){
			ev.setOpacity(EventOpacity.OPAQUE);
		}
		createIfNotNull(e, "opacity", ev.getOpacity().toString());
		createIfNotNull(e, "title", ev.getTitle());
		createIfNotNull(e, "description", ev.getDescription());
		createIfNotNull(e, "owner", ev.getOwner());
		createIfNotNull(e, "ownerEmail", ev.getOwnerEmail());
		createIfNotNull(e, "tz", ev.getTimezoneName());
		if (ev.getDate() != null) {
			createIfNotNull(e, "date", DateHelper.asString(ev.getDate()));
		}
		createIfNotNull(e, "duration", "" + ev.getDuration());
		createIfNotNull(e, "category", ev.getCategory());
		createIfNotNull(e, "location", ev.getLocation());
		if (ev.getAlert() != null) {
			createIfNotNull(e, "alert", "" + ev.getAlert());
		}

		createIfNotNull(e, "priority", (ev.getPriority() != null ? ""
				+ ev.getPriority() : "0"));
		createIfNotNull(e, "privacy", "" + ev.getPrivacy());
		Element atts = DOMUtils.createElement(e, "attendees");
		List<Attendee> la = ev.getAttendees();
		if (la != null) {
			for (Attendee a : ev.getAttendees()) {
				appendAttendee(atts, a);
			}
		}

		if (ev.getRecurrence() == null) {
			EventRecurrence er = new EventRecurrence();
			er.setKind(RecurrenceKind.none);
			ev.setRecurrence(er);
		}

		Element rec = DOMUtils.createElement(e, "recurrence");
		EventRecurrence r = ev.getRecurrence();
		rec.setAttribute("kind", r.getKind().toString());
		if (RecurrenceKind.none != r.getKind()) {
			if (r.getEnd() != null) {
				rec.setAttribute("end", DateHelper.asString(r.getEnd()));
			}
			if (r.getKind() == RecurrenceKind.weekly
					|| r.getKind() == RecurrenceKind.daily) {
				rec.setAttribute("days", r.getDays());
				if (r.getFrequence() == 0) {
					r.setFrequence(1);
				}
			}

			rec.setAttribute("freq", "" + r.getFrequence());
			Element exc = DOMUtils.createElement(rec, "exceptions");
			for (Date ex : r.getExceptions()) {
				DOMUtils.createElementAndText(exc, "exception", DateHelper
						.asString(ex));
			}

			Element eventExp = DOMUtils.createElement(rec, "eventExceptions");
			for (Event ex : r.getEventExceptions()) {
				appendEvent(eventExp, ex, "eventException");
			}
		}
	}

	private Element appendAttendee(Element atts, Attendee a) {
		Element at = DOMUtils.createElement(atts, "attendee");
		at.setAttribute("displayName", a.getDisplayName());
		at.setAttribute("isOrganizer", ""+a.isOrganizer());
		at.setAttribute("email", a.getEmail());
		at.setAttribute("state", (a.getState() != null ? a.getState()
				.toString() : ParticipationState.NEEDSACTION.toString()));
		at.setAttribute("required", (a.getRequired() != null ? a.getRequired()
				.toString() : ParticipationRole.OPT.toString()));
		at.setAttribute("percent", "" + a.getPercent());
		return at;
	}

	public void appendInfo(Element root, CalendarInfo ci) {
		Element info = DOMUtils.createElement(root, "info");

		if (ci.getFirstname() != null) {
			DOMUtils.createElementAndText(info, "first", ci.getFirstname());
		}

		DOMUtils.createElementAndText(info, "last", ci.getLastname());
		DOMUtils.createElementAndText(info, "mail", ci.getMail());
		DOMUtils.createElementAndText(info, "uid", ci.getUid());
		DOMUtils.createElementAndText(info, "read", "" + ci.isRead());
		DOMUtils.createElementAndText(info, "write", "" + ci.isWrite());
	}

	public String getEventString(Event event) throws TransformerException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/event.xsd", "event");
		Element root = doc.getDocumentElement();
		appendEvent(root, event);
		DOMUtils.serialise(doc, out);
		return out.toString();
	}

	public String getListEventString(List<Event> events) throws TransformerException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/events.xsd", "events");
		Element root = doc.getDocumentElement();
		for (Event event : events) {
			appendEvent(root, event);
		}
		DOMUtils.serialise(doc, out);
		return out.toString();
	}

	public void appendCategory(Element root, Category c) {
		Element cat = DOMUtils.createElement(root, "cat");
		cat.setAttribute("id", "" + c.getId());
		cat.setAttribute("label", c.getLabel());
	}

	public void appendEventTimeUpdate(Element parent, EventTimeUpdate ev) {
		Element e = DOMUtils.createElement(parent, "eventTimeUpdate");
		e.setAttribute("id", ev.getUid());
		if (ev.getTimeUpdate() != null) {
			createIfNotNull(e, "timeupdate", ""
					+ DateHelper.asString(ev.getTimeUpdate()));
		}
		createIfNotNull(e, "extId", ev.getExtId());
		if (ev.getRecurrenceId() != null) {
			createIfNotNull(e, "recurrenceId", DateHelper.asString(ev
					.getRecurrenceId()));
		}
	}

	public void appendEventParticipationState(Element parent,
			EventParticipationState eps) {
		Element e = DOMUtils.createElement(parent, "eventParticipationState");
		e.setAttribute("id", eps.getUid());
		createIfNotNull(e, "title", eps.getTitle());
		if (eps.getDate() != null) {
			createIfNotNull(e, "date", DateHelper.asString(eps.getDate()));
		}
		createIfNotNull(e, "state", (eps.getState() != null ? eps.getState()
				.toString() : ParticipationState.NEEDSACTION.toString()));
		if (eps.getAlert() != null) {
			createIfNotNull(e, "alert", "" + eps.getAlert());
		}

	}

	public void appendFreeBusyRequest(Element parent, FreeBusyRequest freeBusy) {
		Element e = parent;

		if (freeBusy.getStart() != null) {
			createIfNotNull(e, "start", DateHelper
					.asString(freeBusy.getStart()));
		}
		if (freeBusy.getEnd() != null) {
			createIfNotNull(e, "end", DateHelper.asString(freeBusy.getEnd()));
		}
		createIfNotNull(e, "uid", freeBusy.getUid());
		createIfNotNull(e, "owner", freeBusy.getOwner());

		Element atts = DOMUtils.createElement(e, "attendees");
		List<Attendee> la = freeBusy.getAttendees();
		if (la != null) {
			for (Attendee a : la) {
				appendAttendee(atts, a);
			}
		}
	}

	public String getFreeBusyRequestString(FreeBusyRequest fbr) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/freeBusyRequest.xsd",
					"freeBusyRequest");
			Element root = doc.getDocumentElement();
			appendFreeBusyRequest(root, fbr);
			DOMUtils.serialise(doc, out);
		} catch (Exception e) {
			logger.error("Error writing freebusy as string", e);
		}
		return out.toString();
	}

	public void appendFreeBusy(Element e, FreeBusy freeBusy) {

		if (freeBusy.getStart() != null) {
			createIfNotNull(e, "start", DateHelper
					.asString(freeBusy.getStart()));
		}
		if (freeBusy.getEnd() != null) {
			createIfNotNull(e, "end", DateHelper.asString(freeBusy.getEnd()));
		}
		createIfNotNull(e, "uid", freeBusy.getUid());
		createIfNotNull(e, "owner", freeBusy.getOwner());

		appendAttendee(e, freeBusy.getAtt());
		for (FreeBusyInterval line : freeBusy.getFreeBusyIntervals()) {
			Element lineElem = DOMUtils.createElement(e, "freebusyinterval");
			if (line.getStart() != null) {
				createIfNotNull(lineElem, "start", DateHelper.asString(line
						.getStart()));
			}
			createIfNotNull(lineElem, "duration", "" + line.getDuration());
			createIfNotNull(lineElem, "allDay",
					(line.isAllDay() != null && line.isAllDay()) ? "true"
							: "false");
		}
	}

	public String getFreeBusyString(FreeBusy fb) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/freeBusy.xsd", "freeBusy");
			Element root = doc.getDocumentElement();
			appendFreeBusy(root, fb);
			DOMUtils.serialise(doc, out);
		} catch (Exception e) {
			logger.error("Error writing freebusy as string", e);
		}
		return out.toString();
	}
}
