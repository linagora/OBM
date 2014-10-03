/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import com.google.common.base.Strings;

/**
 * Serializes calendar related items to XML
 */
public class CalendarItemsWriter extends AbstractItemsWriter {

	public Document getXMLDocumentFrom(EventChanges eventChanges) {
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
		for(DeletedEvent deletedEvent: eventChanges.getDeletedEvents()) {
			appendRemovedEvent(removed, deletedEvent);
		}
	}

	private void writeUpdatedEvents(EventChanges eventChanges, Element root) {
		Element updated = DOMUtils.createElement(root, "updated");
		for (Event ev : eventChanges.getUpdated()) {
			Element event = DOMUtils.createElement(updated, "event");
			appendUpdatedEvent(event, ev);
		}
	}

	private void writeParticipationChanges(EventChanges eventChanges, Element root) {
		Element participationChanges = DOMUtils.createElement(root, "participationChanges");
		for (ParticipationChanges changes : eventChanges.getParticipationUpdated()) {
			appendParticipationChange(participationChanges, changes);
		}
	}

	public Document getXMLDocumentFrom(Event event) {
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/event.xsd", "event");
		Element root = doc.getDocumentElement();
		appendUpdatedEvent(root, event);
		return doc;
	}

	public Document getXMLDocumentFrom(ResourceInfo resourceInfo) {
		Document doc = DOMUtils.createDoc(
				"http://www.obm.org/xsd/sync/resourceinfo.xsd", "resourceInfo");
		Element root = doc.getDocumentElement();
		this.appendResourceInfoAttributes(root, resourceInfo);
		return doc;
	}

	public Document getXMLDocumentFrom(List<Event> events) {
		Document doc = DOMUtils.createDoc("http://www.obm.org/xsd/sync/events.xsd", "events");
		Element root = doc.getDocumentElement();
		for (Event event : events) {
			Element eventElement = DOMUtils.createElement(root, "event");
			appendUpdatedEvent(eventElement, event);
		}
		return doc;
	}

	private void appendUpdatedEvent(Element parent, Event event) {
		appendEvent(parent, event);
	}

	private void appendRemovedEvent(Element removed, DeletedEvent deletedEvent) {
		Element e = DOMUtils.createElement(removed, "event");
		e.setAttribute("id", deletedEvent.getId().serializeToString());
		e.setAttribute("extId", deletedEvent.getExtId().serializeToString());
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
		Participation participation = attendee.getParticipation();
		Participation nonNullParticipation = participation != null ? participation : Participation.needsAction();
		at.setAttribute("state", nonNullParticipation.toString());
		appendCommentFromParticipationFor(at, nonNullParticipation);
	}

	private void appendCommentFromParticipationFor(Element at,
			Participation participation) {
		if(participation.hasDefinedComment()) {
			Comment comment = participation.getComment();
			at.setAttribute("comment", comment.serializeToString());
		}
	}

	private void appendEvent(Element parent, Event event) {
		parent.setAttribute("type", event.getType().toString());
		parent.setAttribute("allDay", String.valueOf(event.isAllday()));
		EventObmId eventId = event.getObmId();
		if (eventId != null) {
			parent.setAttribute("id", eventId.serializeToString());
		}
		parent.setAttribute("sequence", String.valueOf(event.getSequence()));
		parent.setAttribute("isInternal", String.valueOf(event.isInternalEvent()));
		parent.setAttribute("anonymized", String.valueOf(event.isAnonymized()));
		createIfNotNull(parent, "hash", event.hashCode());
		createIfNotNull(parent, "timeupdate", DateHelper.asString(event.getTimeUpdate()));
		createIfNotNull(parent, "timecreate", DateHelper.asString(event.getTimeCreate()));
		createIfNotNull(parent, "recurrenceId", DateHelper.asString(event.getRecurrenceId()));
		createIfNotNull(parent, "extId", event.getExtId().serializeToString());
		if(event.getOpacity() == null){
			event.setOpacity(EventOpacity.OPAQUE);
		}
		createIfNotNull(parent, "opacity", event.getOpacity().toString());
		createIfNotNull(parent, "title", event.getTitle());
		createIfNotNull(parent, "description", event.getDescription());
		createIfNotNull(parent, "owner", event.getOwner());
		createIfNotNull(parent, "ownerEmail", event.getOwnerEmail());
		createIfNotNull(parent, "tz", event.getTimezoneName());
		createIfNotNull(parent, "date", DateHelper.asString(event.getStartDate()));
		createIfNotNull(parent, "duration", String.valueOf(event.getDuration()));
		createIfNotNull(parent, "category", event.getCategory());
		createIfNotNull(parent, "location", event.getLocation());
		createIfNotNull(parent, "alert", event.getAlert());
		createIfNotNull(parent, "priority", (event.getPriority() != null ? String.valueOf(event.getPriority()) : "0"));
		createIfNotNull(parent, "privacy", String.valueOf(event.getPrivacy().toInteger()));

		appendAttendeesToEventXml(parent, event);
		appendRecurrenceToEventXml(parent, event);
	}

	private void appendAttendeesToEventXml(Element parent, Event event) {
		Element attendees = DOMUtils.createElement(parent, "attendees");
		for (Attendee attendee : event.getAttendees()) {
			appendAttendee(attendees, attendee);
		}
	}

	private Element appendAttendee(Element attendees, Attendee attendee) {
		Element attendeeElement = DOMUtils.createElement(attendees, "attendee");
		attendeeElement.setAttribute("displayName", attendee.getDisplayName());
		attendeeElement.setAttribute("isOrganizer", String.valueOf(attendee.isOrganizer()));
		attendeeElement.setAttribute("email", attendee.getEmail());
		Participation participation = attendee.getParticipation();
		attendeeElement.setAttribute("state", (participation != null ? participation.toString() : Participation.needsAction().toString()));
		attendeeElement.setAttribute("required", attendee.getParticipationRole() != null ? attendee.getParticipationRole()
						.toString() : ParticipationRole.OPT.toString());

		attendeeElement.setAttribute("percent", String.valueOf(attendee.getPercent()));
		return attendeeElement;
	}

	private void appendRecurrenceToEventXml(Element parent, Event event) {
		Element recurrence = DOMUtils.createElement(parent, "recurrence");
		EventRecurrence eventRecurrence = event.getRecurrence();
		recurrence.setAttribute("kind", eventRecurrence.getKind().toString());
		if (event.isRecurrent()) {
			appendRecurrenceRuleToRecurrenceElement(recurrence, eventRecurrence);
			appendNegativeExceptionsToRecurrenceElement(recurrence, eventRecurrence);
			appendEventExceptionsToRecurrenceElement(recurrence, eventRecurrence);
		}
	}

	private void appendRecurrenceRuleToRecurrenceElement(Element recurrence,
			EventRecurrence eventRecurrence) {
		if (eventRecurrence.getEnd() != null) {
			recurrence.setAttribute("end", DateHelper.asString(eventRecurrence.getEnd()));
		}
		if (eventRecurrence.getKind() == RecurrenceKind.weekly
				|| eventRecurrence.getKind() == RecurrenceKind.daily) {
			recurrence.setAttribute("days", new RecurrenceDaysSerializer().serialize(eventRecurrence.getDays()));
			if (eventRecurrence.getFrequence() == 0) {
				eventRecurrence.setFrequence(1);
			}
		}
		recurrence.setAttribute("freq", String.valueOf(eventRecurrence.getFrequence()));
	}

	private void appendEventExceptionsToRecurrenceElement(Element recurrence,
			EventRecurrence eventRecurrence) {
		Element eventExceptions = DOMUtils.createElement(recurrence, "eventExceptions");
		for (Event ex : eventRecurrence.getEventExceptions()) {
			Element eventException = DOMUtils.createElement(eventExceptions, "eventException");
			appendEvent(eventException, ex);
		}
	}

	private void appendNegativeExceptionsToRecurrenceElement(
			Element recurrence, EventRecurrence eventRecurrence) {
		Element exceptions = DOMUtils.createElement(recurrence, "exceptions");
		for (Date exception : eventRecurrence.getExceptions()) {
			DOMUtils.createElementAndText(exceptions, "exception", DateHelper.asString(exception));
		}
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


	public void appendResourceInfo(Element root, ResourceInfo ri) {
		Element info = DOMUtils.createElement(root, "resourceInfo");
		appendResourceInfoAttributes(info, ri);
	}

	public void appendResourceInfoAttributes(Element node, ResourceInfo ri) {
		DOMUtils.createElementAndText(node, "id", ri.getId());
		DOMUtils.createElementAndText(node, "name", ri.getName());
		DOMUtils.createElementAndText(node, "mail", ri.getMail());
		DOMUtils.createElementAndText(node, "description", Strings.nullToEmpty(ri.getDescription()));
		DOMUtils.createElementAndText(node, "read", String.valueOf(ri.isRead()));
		DOMUtils.createElementAndText(node, "write", String.valueOf(ri.isWrite()));
		DOMUtils.createElementAndText(node, "domain", ri.getDomainName());
	}

	public String getEventString(Event event) throws TransformerException {
		return DOMUtils.serialize(getXMLDocumentFrom(event));
	}

	public String getListEventString(List<Event> events) throws TransformerException {
		return DOMUtils.serialize(getXMLDocumentFrom(events));
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
		createIfNotNull(e, "state", (eps.getParticipation() != null ? eps.getParticipation()
				.toString() : Participation.needsAction().toString()));
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
