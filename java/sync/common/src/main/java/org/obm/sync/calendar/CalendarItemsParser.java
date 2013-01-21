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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.base.Category;
import org.obm.sync.items.AbstractItemsParser;
import org.obm.sync.items.EventChanges;
import org.obm.sync.utils.DateHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;

public class CalendarItemsParser extends AbstractItemsParser {

	protected Logger logger = LoggerFactory.getLogger(getClass());

	private static final EventPrivacy DEFAULT_PRIVACY_VALUE = EventPrivacy.PUBLIC;
	private static final int DEFAULT_PRIORITY_VALUE = 0;
	private static final int DEFAULT_DURATION_VALUE = 0;

	public EventChanges parseChanges(Document doc) {
		EventChanges changes = new EventChanges();
		Element root = doc.getDocumentElement();
		Date lastSync = DateHelper.asDate(root.getAttribute("lastSync"));
		changes.setLastSync(lastSync);
		Element removed = DOMUtils.getUniqueElement(root, "removed");
		Element updated = DOMUtils.getUniqueElement(root, "updated");

		NodeList rmed = removed.getElementsByTagName("event");
		Set<DeletedEvent> removedIds = Sets.newHashSetWithExpectedSize(rmed.getLength() + 1);
		for (int i = 0; i < rmed.getLength(); i++) {
			Element e = (Element) rmed.item(i);
			removedIds.add(new DeletedEvent(
					new EventObmId(e.getAttribute("id")), 
					new EventExtId(e.getAttribute("extId"))));
		}
		changes.setDeletedEvents(removedIds);

		NodeList upd = updated.getElementsByTagName("event");
		List<Event> updatedEvents = new ArrayList<Event>(upd.getLength() + 1);
		for (int i = 0; i < upd.getLength(); i++) {
			Element e = (Element) upd.item(i);
			Event ev = parseEvent(e);
			updatedEvents.add(ev);
		}
		changes.setUpdated(updatedEvents);

		return changes;
	}

	public Event parseEvent(Element e) {
		Event ev = new Event();
		String id = e.getAttribute("id");
		if (!Strings.isNullOrEmpty(id)) {
			ev.setUid(new EventObmId(id));
		}
		ev.setInternalEvent(e.hasAttribute("isInternal") ? !"false".equals(e
				.getAttribute("isInternal")) : true);
		ev.setAllday(e.hasAttribute("allDay") ? "true".equals(e
				.getAttribute("allDay")) : false);
		ev.setType(EventType.valueOf(e.getAttribute("type")));
		// The sequence is not repeated for an event exception
		if (e.hasAttribute("sequence")) {
			ev.setSequence(Integer.valueOf(e.getAttribute("sequence")));
		}
		ev.setExtId(new EventExtId(s(e, "extId")));
		ev.setRecurrenceId(d(e, "recurrenceId"));

		String opacity = DOMUtils.getElementTextInChildren(e, "opacity");
		ev.setOpacity(EventOpacity.getValueOf(opacity));
		ev.setTitle(s(e, "title"));
		String owner = s(e, "owner");
		ev.setOwner(owner);
		ev.setOwnerEmail(s(e, "ownerEmail"));
		ev.setOwnerDisplayName(owner);
		String tz = s(e, "tz");
		if (tz == null || tz.trim().length() == 0) {
			tz = "Europe/Paris";
		}
		ev.setTimezoneName(tz);
		ev.setDescription(s(e, "description"));
		ev.setStartDate(d(e, "date"));
		ev.setPrivacy(getPrivacy(e));
		ev.setPriority(i(e, "priority", DEFAULT_PRIORITY_VALUE));
		ev.setDuration(i(e, "duration", DEFAULT_DURATION_VALUE));
		ev.setCategory(s(e, "category"));
		ev.setLocation(s(e, "location"));
		ev.setAlert(getAlert(e));
		ev.setTimeUpdate(d(e, "timeupdate"));
		ev.setTimeCreate(d(e, "timecreate"));

		parseAttendees(ev, e);

		Element rec = DOMUtils.getUniqueElementInChildren(e, "recurrence");
		if (rec != null) {
			parseRecurrence(ev, rec);
		}

		Element eventsExp = DOMUtils.getUniqueElement(e, "eventExceptions");
		if (eventsExp != null) {
			NodeList elems = eventsExp.getElementsByTagName("eventException");
			for (int i = 0; i < elems.getLength(); i++) {
				Event eexcept = parseEventException(ev, (Element) elems.item(i));
				ev.getRecurrence().addEventException(eexcept);
			}
		}
		return ev;
	}

	private EventPrivacy getPrivacy(Element e) {
		Integer privacy = i(e, "privacy");
		if (privacy != null) {
			return EventPrivacy.fromXmlIntCode(privacy);
		}
		return DEFAULT_PRIVACY_VALUE;
	}

	private Integer getAlert(Element e) {
		Integer alert = i(e, "alert");
		if (alert == null || alert < 0) {
			return null;
		}
		return alert;
	}

	private Event parseEventException(Event eventReference, Element item) {
		Event eexcept = parseEvent(item);
		eexcept.setSequence(eventReference.getSequence());
		eexcept.setExtId(eventReference.getExtId());
		return eexcept;
	}

	private void parseAttendees(Event ev, Element e) {
		Element ats = DOMUtils.getUniqueElementInChildren(e, "attendees");
		ev.setAttendees(getAttendees(ats));
	}

	private List<Attendee> getAttendees(Element ats) {
		String[][] atVals = DOMUtils.getAttributes(ats, "attendee",
				new String[] { "displayName", "email", "state", "required",
						"percent", "isOrganizer" });
		List<Attendee> la = new ArrayList<Attendee>(atVals.length);
		for (String[] attendee : atVals) {
			Attendee at = UnknownAttendee
					.builder()
					.displayName(attendee[0])
					.email(attendee[1])
					.participation(Participation.getValueOf(attendee[2]))
					.participationRole(ParticipationRole.valueOf(attendee[3]))
					.build();
			
			if (!Strings.isNullOrEmpty(attendee[4])) {
				at.setPercent(Integer.parseInt(attendee[4]));
			} else {
				at.setPercent(100);
			}
			
			at.setOrganizer(Boolean.parseBoolean(attendee[5]));
			
			la.add(at);
		}
		return la;

	}

	private void parseRecurrence(Event ev, Element rec) {
		String kind = rec.getAttribute("kind");
		EventRecurrence er = new EventRecurrence();
		er.setKind(RecurrenceKind.lookup(kind));
		if (er.getKind() != RecurrenceKind.none) {
			if (rec.hasAttribute("end")) {
				er.setEnd(DateHelper.asDate(rec.getAttribute("end")));
			}
			er.setFrequence(Integer.parseInt(rec.getAttribute("freq")));
			RecurrenceDays recurrenceDays = new RecurrenceDaysParser().parse(rec.getAttribute("days"));
			er.setDays(recurrenceDays);
			if (!er.getDays().isEmpty()) {
				er.setKind(RecurrenceKind.weekly);
				if (er.getFrequence() == 0) {
					er.setFrequence(1);
				}
			}
			String[] exDates = DOMUtils.getTexts(rec, "exception");
			for (int i = 0; i < exDates.length; i++) {
				er.addException(DateHelper.asDate(exDates[i]));

			}
		} else {
			er.setEnd(new Date());
		}
		ev.setRecurrence(er);

	}

	public CalendarInfo[] parseInfos(Document doc) {
		NodeList infosList = doc.getElementsByTagName("info");
		CalendarInfo[] infos = new CalendarInfo[infosList.getLength()];
		for (int i = 0; i < infosList.getLength(); i++) {
			infos[i] = parseInfo((Element) infosList.item(i));
		}
		return infos;
	}

	private CalendarInfo parseInfo(Element item) {
		CalendarInfo ci = new CalendarInfo();
		ci.setFirstname(s(item, "first"));
		ci.setLastname(s(item, "last"));
		ci.setMail(s(item, "mail"));
		ci.setUid(s(item, "uid"));
		ci.setRead("true".equals(s(item, "read")));
		ci.setWrite("true".equals(s(item, "write")));
		return ci;
	}

	public ResourceInfo[] parseResourceInfo(Document doc) {
		NodeList resourceInfoList = doc.getElementsByTagName("resourceInfo");
		ResourceInfo[] resourceInfo = new ResourceInfo[resourceInfoList.getLength()];
		for (int i = 0; i < resourceInfoList.getLength(); i++) {
			resourceInfo[i] = parseResourceInfo((Element) resourceInfoList.item(i));
		}
		return resourceInfo;
	}

	public ResourceInfo parseResourceInfo(Element item) {
		return ResourceInfo.builder().id(i(item, "id")).
				name(s(item, "name")).
				mail(s(item, "mail")).
				description(Strings.emptyToNull(s(item, "description"))).
				read("true".equals(s(item, "read"))).
				write("true".equals(s(item, "write"))).
				domainName(s(item, "domain")).
				build();
	}

	public List<Category> parseCategories(Element documentElement) {
		NodeList nl = documentElement.getElementsByTagName("cat");
		ArrayList<Category> ret = new ArrayList<Category>(nl.getLength());
		for (int i = 0; i < nl.getLength(); i++) {
			Element cat = (Element) nl.item(i);
			Category c = new Category();
			c.setId(Integer.parseInt(cat.getAttribute("id")));
			c.setLabel(cat.getAttribute("label"));
			ret.add(c);
		}
		return ret;
	}

	public List<Event> parseListEvents(Element documentElement) {
		List<Event> ret = new LinkedList<Event>();
		NodeList nl = documentElement.getElementsByTagName("event");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			ret.add(parseEvent(e));
		}
		return ret;
	}

	public List<EventTimeUpdate> parseListEventTimeUpdate(
			Element documentElement) {
		List<EventTimeUpdate> ret = new LinkedList<EventTimeUpdate>();
		NodeList nl = documentElement.getElementsByTagName("eventTimeUpdate");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			ret.add(parseEventTimeUpdate(e));
		}
		return ret;
	}

	private EventTimeUpdate parseEventTimeUpdate(Element e) {
		EventTimeUpdate ev = new EventTimeUpdate();
		ev.setUid(e.getAttribute("id"));
		ev.setExtId(s(e, "extId"));
		ev.setTimeUpdate(d(e, "timeupdate"));
		ev.setRecurrenceId(d(e, "recurrenceId"));
		return ev;
	}

	public List<EventParticipationState> parseListEventParticipationState(
			Element documentElement) {
		List<EventParticipationState> ret = new LinkedList<EventParticipationState>();
		NodeList nl = documentElement
				.getElementsByTagName("eventParticipationState");
		for (int i = 0; i < nl.getLength(); i++) {
			Element e = (Element) nl.item(i);
			ret.add(parseEventParticipationState(e));
		}
		return ret;
	}

	public EventParticipationState parseEventParticipationState(Element e) {
		EventParticipationState eps = new EventParticipationState();
		eps.setUid(e.getAttribute("id"));
		eps.setTitle(s(e, "title"));
		eps.setParticipation(Participation.getValueOf(s(e, "state")));
		eps.setAlert(getAlert(e));
		eps.setDate(d(e, "date"));
		return eps;
	}

	public FreeBusyRequest parseFreeBusyRequest(Element e) {
		FreeBusyRequest fb = new FreeBusyRequest();
		fb.setUid(s(e, "uid"));
		fb.setOwner(s(e, "owner"));
		fb.setStart(d(e, "start"));
		fb.setEnd(d(e, "end"));
		Element ats = DOMUtils.getUniqueElementInChildren(e, "attendees");
		List<Attendee> atts = getAttendees(ats);
		for (Attendee att : atts) {
			fb.addAttendee(att);
		}
		return fb;
	}

	public FreeBusy parseFreeBusy(Element e) {
		FreeBusy fb = new FreeBusy();

		fb.setUid(s(e, "uid"));
		fb.setOwner(s(e, "owner"));
		fb.setStart(d(e, "start"));
		fb.setEnd(d(e, "end"));

		List<Attendee> atts = getAttendees(e);
		if (atts.size() > 0) {
			fb.setAtt(atts.get(0));
		}

		NodeList nlines = e.getElementsByTagName("freebusyinterval");
		for (int j = 0; j < nlines.getLength(); j++) {
			FreeBusyInterval fbl = new FreeBusyInterval();
			Element lineE = (Element) nlines.item(j);
			fbl.setAllDay(b(lineE, "allDay"));
			fbl.setStart(d(lineE, "start"));
			fbl.setDuration(i(lineE, "duration", DEFAULT_DURATION_VALUE));
			fb.addFreeBusyInterval(fbl);
		}
		return fb;
	}

	public List<FreeBusy> parseListFreeBusy(Element e) {
		List<FreeBusy> ret = new LinkedList<FreeBusy>();
		NodeList freebusys = e.getChildNodes();
		for (int i = 0; i < freebusys.getLength(); i++) {
			Element fbe = (Element) freebusys.item(i);
			FreeBusy fb = parseFreeBusy(fbe);
			ret.add(fb);
		}
		return ret;
	}
}
