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

import java.util.Date;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.base.Category;
import org.obm.sync.items.AbstractItemsWriter;
import org.obm.sync.items.EventChanges;
import org.obm.sync.items.ParticipationChanges;
import org.obm.sync.utils.DateHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Serializes calendar related items to XML
 */
public class CalendarItemsWriter extends AbstractItemsWriter {

	public Document writeChanges(EventChanges eventChanges) {
		Document doc = null;
		try {
			doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/calendar-changes.xsd",
					"calendar-changes");
			Element root = doc.getDocumentElement();
			root.setAttribute("lastSync", DateHelper.asString(eventChanges
				.getLastSync()));

			writeRemovedEvents(eventChanges, root);
			writeUpdatedEvents(eventChanges, root);
			writeParticipationChanges(eventChanges, root);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return doc;
	}

	private void writeRemovedEvents(EventChanges eventChanges, Element root) {
		Element removed = DOMUtils.createElement(root, "removed");
		EventObmId[] rmIds = eventChanges.getRemoved();
		EventExtId[] rmExtIds = eventChanges.getRemovedExtIds();
		for (int i = 0; i < rmIds.length; i++) {
			appendRemovedEvent(removed, rmIds, rmExtIds, i);
		}
	}

	private void writeUpdatedEvents(EventChanges eventChanges, Element root) {
		Element updated = DOMUtils.createElement(root, "updated");
		for (Event ev : eventChanges.getUpdated()) {
			appendUpdatedEvent(updated, ev);
		}
	}

	private void writeParticipationChanges(EventChanges eventChanges, Element root) {
		Element participationChanges = DOMUtils.createElement(root, "participationChanges");
		for (ParticipationChanges changes : eventChanges.getParticipationUpdated()) {
			appendParticipationChange(participationChanges, changes);
		}
	}

	public void appendUpdatedEvent(Element parent, Event event) {
		appendEvent(parent, event, "event");
	}

	private void appendRemovedEvent(Element removed, EventObmId[] rmIds,
			EventExtId[] rmExtIds, int i) {
		Element e = DOMUtils.createElement(removed, "event");
		e.setAttribute("id", rmIds[i].serializeToString());
		e.setAttribute("extId", rmExtIds[i].serializeToString());
	}

	private void appendParticipationChange(Element parent,	ParticipationChanges participationChanges) {
		Element participation = DOMUtils.createElement(parent, "participation");
		setParticipationChangeAttributes(participationChanges, participation);
		appendAttendeesForParticipationChange(participationChanges, participation);
	}

	private void setParticipationChangeAttributes(ParticipationChanges changes,
			Element participation) {
		participation.setAttribute("id", changes.getEventId().serializeToString());
		participation.setAttribute("extId", changes.getEventExtId().serializeToString());
		if (changes.getRecurrenceId() != null) {
			participation.setAttribute("recurrenceId", changes.getRecurrenceId().serializeToString());
		}
	}

	private void appendAttendeesForParticipationChange(ParticipationChanges changes,
			Element participation) {
		Element attendees = DOMUtils.createElement(participation, "attendees");
		for (Attendee a: changes.getAttendees()) {
			appendAttendeeForParticipationChange(attendees, a);
		}
	}

	private void appendAttendeeForParticipationChange(Element attendees, Attendee attendee) {
		Element at = DOMUtils.createElement(attendees, "attendee");
		at.setAttribute("email", attendee.getEmail());
		ParticipationState status = attendee.getState();
		at.setAttribute("state", (status != null ? status.toString() : ParticipationState.NEEDSACTION.toString()));
		appendCommentFromParticipationFor(at, status);
	}

	private void appendCommentFromParticipationFor(Element at,
			ParticipationState status) {
		if(status.hasDefinedComment()) {
			Comment comment = status.getComment();
			at.setAttribute("comment", comment.serializeToString());
		}
	}

	private void appendEvent(Element parent, Event event, String eventNodeName) {
		Element e = parent;
		if (!eventNodeName.equals(parent.getNodeName())) {
			e = DOMUtils.createElement(parent, eventNodeName);
		}
		e.setAttribute("type", event.getType().toString());
		e.setAttribute("allDay", "" + event.isAllday());
		EventObmId eventId = event.getObmId();
		if (eventId != null) {
			e.setAttribute("id", eventId.serializeToString());
		}
		e.setAttribute("sequence", String.valueOf(event.getSequence()));
		e.setAttribute("isInternal", ""+event.isInternalEvent());
		if (event.getTimeUpdate() != null) {
			createIfNotNull(e, "timeupdate", ""
					+ DateHelper.asString(event.getTimeUpdate()));
		}
		if (event.getTimeCreate() != null) {
			createIfNotNull(e, "timecreate", ""
					+ DateHelper.asString(event.getTimeCreate()));
		}
		if (event.getRecurrenceId() != null) {
			createIfNotNull(e, "recurrenceId", DateHelper.asString(event
					.getRecurrenceId()));
		}
		if (event.getExtId() != null) {
			createIfNotNull(e, "extId", event.getExtId().serializeToString());
		}
		if(event.getOpacity() == null){
			event.setOpacity(EventOpacity.OPAQUE);
		}
		createIfNotNull(e, "opacity", event.getOpacity().toString());
		createIfNotNull(e, "title", event.getTitle());
		createIfNotNull(e, "description", event.getDescription());
		createIfNotNull(e, "owner", event.getOwner());
		createIfNotNull(e, "ownerEmail", event.getOwnerEmail());
		createIfNotNull(e, "tz", event.getTimezoneName());
		if (event.getStartDate() != null) {
			createIfNotNull(e, "date", DateHelper.asString(event.getStartDate()));
		}
		createIfNotNull(e, "duration", "" + event.getDuration());
		createIfNotNull(e, "category", event.getCategory());
		createIfNotNull(e, "location", event.getLocation());
		if (event.getAlert() != null) {
			createIfNotNull(e, "alert", "" + event.getAlert());
		}

		createIfNotNull(e, "priority", (event.getPriority() != null ? ""
				+ event.getPriority() : "0"));
		createIfNotNull(e, "privacy", String.valueOf(event.getPrivacy().toSqlIntCode()));
		Element atts = DOMUtils.createElement(e, "attendees");
		List<Attendee> la = event.getAttendees();
		if (la != null) {
			for (Attendee a : event.getAttendees()) {
				appendAttendee(atts, a);
			}
		}

		Element rec = DOMUtils.createElement(e, "recurrence");
		EventRecurrence r = event.getRecurrence();
		rec.setAttribute("kind", r.getKind().toString());
		if (RecurrenceKind.none != r.getKind()) {
			if (r.getEnd() != null) {
				rec.setAttribute("end", DateHelper.asString(r.getEnd()));
			}
			if (r.getKind() == RecurrenceKind.weekly
					|| r.getKind() == RecurrenceKind.daily) {
				rec.setAttribute("days", new RecurrenceDaysSerializer().serialize(r.getDays()));
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
		at.setAttribute("required", (a.getParticipationRole() != null ? a.getParticipationRole()
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
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/event.xsd", "event");
		Element root = doc.getDocumentElement();
		appendUpdatedEvent(root, event);
		return DOMUtils.serialize(doc);
	}

	public String getListEventString(List<Event> events) throws TransformerException {
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/events.xsd", "events");
		Element root = doc.getDocumentElement();
		for (Event event : events) {
			appendUpdatedEvent(root, event);
		}
		return DOMUtils.serialize(doc);
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
		String out = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/freeBusyRequest.xsd",
					"freeBusyRequest");
			Element root = doc.getDocumentElement();
			appendFreeBusyRequest(root, fbr);
			out = DOMUtils.serialize(doc);
		} catch (TransformerException e) {
			logger.error("Error writing freebusy as string", e);
		}
		return out;
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
		String out = "";
		try {
			Document doc = DOMUtils.createDoc(
					"http://www.obm.org/xsd/sync/freeBusy.xsd", "freeBusy");
			Element root = doc.getDocumentElement();
			appendFreeBusy(root, fb);
			out = DOMUtils.serialize(doc);
		} catch (TransformerException e) {
			logger.error("Error writing freebusy as string", e);
		}
		return out;
	}
}
