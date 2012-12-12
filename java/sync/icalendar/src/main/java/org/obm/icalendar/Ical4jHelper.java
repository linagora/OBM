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
package org.obm.icalendar;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.data.UnfoldingReader;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Dur;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyFactoryImpl;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.ValidationException;
import net.fortuna.ical4j.model.WeekDay;
import net.fortuna.ical4j.model.WeekDayList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VAlarm;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VFreeBusy;
import net.fortuna.ical4j.model.component.VToDo;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.CuType;
import net.fortuna.ical4j.model.parameter.FbType;
import net.fortuna.ical4j.model.parameter.PartStat;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.parameter.Rsvp;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Action;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Categories;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Comment;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.DateProperty;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Due;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.Method;
import net.fortuna.ical4j.model.property.Organizer;
import net.fortuna.ical4j.model.property.PercentComplete;
import net.fortuna.ical4j.model.property.Priority;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Repeat;
import net.fortuna.ical4j.model.property.Sequence;
import net.fortuna.ical4j.model.property.Status;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Transp;
import net.fortuna.ical4j.model.property.Trigger;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;

import org.apache.commons.lang.StringUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventOpacity;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.EventType;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyInterval;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.RecurrenceDay;
import org.obm.sync.calendar.RecurrenceDays;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.date.DateProvider;
import org.obm.sync.exception.IllegalRecurrenceKindException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.domain.ObmDomain;

@Singleton
public class Ical4jHelper {
	
	private static final int MAX_FOLD_LENGTH = 74; 
	private static final int SECONDS_IN_DAY = 43200000;
	private static final String X_OBM_DOMAIN = "X-OBM-DOMAIN";
	private static final String X_OBM_DOMAIN_UUID = "X-OBM-DOMAIN-UUID";
	private static final String XOBMORIGIN = "X-OBM-ORIGIN";

	private static Logger logger = LoggerFactory.getLogger(Ical4jHelper.class);
	private static final BiMap<RecurrenceDay, WeekDay> RECURRENCE_DAY_TO_WEEK_DAY = new ImmutableBiMap.Builder<RecurrenceDay, WeekDay>()
			.put(RecurrenceDay.Sunday, WeekDay.SU).put(RecurrenceDay.Monday, WeekDay.MO)
			.put(RecurrenceDay.Tuesday, WeekDay.TU).put(RecurrenceDay.Wednesday, WeekDay.WE)
			.put(RecurrenceDay.Thursday, WeekDay.TH).put(RecurrenceDay.Friday, WeekDay.FR)
			.put(RecurrenceDay.Saturday, WeekDay.SA).build();
	private static final BiMap<WeekDay, RecurrenceDay> WEEK_DAY_TO_RECURRENCE_DAY = RECURRENCE_DAY_TO_WEEK_DAY.inverse();
	private static final Map<RecurrenceKind, String> RECURRENCEKIND_TO_RECUR = new ImmutableMap.Builder<RecurrenceKind, String>()
			.put(RecurrenceKind.daily, Recur.DAILY).put(RecurrenceKind.weekly, Recur.WEEKLY)
			.put(RecurrenceKind.monthlybydate, Recur.MONTHLY).put(RecurrenceKind.monthlybyday, Recur.MONTHLY)
			.put(RecurrenceKind.yearly, Recur.YEARLY).put(RecurrenceKind.yearlybyday, Recur.YEARLY).build();
	
	private final DateProvider dateProvider;
	
	@Inject
	@VisibleForTesting
	public Ical4jHelper(DateProvider obmHelper) {
		this.dateProvider = obmHelper;
	}

	public String buildIcsInvitationRequest(Ical4jUser iCal4jUser, Event event, AccessToken token) {
		Calendar calendar = initCalendar();
		VEvent vEvent = buildIcsInvitationVEvent(iCal4jUser, event, token);
		calendar.getComponents().add(vEvent);
		if (event.isRecurrent()) {
			for (Event ee : event.getRecurrence().getEventExceptions()) {
				VEvent eventExt = buildIcsInvitationVEventException(ee);
				appendUidToICS(eventExt.getProperties(), ee, event.getExtId());
				calendar.getComponents().add(eventExt);
			}
		}
		calendar.getProperties().add(Method.REQUEST);
		return foldingWriterToString(calendar);
	}
	
	private String foldingWriterToString(final Calendar calendar) {
		Writer writer =  new StringWriter();
		CalendarOutputter calendarOutputter = new CalendarOutputter(true, MAX_FOLD_LENGTH);
		try {
			calendarOutputter.output(calendar, writer);
			return writer.toString(); 
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (ValidationException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public String buildIcsInvitationReply(final Event event, final Ical4jUser iCal4jUser, AccessToken token) {
		Method method = Method.REPLY;
		final Attendee replyAttendee = findAttendeeFromObmUserReply(event.getAttendees(), iCal4jUser);
		final Calendar calendar = buildVEvent(iCal4jUser, event, replyAttendee,method, token);		
		calendar.getProperties().add(method);
		return foldingWriterToString(calendar);
	}
	
	public String buildIcsInvitationCancel(Ical4jUser iCal4jUser, Event event, AccessToken token) {
		Method method = Method.CANCEL;
		Calendar calendar = buildVEvent(iCal4jUser, event, null, method, token);
		calendar.getProperties().add(method);
		return foldingWriterToString(calendar);
	}
	
	public String buildIcs(Ical4jUser iCal4jUser, Collection<Event> events, AccessToken token) {
		Calendar calendar = this.buildVEvents(iCal4jUser, events, null, null, token);
		return foldingWriterToString(calendar);
	}

	private VEvent buildIcsInvitationVEventDefaultValue(Event event) {
		VEvent vEvent = new VEvent();
		PropertyList prop = vEvent.getProperties();
		appendDtstamp(event, vEvent);
		appendCreated(prop, event);
		appendLastModified(prop, event);
		appendSequence(prop, event);
		appendAttendeesToICS(prop, event.getAttendees());
		appendCategoryToICS(prop, event);
		appendEventDates(prop, event);
		appendDescriptionToICS(prop, event);
		appendLocationToICS(prop, event);
		appendTranspToICS(prop, event);
		appendOrganizerToICS(prop, event);
		appendPriorityToICS(prop, event);
		appendPrivacyToICS(prop, event);
		appendSummaryToICS(prop, event);
		appendRRuleToICS(prop, event);
		appendExDateToICS(prop, event);
		appendVAlarmToICS(vEvent.getAlarms(), event);
		appendRecurenceIdToICS(prop, event);
		appendXMozLastAck(prop);
		return vEvent;
	}

	private VEvent buildIcsInvitationVEventException(Event event) {
		return buildIcsInvitationVEventDefaultValue(event);
	}
	
	private VEvent buildIcsInvitationVEvent(Ical4jUser iCal4jUser, Event event, AccessToken token) {
		VEvent vEvent = buildIcsInvitationVEventDefaultValue(event);
		PropertyList prop = vEvent.getProperties();
		appendUidToICS(prop, event, null);
		appendXObmDomainProperties(iCal4jUser, prop);
		appendXObmOrigin(prop, token);
		return vEvent;
	}
	
	public FreeBusyRequest parseICSFreeBusy(String ics) 
		throws IOException, ParserException {
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(new StringReader(ics));
		FreeBusyRequest freeBusy = new FreeBusyRequest();
		if (calendar != null) {
			ComponentList comps = getComponents(calendar, Component.VFREEBUSY);
			if (comps.size() > 0) {
				VFreeBusy vFreeBusy = (VFreeBusy) comps.get(0);
				freeBusy = getFreeBusy(vFreeBusy);
			}
		}

		return freeBusy;
	}

	public List<Event> parseICS(String ics, Ical4jUser ical4jUser) 
		throws IOException, ParserException {
		
		Calendar calendar = buildCalendar(ics);

		if (calendar != null) {
			return ImmutableList.copyOf(
					Iterables.concat(
							getEvents(calendar, ical4jUser),
							getTodos(ical4jUser, calendar)));
		}
		return ImmutableList.<Event>of();
	}

	private Calendar buildCalendar(String ics) throws IOException, ParserException {
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(new UnfoldingReader(new StringReader(ics), true));
		return calendar;
	}


	public List<Event> parseICSEvent(String ics, Ical4jUser ical4jUser) throws IOException, ParserException {
		Calendar calendar = buildCalendar(ics);
		if (calendar != null) {
			return ImmutableList.copyOf(getEvents(calendar, ical4jUser));
		}
		return ImmutableList.<Event>of();
	}
	
	private Collection<Event> getTodos(Ical4jUser ical4jUser, Calendar calendar) {
		List<Event> todos = Lists.newArrayList();
		ComponentList comps = getComponents(calendar, Component.VTODO);
		for (Object obj: comps) {
			VToDo vTodo = (VToDo) obj;
			Event event = convertVTodoToEvent(ical4jUser, vTodo);
			todos.add(event);
		}
		return todos;
	}

	private Collection<Event> getEvents(Calendar calendar, Ical4jUser ical4jUser) {
		Map<EventExtId, Event> mapEvents = Maps.newHashMap();
		Multimap<EventExtId, Event> mapExceptionEvents = HashMultimap.create();
		ComponentList comps = getComponents(calendar, Component.VEVENT);
		for (Object obj: comps) {
			VEvent vEvent = (VEvent) obj;
			Event event = convertVEventToEvent(ical4jUser, vEvent);
			if(event.getRecurrenceId() == null) {
				mapEvents.put(event.getExtId(), event);
			} else {
				mapExceptionEvents.put(event.getExtId(), event);
			}
		}
		return addEventExceptionToDefinedParentEvent(mapEvents, mapExceptionEvents);
	}

	private Collection<Event> addEventExceptionToDefinedParentEvent(
			Map<EventExtId, Event> mapEvents,
			Multimap<EventExtId, Event> mapExceptionEvents) {

		Collection<Entry<EventExtId, Collection<Event>>> mapExceptionEventsEntries = mapExceptionEvents.asMap().entrySet();

		for (Entry<EventExtId, Collection<Event>> entry : mapExceptionEventsEntries) {
			Event parentEvent = mapEvents.get(entry.getKey());
			Collection<Event> eventsException = entry.getValue();
			if (parentEvent != null) {
				addOrReplaceExceptions(parentEvent.getRecurrence(), eventsException);
			} else {
				logger.warn(
						"Drop following events exception while parsing ICS file because parent was not defined: {}",
						eventsException);
			}
		}
		return mapEvents.values();
	}

	private void addOrReplaceExceptions(EventRecurrence recurrenceTarget, Collection<Event> eventsToAdd) {
		for (Event eventToAdd : eventsToAdd) {
			recurrenceTarget.getExceptions().remove(eventToAdd.getRecurrenceId());
		}
		recurrenceTarget.getEventExceptions().addAll(eventsToAdd);
	}
	
	/* package */ Event convertVEventToEvent(Ical4jUser ical4jUser, VEvent vEvent) {
		Event event = new Event();
		event.setType(EventType.VEVENT);
		appendSummary(event, vEvent.getSummary());
		appendDescription(event, vEvent.getDescription());
		appendUid(event, vEvent.getUid());
		appendPrivacy(event, vEvent.getClassification());
		appendOwner(event, vEvent.getOrganizer());
		appendCategory(event, vEvent.getProperty(Property.CATEGORIES));
		appendLocation(event, vEvent.getLocation());
		appendSequence(event, vEvent.getSequence());
		appendDate(event, vEvent.getStartDate());
		appendDuration(event, vEvent.getStartDate(), vEvent.getEndDate());
		appendAllDay(event, vEvent.getDuration());
		appendPriority(event, vEvent.getPriority());
		appendRecurrenceId(event, vEvent.getRecurrenceId());
		appendAttendees(event, vEvent);
		appendRecurence(event, vEvent);
		appendAlert(event, vEvent.getAlarms());
		appendOpacity(event, vEvent.getTransparency(), event.isAllday());
		appendIsInternal(ical4jUser, event, vEvent.getProperty(X_OBM_DOMAIN_UUID));
		
		appendCreated(event, vEvent.getCreated());
		appendLastModified(event, vEvent.getLastModified());
		return event;
	}

	/* package */ Event convertVTodoToEvent(Ical4jUser ical4jUser, VToDo vTodo) {
		Event event = new Event();
		event.setType(EventType.VTODO);
		appendSummary(event, vTodo.getSummary());
		appendDescription(event, vTodo.getDescription());
		appendUid(event, vTodo.getUid());
		appendPrivacy(event, vTodo.getClassification());
		appendOwner(event, vTodo.getOrganizer());
		appendCategory(event, vTodo.getProperty(Property.CATEGORIES));
		appendLocation(event, vTodo.getLocation());
		appendDate(event, vTodo.getStartDate());
		appendDuration(event, vTodo.getStartDate(), vTodo.getDue());
		appendAllDay(event, vTodo.getStartDate(), vTodo.getDue());
		appendPriority(event, vTodo.getPriority());
		appendRecurrenceId(event, vTodo.getRecurrenceId());

		appendAttendees(event, vTodo);
		appendRecurence(event, vTodo);
		appendAlert(event, vTodo.getAlarms());
		appendPercent(event, vTodo.getPercentComplete(), ical4jUser.getEmail());
		appendStatus(event, vTodo.getStatus(), ical4jUser.getEmail());
		appendOpacity(event,
				(Transp) vTodo.getProperties().getProperty(Property.TRANSP),
				event.isAllday());
		appendIsInternal(ical4jUser, event, vTodo.getProperty(X_OBM_DOMAIN_UUID));
		
		appendCreated(event, vTodo.getCreated());
		appendLastModified(event, vTodo.getLastModified());
		return event;
	}
	
	private void appendLastModified(Event event,
			LastModified lastModified) {
		if (lastModified != null) {
			event.setTimeUpdate(lastModified.getDate());
		}
	}

	private void appendCreated(Event event, Created created) {
		if (created != null) {
			event.setTimeCreate(created.getDate());
		}
	}
	
	private void appendIsInternal(Ical4jUser ical4jUser, Event event, Property obmDomain) {
		boolean eventIsInternal = false;
		if(obmDomain != null){
			eventIsInternal = ical4jUser.getObmDomain().getUuid().equals(obmDomain.getValue());
		}
		event.setInternalEvent(eventIsInternal);
		
	}

	private void appendOpacity(Event event, Transp transp,
			boolean isAllDay) {
		if (Transp.TRANSPARENT.equals(transp)) {
			event.setOpacity(EventOpacity.TRANSPARENT);
		} else if (Transp.OPAQUE.equals(transp)) {
			event.setOpacity(EventOpacity.OPAQUE);
		} else if (isAllDay) {
			event.setOpacity(EventOpacity.TRANSPARENT);
		} else {
			event.setOpacity(EventOpacity.OPAQUE);
		}
	}

	private void appendRecurrenceId(Event event,
			RecurrenceId recurrenceId) {
		if (recurrenceId != null) {
			event.setRecurrenceId(recurrenceId.getDate());
		}
	}

	private void appendStatus(Event event, Status status, String email) {
		if (status != null) {
			for (Attendee att : event.getAttendees()) {
				if (att.getEmail().equals(email)) {

					if (Status.VTODO_NEEDS_ACTION.equals(status)) {
						att.setParticipation(Participation.needsAction());
					} else if (Status.VTODO_IN_PROCESS.equals(status)) {
						att.setParticipation(Participation.inProgress());
					} else if (Status.VTODO_COMPLETED.equals(status)) {
						att.setParticipation(Participation.completed());
					} else if (Status.VTODO_CANCELLED.equals(status)) {
						att.setParticipation(Participation.declined());
					} else {
						att.setParticipation(null);
					}
				}
			}
		}
	}

	private void appendPercent(Event event,
			PercentComplete percentComplete, String email) {
		if (percentComplete != null) {
			for (Attendee att : event.getAttendees()) {
				if (att.getEmail().equals(email)) {
					att.setPercent(percentComplete.getPercentage());
				}
			}
		}
	}

	private void appendPriority(Event event, Priority priority) {
		int value = 2;
		if (priority != null) {
			if (priority.getLevel() >= 6) {
				value = 1;
			} else if (priority.getLevel() >= 3 && priority.getLevel() < 6) {
				value = 2;
			} else if (priority.getLevel() > 0 && priority.getLevel() < 3) {
				value = 3;
			}
			event.setPriority(value);
		}
	}

	@VisibleForTesting void appendAllDay(Event event, DtStart startDate, DateProperty endDate) {
		if (endDate != null && startDate != null && startDate.getDate() != null && endDate.getDate() != null)  {
			if (startDate.getDate() instanceof DateTime	|| endDate.getDate() instanceof DateTime) {
				event.setAllday(false);
				return;
			}
		}		
		event.setAllday(true);
	}

	@VisibleForTesting void appendAllDay(Event event, Duration duration) {
		event.setAllday(isAllDayDuration(duration));
	}

	private boolean isAllDayDuration(Duration duration) {
		if (duration == null) {
			return false;
		}
		
		Dur dur = duration.getDuration();
		boolean isAllDay = dur.getDays() > 0
				&& dur.getWeeks() == 0
				&& dur.getHours() == 0
				&& dur.getMinutes() == 0
				&& dur.getSeconds() == 0;
		return isAllDay;
	}

	private void appendDuration(Event event, DtStart startDate,
			DateProperty due) {
		int duration = getDuration(startDate, due);
		event.setDuration(duration);
	}

	private void appendDate(Event event, DtStart startDate) {
		if (startDate != null) {
			event.setStartDate(startDate.getDate());
			event.setTimezoneName("Etc/GMT");
		}

	}

	private void appendLocation(Event event, Location location) {
		if (location != null) {
			event.setLocation(location.getValue());
		}
	}

	private void appendSequence(Event event, Sequence sequence) {
		if (sequence != null) {
			event.setSequence(sequence.getSequenceNo());
		}
	}
	
	private void appendCategory(Event event, Property category) {
		if (category != null) {
			event.setCategory(category.getValue());
		}
	}

	private void appendOwner(Event event, Organizer organizer) {
		if (organizer != null) {
			Parameter cn = organizer.getParameter(Parameter.CN);
			String cnOrganizer = "";
			if (cn != null) {
				cnOrganizer = cn.getValue();
			}

			if (cnOrganizer != null && !"".equals(cnOrganizer)) {
				event.setOwner(cnOrganizer);
			}

			int mailToIndex = organizer.getValue().toLowerCase()
					.indexOf("mailto:");
			if (mailToIndex != -1) {
				event.setOwnerEmail(organizer.getValue().substring(
						mailToIndex + "mailto:".length()));
				
				if (StringUtils.isEmpty(event.getOwner())) {
					event.setOwner(organizer.getValue().substring(
							mailToIndex + "mailto:".length()));
				}
			}
		}
	}

	private void appendPrivacy(Event event, Clazz classification) {
		if (classification != null
				&& (Clazz.PRIVATE.equals(classification) || Clazz.CONFIDENTIAL
						.equals(classification))) {
			event.setPrivacy(EventPrivacy.PRIVATE);
		} else {
			event.setPrivacy(EventPrivacy.PUBLIC);
		}

	}

	private void appendUid(Event event, Uid uid) {
		String extId = uid != null && !Strings.isNullOrEmpty(uid.getValue()) ? uid.getValue() : EventExtId.generateUid().toString();
		
		event.setExtId(new EventExtId(extId));
	}

	private void appendDescription(Event event, Description description) {
		if (description != null) {
			event.setDescription(description.getValue());
		}
	}

	private void appendSummary(Event event, Summary summary) {
		if (summary != null) {
			event.setTitle(summary.getValue());
		}

	}

	public String parseEvents(Ical4jUser iCal4jUser, Collection<Event> listEvent, AccessToken token) {

		Calendar calendar = initCalendar();

		for (Event event : listEvent) {
			VEvent vEvent = getVEvent(iCal4jUser, event, null, null, token);
			calendar.getComponents().add(vEvent);
		}
		return calendar.toString();
	}

	public String parseEvent(Event event, Ical4jUser iCal4jUser, AccessToken token) {
		if (EventType.VEVENT.equals(event.getType())) {
			Calendar c = buildVEvent(iCal4jUser, event, null, null, token);
			return c.toString();
		} else if (EventType.VTODO.equals(event.getType())) {
			Calendar c = buildVTodo(event, iCal4jUser);
			return c.toString();
		}
		return null;
	}

	private Calendar buildVTodo(Event event, Ical4jUser iCal4jUser) {
		Calendar calendar = initCalendar();
		VToDo vTodo = getVToDo(event, iCal4jUser);
		calendar.getComponents().add(vTodo);
		if (event.isRecurrent()) {
			for (Event ee : event.getRecurrence().getEventExceptions()) {
				VToDo todoExt = getVTodo(ee, event.getExtId(), iCal4jUser, event);
				calendar.getComponents().add(todoExt);
			}
		}
		return calendar;
	}

	private Calendar buildVEvent(Ical4jUser iCal4jUser, Event event, Attendee replyAttendee, Method method, AccessToken token) {
		return buildVEvents(iCal4jUser, Arrays.asList(event), replyAttendee, method, token);
	}

	private Calendar buildVEvents(Ical4jUser iCal4jUser, Collection<Event> events, Attendee replyAttendee, Method method, AccessToken token) {
		Calendar calendar = initCalendar();
		for (Event event : events) {
			VEvent vEvent = getVEvent(iCal4jUser, event, replyAttendee, method, token);
			calendar.getComponents().add(vEvent);
			if (event.isRecurrent()) {
				for (Event ee : event.getRecurrence().getEventExceptions()) {
					VEvent eventExt = getVEvent(null, ee, event.getExtId(), event, replyAttendee,
							method, token);
					calendar.getComponents().add(eventExt);
				}
			}
		}
		return calendar;
	}

	
	private Attendee findAttendeeFromObmUserReply(final List<Attendee> attendees, final Ical4jUser iCal4jUser) {
		for (final Attendee attendee: attendees) {
			if (attendee.getEmail().equalsIgnoreCase(iCal4jUser.getEmail())) {
				return attendee;
			}
		}
		return null;
	}

	private VEvent getVEvent(Ical4jUser iCal4jUser, Event event, Attendee replyAttendee, Method method, AccessToken token) {
		return getVEvent(iCal4jUser, event, null, null, replyAttendee, method, token);
	}

	private VEvent getVEvent(Ical4jUser iCal4jUser, Event event, EventExtId parentExtID, Event parent, Attendee replyAttendee, Method method, AccessToken token) {
		VEvent vEvent = new VEvent();
		PropertyList prop = vEvent.getProperties();

		if (Method.REPLY.equals(method)) {
			appendDtstamp(dateProvider.getDate(), vEvent);
		} else {
			appendDtstamp(event, vEvent);
		}
		appendUidToICS(prop, event, parentExtID);
		appendCreated(prop, event);
		appendLastModified(prop, event);
		appendSequence(prop, event);
		if (replyAttendee == null) {
			appendAttendeesToICS(prop, event.getAttendees());
		} else {
			appendAttendeesToICS(prop, ImmutableList.of(replyAttendee));
			appendReplyCommentToICS(prop, replyAttendee);
		}
		appendCategoryToICS(prop, event);
		appendEventDates(prop, event);
		appendDescriptionToICS(prop, event);
		appendLocationToICS(prop, event);
		appendTranspToICS(prop, event);
		if (parent != null) {
			appendOrganizerToICS(prop, parent);
		} else {
			appendOrganizerToICS(prop, event);
		}
		appendPriorityToICS(prop, event);
		appendPrivacyToICS(prop, event);
		appendSummaryToICS(prop, event);
		appendRRuleToICS(prop, event);
		appendExDateToICS(prop, event);
		if(canAddVAlarmToICS(method)){
			appendVAlarmToICS(vEvent.getAlarms(), event);
		}
		appendRecurenceIdToICS(prop, event);
		appendXMozLastAck(prop);
		if (token != null) {
			appendXObmOrigin(prop, token);
		}
		if(iCal4jUser != null){
			appendXObmDomainProperties(iCal4jUser, prop);
		}
		return vEvent;
	}

	private void appendDtstamp(Event event, VEvent vEvent) {
		Date eventTimeUpdate = event.getTimeUpdate();
		Date eventTimeCreate = event.getTimeCreate();
		if (eventTimeUpdate == null && eventTimeCreate == null) {
			return;
		}
		
		appendDtstamp(Objects.firstNonNull(eventTimeUpdate, eventTimeCreate), vEvent);
	}
	
	private void appendDtstamp(Date time, VEvent vEvent) {
		vEvent.getDateStamp().setDateTime(new DateTime(time));
	}

	private boolean canAddVAlarmToICS(Method method) {
		if(method == null || Method.ADD.equals(method) || Method.COUNTER.equals(method) || Method.PUBLISH.equals(method) || Method.REQUEST.equals(method)){
			return true;
		} else {
			return false;
		}
	}

	private VToDo getVToDo(Event event, Ical4jUser iCal4jUser) {
		return getVTodo(event, null, iCal4jUser, null);
	}

	private VToDo getVTodo(Event event, EventExtId parentExtID, Ical4jUser iCal4jUser, Event pere) {
		VToDo vTodo = new VToDo();
		PropertyList prop = vTodo.getProperties();

		appendUidToICS(prop, event, parentExtID);
		appendCreated(prop, event);
		appendLastModified(prop, event);
		appendAttendeesToICS(prop, event.getAttendees());
		appendCategoryToICS(prop, event);
		appendDtStartToICS(prop, event);
		appendDuedToICS(prop, event);
		appendDescriptionToICS(prop, event);
		appendLocationToICS(prop, event);
		appendTranspToICS(prop, event);
		if (pere != null) {
			appendOrganizerToICS(prop, pere);
		} else {
			appendOrganizerToICS(prop, event);
		}
		appendPriorityToICS(prop, event);
		appendPrivacyToICS(prop, event);
		appendSummaryToICS(prop, event);
		appendRRuleToICS(prop, event);
		appendExDateToICS(prop, event);
		appendVAlarmToICS(vTodo.getAlarms(), event);
		appendRecurenceIdToICS(prop, event);
		appendPercentCompleteToICS(prop, event, iCal4jUser);
		appendStatusToICS(prop, event, iCal4jUser);
		appendXMozLastAck(prop);

		return vTodo;
	}

	private void appendXMozLastAck(PropertyList prop) {
		java.util.Calendar cal = java.util.Calendar.getInstance(TimeZone
				.getTimeZone("GMT"));
		cal.setTimeInMillis(System.currentTimeMillis());
		DateProperty p = new DateProperty("X-MO-LASTACK",
				PropertyFactoryImpl.getInstance()) {
					private static final long serialVersionUID = -2237202839701797737L;
		};
		p.setDate(new DateTime(cal.getTime()));
		prop.add(p);
	}
	
	private void appendXObmDomainProperties(Ical4jUser iCal4jUser, PropertyList prop) {
		ObmDomain obmDomain = iCal4jUser.getObmDomain();
		XProperty domainProp = new XProperty(X_OBM_DOMAIN, obmDomain.getName());
		XProperty uuidDomainProp = new XProperty(X_OBM_DOMAIN_UUID, obmDomain.getUuid());
		
		prop.add(domainProp);
		prop.add(uuidDomainProp);
	}

	private void appendXObmOrigin(PropertyList prop, AccessToken token) {
		XProperty p = new XProperty(XOBMORIGIN, token.getOrigin());
		prop.add(p);
	}

	private void appendStatusToICS(PropertyList prop, Event event, Ical4jUser iCal4jUser) {
		for (Attendee att : event.getAttendees()) {
			if (att.getEmail().equals(iCal4jUser.getEmail())) {
				if (Participation.needsAction().equals(att.getParticipation())) {
					prop.add(Status.VTODO_NEEDS_ACTION);
				} else if (Participation.inProgress().equals(att.getParticipation())) {
					prop.add(Status.VTODO_IN_PROCESS);
				} else if (Participation.completed().equals(att.getParticipation())) {
					prop.add(Status.VTODO_COMPLETED);
				} else if (Participation.declined().equals(att.getParticipation())) {
					prop.add(Status.VTODO_CANCELLED);
				} else {
					prop.add(new Status(""));
				}

			}
		}
	}

	private void appendPercentCompleteToICS(PropertyList prop, Event event, Ical4jUser iCal4jUser) {
		for (Attendee att : event.getAttendees()) {
			if (att.getEmail().equals(iCal4jUser.getEmail())) {
				prop.add(new PercentComplete(att.getPercent()));
			}
		}

	}

	private void appendDuedToICS(PropertyList prop, Event event) {
		if (event.getDuration() != 0) {
			Due dtEnd = getDue(event.getStartDate(), event.getDuration());
			if (dtEnd != null) {
				prop.add(dtEnd);
			}
		}
	}

	private void appendRecurenceIdToICS(PropertyList prop, Event event) {
		if (event.getRecurrenceId() != null) {
			prop.add(getRecurrenceId(event));
		}
	}

	private void appendVAlarmToICS(ComponentList prop, Event event) {
		VAlarm vAlarm = getVAlarm(event.getAlert());
		if (vAlarm != null) {
			prop.add(vAlarm);
		}
	}

	private void appendExDateToICS(PropertyList prop, Event event) {
		ExDate exDate = getExDate(event);
		if (exDate != null) {
			prop.add(exDate);
		}
	}

	private void appendRRuleToICS(PropertyList prop, Event event) {
		if (event.getRecurrenceId() == null) {
			RRule rrule = getRRule(event);
			if (rrule != null) {
				prop.add(rrule);
			}
		}
	}

	private void appendSummaryToICS(PropertyList prop, Event event) {
		prop.add(new Summary(event.getTitle()));
	}

	private void appendReplyCommentToICS(PropertyList prop, Attendee attendee) {
		Participation status = attendee.getParticipation();

		if (status.hasDefinedComment()) {
			org.obm.sync.calendar.Comment comment = status.getComment();
			prop.add(new Comment(comment.serializeToString()));
		}
	}

	private void appendPrivacyToICS(PropertyList prop, Event event) {
		prop.add(getClazz(event.getPrivacy()));
	}

	private void appendPriorityToICS(PropertyList prop, Event event) {
		int priority = 5;
		if (event.getPriority() != null) {
			if (event.getPriority() == 1) {
				priority = 9;
			} else if (event.getPriority() == 2) {
				priority = 5;
			} else if (event.getPriority() == 3) {
				priority = 1;
			}
		}
		prop.add(new Priority(priority));
	}

	private void appendOrganizerToICS(PropertyList prop, Event event) {
		final Attendee organizer = event.findOrganizer();
		if (organizer != null) {
			prop.add(getOrganizer(organizer.getDisplayName(), organizer.getEmail()));	
		} else {
			prop.add(getOrganizer(event.getOwnerDisplayName(), event.getOwnerEmail()));
		}
	}

	private void appendTranspToICS(PropertyList prop, Event event) {
		prop.add(getTransp(event.getOpacity()));
	}

	private void appendLocationToICS(PropertyList prop, Event event) {
		if (!isEmpty(event.getLocation())) {
			prop.add(new Location(event.getLocation()));
		}
	}

	private void appendDescriptionToICS(PropertyList prop, Event event) {
		if (!isEmpty(event.getDescription())) {
			prop.add(new Description(event.getDescription()));
		}

	}
	
	private void appendEventDates(PropertyList prop, Event event) {
		if (event.isAllday()) {
			appendStartDateToICS(prop, event);
			appendEndDateToICS(prop, event);
		} else {
			appendDtStartToICS(prop, event);
			appendDurationToICS(prop, event);
		}
	}

	private void appendLastModified(PropertyList prop, Event event) {
		if(event.getTimeUpdate() != null){
			prop.add(new LastModified(new DateTime(event.getTimeUpdate().getTime())));
		}
	}

	private void appendSequence(PropertyList prop, Event event) {
		prop.add(new Sequence(event.getSequence()));
	}
	
	private void appendCreated(PropertyList prop, Event event) {
		if(event.getTimeCreate() != null){
			prop.add(new Created(new DateTime(event.getTimeCreate().getTime())));
		}
	}

	private void appendDtStartToICS(PropertyList prop, Event event) {
			prop.add(getDtStart(event.getStartDate()));
	}
	
	private void appendStartDateToICS(PropertyList prop, Event event) {
		prop.add(getDateStart(event.getStartDate()));
	}
	
	private void appendDurationToICS(PropertyList prop, Event event) {
		prop.add(new Duration(new Dur(event.getStartDate(), event.getEndDate())));
	}
	
	private void appendEndDateToICS(PropertyList prop, Event event) {
		prop.add(getDtEnd(event.getEndDate()));
	}

	private void appendCategoryToICS(PropertyList prop, Event event) {
		if (!isEmpty(event.getCategory())) {
			prop.add(new Categories(event.getCategory()));
		}
	}

	private void appendAttendeesToICS(PropertyList prop, List<Attendee> attendees) {
		for (final Attendee attendee: attendees) {
			prop.add(getAttendee(attendee));
		}
	}

	private net.fortuna.ical4j.model.property.Attendee getAttendee(
			Attendee attendee) {
		net.fortuna.ical4j.model.property.Attendee att = new net.fortuna.ical4j.model.property.Attendee();

		att.getParameters().add(CuType.INDIVIDUAL);

		PartStat ps = getPartStat(attendee);
		att.getParameters().add(ps);

		att.getParameters().add(Rsvp.TRUE);

		Cn cn = getCn(attendee);
		att.getParameters().add(cn);

		Role role = getRole(attendee);
		att.getParameters().add(role);

		try {
			att.setValue("mailto:" + attendee.getEmail());
		} catch (URISyntaxException e) {
			logger.error(e.getMessage(), e);
		}
		return att;
	}

	private void appendUidToICS(PropertyList prop, Event event,
			EventExtId parentExtId) {
		if (parentExtId != null && parentExtId.getExtId() != null) {
			prop.add(new Uid(parentExtId.serializeToString()));
		} else if (event.getExtId() != null && event.getExtId().getExtId() != null) {
			prop.add(new Uid(event.getExtId().serializeToString()));
		} else {
			throw new InvalidParameterException();
		}
	}

	public Date isInIntervalDate(Event event, Date start, Date end,
			Set<Date> dateExce) {
		return isInIntervalDate(event.getRecurrence(), event.getStartDate(), start,
				end, dateExce);
	}

	public Date isInIntervalDate(EventRecurrence recurrence,
			Date eventDate, Date start, Date end, Set<Date> dateExce) {
		List<Date> dates = dateInInterval(recurrence, eventDate, start, end, dateExce);
		for (Date date : dates) {
			if ((date.after(start) || date.equals(start))
					&& (end == null || ((date.before(end) || date.equals(end))))) {
				return date;
			}
		}
		return null;

	}

	public List<Date> dateInInterval(EventRecurrence recurrence,
			Date eventDate, Date start, Date end, Set<Date> dateExce) {
		List<Date> ret = new LinkedList<Date>();
		Recur recur = getRecur(recurrence, eventDate);
		if (recur == null) {
			ret.add(eventDate);
			return ret;
		}
		if (end == null) {
			if (start.before(eventDate)) {
				ret.add(eventDate);
				return ret;
			}
			return ImmutableList.of();
		}
		DateList dl = recur.getDates(new DateTime(eventDate), new DateTime(
				start), new DateTime(end), Value.DATE_TIME);
		for (Iterator<?> it = dl.iterator(); it.hasNext();) {
			Date evD = (Date) it.next();
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(evD);
			cal.set(GregorianCalendar.MILLISECOND, 0);
			if (!dateExce.contains(cal.getTime())) {
				ret.add(evD);
			}
		}
		return ret;
	}

	@VisibleForTesting Recur getRecur(EventRecurrence eventRecurrence, Date eventStartDate) {
		Recur recur = null;
		if (eventRecurrence.isRecurrent()) {
			RecurrenceKind recurrenceKind = eventRecurrence.getKind();

			if (!RECURRENCEKIND_TO_RECUR.containsKey(recurrenceKind)) {
				throw new IllegalRecurrenceKindException(recurrenceKind);
			}

			String recurFrequency = RECURRENCEKIND_TO_RECUR.get(recurrenceKind);

			recur = getRecurFrom(eventRecurrence, recurFrequency);
			if (RecurrenceKind.monthlybyday.equals(recurrenceKind)) {
				addMonthlyOffsetToRecurDayList(eventStartDate, recur);
			}
			recur.setInterval(eventRecurrence.getFrequence());
			setRecurDayList(eventRecurrence, recur);
		}
		return recur;
	}

	private void addMonthlyOffsetToRecurDayList(Date eventStartDate, Recur recur) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(eventStartDate);
		recur.getDayList().add(WeekDay.getMonthlyOffset(cal));
	}

	private void setRecurDayList(EventRecurrence eventRecurrence, Recur recur) {
		Set<WeekDay> listDay = getListDay(eventRecurrence);
		for (WeekDay weekDay : listDay) {
			recur.getDayList().add(weekDay);
		}
	}

	private Recur getRecurFrom(EventRecurrence eventRecurrence, String frequency) {
		if (eventRecurrence.getEnd() == null) {
			return new Recur(frequency, null);
		} else {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(eventRecurrence.getEnd());
			cal.set(GregorianCalendar.SECOND, 0);
			return new Recur(frequency, new DateTime(cal.getTime()));
		}
	}

	@VisibleForTesting Set<WeekDay> getListDay(EventRecurrence eventRecurrence) {
		Set<WeekDay> listDay = new HashSet<WeekDay>();
		RecurrenceDays recurrenceDays = eventRecurrence.getDays();
		for (RecurrenceDay recurrenceDay : recurrenceDays) {
			WeekDay weekDay = RECURRENCE_DAY_TO_WEEK_DAY.get(recurrenceDay);
			if (weekDay == null) {
				throw new IllegalArgumentException("Unknown recurrence day " + recurrenceDay);
			}
			listDay.add(weekDay);
		}
		return listDay;
	}

	private int getDuration(DtStart startDate, DateProperty endDate) {
		if (startDate != null && endDate != null) {
			long start = startDate.getDate().getTime();
			long end = endDate.getDate().getTime();
			return (int) ((end - start) / 1000);
		}
		return 0;
	}

	private void appendAlert(Event event, ComponentList cl) {
		if (cl.size() > 0) {
			
			final VAlarm valarm = (VAlarm) cl.get(0);
			if (valarm != null) {

				if (	(isVAlarmRepeat(valarm) && valarm.getDuration()!=null)
					||	(!isVAlarmRepeat(valarm) && valarm.getDuration()==null)
				) {
					final Trigger trigger = valarm.getTrigger();
					
					Dur dur = trigger.getDuration();
					Dur durZero = new Dur(0, 0, 0, 0);
					
					if (dur==null || dur.equals(durZero)) {
						event.setAlert(null);
						return;
					} else if (dur.isNegative()) {
						dur = dur.negate();
					}
					
					int day = (dur.getWeeks() * 7) + dur.getDays();
					int hours = (day * 24) + dur.getHours();
					int min = (hours * 60) + dur.getMinutes();
					int sec = min * 60 + dur.getSeconds();
					
					event.setAlert(sec);	
					return;
				}
			}
		} else {
			event.setAlert(null);
		}
	}

	private boolean isVAlarmRepeat(final VAlarm valarm) {
		final Repeat repeat = valarm.getRepeat();
		if (repeat != null) {
			return true;
		}
		return false;
	}
	
	private void appendRecurence(Event event, CalendarComponent component) {
		RRule rrule = (RRule) component.getProperties().getProperty(
				Property.RRULE);

		EventRecurrence er = new EventRecurrence();
		EnumSet<RecurrenceDay> recurrenceDays = EnumSet.noneOf(RecurrenceDay.class);
		if (rrule != null) {
			Recur recur = rrule.getRecur();
			boolean setDays = false;
			for (Object ob : recur.getDayList()) {
				WeekDay weekDay = (WeekDay) ob;
				RecurrenceDay recurrenceDay = WEEK_DAY_TO_RECURRENCE_DAY.get(weekDay);
				if (recurrenceDay == null) {
					throw new IllegalArgumentException("Unknown week day " + weekDay);
				}
				recurrenceDays.add(recurrenceDay);
				setDays = true;
			}
			if (Recur.WEEKLY.equals(rrule.getRecur().getFrequency())
					&& !setDays) {
				DtStart start = (DtStart) component.getProperties()
						.getProperty(Property.DTSTART);
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTime(start.getDate());
				switch (cal.get(GregorianCalendar.DAY_OF_WEEK)) {
				case 1:
					recurrenceDays.add(RecurrenceDay.Sunday);
					break;
				case 2:
					recurrenceDays.add(RecurrenceDay.Monday);
					break;
				case 3:
					recurrenceDays.add(RecurrenceDay.Tuesday);
					break;
				case 4:
					recurrenceDays.add(RecurrenceDay.Wednesday);
					break;
				case 5:
					recurrenceDays.add(RecurrenceDay.Thursday);
					break;
				case 6:
					recurrenceDays.add(RecurrenceDay.Friday);
					break;
				case 7:
					recurrenceDays.add(RecurrenceDay.Saturday);
					break;
				}

			}
			er.setDays(new RecurrenceDays(recurrenceDays));
			er.setEnd(recur.getUntil());
			PropertyList exdates = component.getProperties(Property.EXDATE);
			Set<Date> dates = new HashSet<Date>();
			for (Object ob : exdates) {
				ExDate exdate = (ExDate) ob;
				DateList dl = exdate.getDates();
				for (Object date : dl) {
					dates.add((Date) date);
				}
			}

			for (Date d : dates) {
				er.addException(d);
			}

			er.setFrequence(recur.getInterval());
			if (er.getDays().isEmpty()) {
				if (Recur.DAILY.equals(recur.getFrequency())) {
					er.setKind(RecurrenceKind.daily);
				} else if (Recur.WEEKLY.equals(recur.getFrequency())) {
					er.setKind(RecurrenceKind.weekly);
				} else if (Recur.MONTHLY.equals(recur.getFrequency())) {
					WeekDayList wdl = recur.getDayList();

					if (wdl.size() > 0) {
						er.setKind(RecurrenceKind.monthlybyday);
						GregorianCalendar cal = new GregorianCalendar();
						cal.setTime(event.getStartDate());
						cal.set(GregorianCalendar.WEEK_OF_MONTH,
								((WeekDay) wdl.get(0)).getOffset());
						cal.set(GregorianCalendar.DAY_OF_WEEK,
								WeekDay.getCalendarDay((WeekDay) wdl.get(0)));
						event.setStartDate(cal.getTime());
					} else {
						er.setKind(RecurrenceKind.monthlybydate);
					}

				} else if (Recur.YEARLY.equals(recur.getFrequency())) {
					er.setKind(RecurrenceKind.yearly);
				}

			} else {
				er.setKind(RecurrenceKind.weekly);
			}
		}
		if (er.getKind() == null) {
			er.setKind(RecurrenceKind.none);
		}
		if (er.getFrequence() < 1) {
			er.setFrequence(1);
		}
		event.setRecurrence(er);
	}

	private void appendAttendees(Event event, Component vEvent) {
		Map<String, Attendee> emails = new HashMap<String, Attendee>();
		for (Property prop : getProperties(vEvent, Property.ATTENDEE)) {
			Attendee att = new Attendee();
			Parameter param = prop.getParameter(Parameter.CN);
			if (param != null) {
				att.setDisplayName(param.getValue());
			}

			int mailIndex = prop.getValue().toLowerCase().indexOf("mailto:");
			if (mailIndex != -1) {
				att.setEmail(prop.getValue().substring(
						mailIndex + "mailto:".length()));
			}

			param = prop.getParameter(Parameter.ROLE);
			if (param != null) {
				int index = param.getValue().indexOf("-");
				if (index != -1) {
					att.setParticipationRole(ParticipationRole.valueOf(param.getValue()
							.substring(0, index).replace("-", "")));
				}
			}

			PartStat partStat = (PartStat) prop
					.getParameter(Parameter.PARTSTAT);
			if (partStat != null) {
				if (partStat.equals(PartStat.IN_PROCESS)) {
					att.setParticipation(Participation.inProgress());
				} else {
					att.setParticipation(Participation.getValueOf(partStat.getValue()));
				}
			} else {
				//rfc5545 : 3.2.12, if PART-STAT is missing, default is NEEDS-ACTION
				att.setParticipation(Participation.needsAction());
			}
			if (att.getEmail() != null && 
					!attendeeAlreadyExist(emails, att)) {
				emails.put(att.getEmail(), att);
			}
		}
		appendOrganizer(emails, vEvent);
		event.addAttendees(new ArrayList<Attendee>(emails.values()));
	}

	private boolean attendeeAlreadyExist(Map<String, Attendee> emails, Attendee att) {
		return emails.containsKey(att.getEmail());
	}

	private void appendOrganizer(Map<String, Attendee> emails,
			Component vEvent) {
		Property prop = vEvent.getProperty(Property.ORGANIZER);
		if(prop != null){
			Organizer orga = (Organizer) prop;
			if(orga.getValue() != null){
				String email = removeMailto(orga);
				
				Attendee organizer = emails.get(email);
				if(organizer != null){
					organizer.setOrganizer(true);
				} else {
					organizer = new Attendee();
					organizer.setEmail(email);
					Parameter cnParam = orga.getParameter(Parameter.CN);
					if(cnParam != null){
						Cn cn = (Cn) cnParam;
						organizer.setDisplayName(cn.getValue());
					}
					organizer.setParticipationRole(ParticipationRole.REQ);
					organizer.setParticipation(Participation.accepted());
					organizer.setOrganizer(true);
					emails.put(organizer.getEmail(), organizer);
				}
			}
		}
	}

	private String removeMailto(Organizer orga) {
		String ret = orga.getValue();
		int mailIndex = ret.toLowerCase().indexOf("mailto:");
		if (mailIndex != -1) {
			ret = orga.getValue().substring(
					mailIndex + "mailto:".length());
		}
		return ret;
	}

	private Calendar initCalendar() {
		Calendar calendar = new Calendar();

		calendar.getProperties().add(
				new ProdId("-//Aliasource Groupe LINAGORA//OBM Calendar //FR"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);

		return calendar;
	}

	@VisibleForTesting ExDate getExDate(Event event) {
		if (eventHasExceptions(event)) {
			return buildExDate(event.getRecurrence());
		} else {
			return null;
		}
	}

	private boolean eventHasExceptions(Event event) {
		if (event.isRecurrent() && event.getRecurrence().hasException() || event.getRecurrence().hasEventException()) {
			return true;
		}
		return false;
	}

	private ExDate buildExDate(EventRecurrence eventRecurrence) {
		DateList exceptionDates = new DateList();
		exceptionDates.setUtc(true);

		for (Date exceptionDeleted : eventRecurrence.getExceptions()) {
			exceptionDates.add(new DateTime(exceptionDeleted));
		}
		return new ExDate(exceptionDates);
	}

	/* package */ VAlarm getVAlarm(Integer alert) {
		if (alert != null && !alert.equals(0)) {
			Dur dur = new Dur(0, 0, 0, -alert);
			VAlarm va = new VAlarm(dur);
			va.getProperties().add(Action.DISPLAY);
			va.getProperties().add(new Description("Default Obm Description"));
			Trigger ti = va.getTrigger();
			ti.getParameters().add(new Value("DURATION"));
			return va;
		}
		return null;
	}

	/* package */ Clazz getClazz(EventPrivacy privacy) {
		if (EventPrivacy.PUBLIC == privacy) {
			return Clazz.PUBLIC;
		}
		return Clazz.PRIVATE;
	}

	/* package */ Organizer getOrganizer(String owner, String ownerEmail) {
		Organizer orga = new Organizer();
		try {
			if (owner != null && !"".equals(owner)) {
				orga.getParameters().add(new Cn(owner));
			}
			if (ownerEmail != null && !"".equals(ownerEmail)) {
				orga.setValue("mailto:" + ownerEmail);
			}
		} catch (URISyntaxException e) {
			logger.error(e.getMessage(), e);
		}
		return orga;
	}

	/* package */ Transp getTransp(EventOpacity eo) {
		Transp transp = Transp.OPAQUE;
		if (EventOpacity.OPAQUE.equals(eo)) {
			transp = Transp.OPAQUE;
		} else if (EventOpacity.TRANSPARENT.equals(eo)) {
			transp = Transp.TRANSPARENT;
		}
		return transp;
	}

	private Due getDue(Date start, int duration) {
		if (start != null && duration >= 0) {
			int durationInMS = duration * 1000;
			DateTime dateTimeEnd = new DateTime(start.getTime() + durationInMS);
			return new Due(dateTimeEnd);
		}
		return null;
	}

	/* package */ DtEnd getDtEnd(Date start, int duration, boolean isAllDays) {
		if (start != null && duration >= 0) {
			net.fortuna.ical4j.model.Date dateTimeEnd = null;
			if (isAllDays) {
				dateTimeEnd = new net.fortuna.ical4j.model.Date(start.getTime()
						+ (duration * 1000) + SECONDS_IN_DAY);
			} else {
				dateTimeEnd = new DateTime(start.getTime() + duration * 1000);
			}
			return new DtEnd(dateTimeEnd, true);
		}
		return null;
	}

	private DtEnd getDtEnd(Date end) {
		return new DtEnd(new DateTime(end));
	}

	@VisibleForTesting DtStart getDtStart(Date start) {
		return new DtStart(new DateTime(start), true);
	}
	
	private DtEnd getDateEnd(Date end) {
		return new DtEnd(new net.fortuna.ical4j.model.Date(end));
	}

	private DtStart getDateStart (Date start) {
		return new DtStart(new net.fortuna.ical4j.model.Date(start), true);
	}

	@VisibleForTesting Duration getDuration(Date startDate, Date endDate) {
		return new Duration(startDate, endDate);
	}

	private RecurrenceId getRecurrenceId(Event event) {
		net.fortuna.ical4j.model.Date dt = null;
		dt = new DateTime(event.getRecurrenceId());
		return new RecurrenceId(dt);
	}

	/* package */ Role getRole(Attendee attendee) {
		Role role = Role.OPT_PARTICIPANT;
		if (ParticipationRole.CHAIR.equals(attendee.getParticipationRole())) {
			role = Role.CHAIR;
		} else if (ParticipationRole.NON.equals(attendee.getParticipationRole())) {
			role = Role.NON_PARTICIPANT;
		} else if (ParticipationRole.OPT.equals(attendee.getParticipationRole())) {
			role = Role.OPT_PARTICIPANT;
		} else if (ParticipationRole.REQ.equals(attendee.getParticipationRole())) {
			role = Role.REQ_PARTICIPANT;
		}

		return role;
	}

	/* package */ Cn getCn(Attendee attendee) {
		if (isEmpty(attendee.getDisplayName())) {
			return new Cn(attendee.getEmail());
		}
		return new Cn(attendee.getDisplayName());
	}

	/* package */ PartStat getPartStat(Attendee attendee) {
		PartStat partStat = PartStat.NEEDS_ACTION;
		if (Participation.accepted().equals(attendee.getParticipation())) {
			partStat = PartStat.ACCEPTED;
		} else if (Participation.completed().equals(attendee.getParticipation())) {
			partStat = PartStat.COMPLETED;
		} else if (Participation.declined().equals(attendee.getParticipation())) {
			partStat = PartStat.DECLINED;
		} else if (Participation.delegated().equals(attendee.getParticipation())) {
			partStat = PartStat.DELEGATED;
		} else if (Participation.inProgress().equals(attendee.getParticipation())) {
			partStat = PartStat.IN_PROCESS;
		} else if (Participation.needsAction().equals(attendee.getParticipation())) {
			partStat = PartStat.NEEDS_ACTION;
		} else if (Participation.tentative().equals(attendee.getParticipation())) {
			partStat = PartStat.TENTATIVE;
		}
		return partStat;
	}

	/* package */ RRule getRRule(Event event) {
		RRule rrule = null;
		Recur recur = getRecur(event.getRecurrence(), event.getStartDate());
		if (recur != null) {
			rrule = new RRule(recur);
		}
		return rrule;
	}

	/* package */ ComponentList getComponents(Calendar calendar,
			String component) {
		return calendar.getComponents(component);
	}

	private List<Property> getProperties(Component comp, String property) {
		List<Property> propsSet = new ArrayList<Property>();
		PropertyList propList = comp.getProperties(property);
		for (Iterator<Property> it = propList.iterator(); it.hasNext();) {
			Property prop = it.next();
			propsSet.add(prop);
		}
		return propsSet;
	}

	private boolean isEmpty(String st) {
		return st == null || "".equals(st);
	}

	private FreeBusyRequest getFreeBusy(VFreeBusy vFreeBusy) {
		FreeBusyRequest fb = new FreeBusyRequest();
		appendOwner(fb, vFreeBusy.getOrganizer());
		fb.setUid(vFreeBusy.getUid().getValue());
		if (vFreeBusy.getStartDate() != null) {
			fb.setStart(vFreeBusy.getStartDate().getDate());
		}

		if (vFreeBusy.getEndDate() != null) {
			fb.setEnd(vFreeBusy.getEndDate().getDate());
		}

		appendAttendee(fb, vFreeBusy);
		return fb;
	}

	private void appendAttendee(FreeBusyRequest fb, VFreeBusy vFreeBusy) {
		List<Property> props = getProperties(vFreeBusy, Property.ATTENDEE);
		for (Property prop : props) {
			Attendee att = new Attendee();
			Parameter param = prop.getParameter(Parameter.CN);
			if (param != null) {
				att.setDisplayName(param.getValue());
			}

			int mailIndex = prop.getValue().toLowerCase().indexOf("mailto:");
			if (mailIndex != -1) {
				att.setEmail(prop.getValue().substring(
						mailIndex + "mailto:".length()));
			}

			param = prop.getParameter(Parameter.ROLE);
			if (param != null) {
				int index = param.getValue().indexOf("-");
				if (index != -1) {
					att.setParticipationRole(ParticipationRole.valueOf(param.getValue()
							.substring(0, index).replace("-", "")));
				}
			}

			PartStat partStat = (PartStat) prop
					.getParameter(Parameter.PARTSTAT);
			if (partStat != null) {
				if (partStat.equals(PartStat.IN_PROCESS)) {
					att.setParticipation(Participation.inProgress());
				} else {
					att.setParticipation(Participation.getValueOf(partStat.getValue()));
				}
			}
			fb.addAttendee(att);
		}
	}

	private void appendOwner(FreeBusyRequest fb, Organizer organizer) {
		if (organizer != null) {
			Parameter cn = organizer.getParameter(Parameter.CN);
			String cnOrganizer = "";
			if (cn != null) {
				cnOrganizer = cn.getValue();
			}

			if (cnOrganizer != null && !"".equals(cnOrganizer)) {
				fb.setOwner(cnOrganizer);
			} else {
				int mailToIndex = organizer.getValue().toLowerCase()
						.indexOf("mailto:");
				if (mailToIndex != -1) {
					fb.setOwner(organizer.getValue().substring(
							mailToIndex + "mailto:".length()));
				}
			}
		}
	}

	public String parseFreeBusy(FreeBusy fb) {
		Calendar calendar = initCalendar();
		calendar.getProperties().add(Method.REPLY);
		VFreeBusy vFreeBusy = getVFreeBusy(fb, fb.getAtt(),	fb.getFreeBusyIntervals());
		calendar.getComponents().add(vFreeBusy);

		return calendar.toString();
	}

	private VFreeBusy getVFreeBusy(FreeBusy fb, Attendee att,
			Set<FreeBusyInterval> fbls) {
		VFreeBusy vfb = new VFreeBusy();
		Organizer orga = getOrganizer("", fb.getOwner());
		vfb.getProperties().add(orga);

		DtStart st = getDtStart(fb.getStart());
		vfb.getProperties().add(st);

		DtEnd en = getDtEnd(fb.getEnd());
		vfb.getProperties().add(en);

		net.fortuna.ical4j.model.property.Attendee at = getAttendee(att);
		vfb.getProperties().add(at);

		if (fb.getUid() != null && !"".equals(fb.getUid())) {
			vfb.getProperties().add(new Uid(fb.getUid()));
		}

		for (FreeBusyInterval line : fbls) {
			DtStart start = getDtStart(line.getStart());
			DtEnd end = getDtEnd(line.getStart(), line.getDuration(),
					line.isAllDay());
			if (start != null && end != null) {
				net.fortuna.ical4j.model.property.FreeBusy fbics = new net.fortuna.ical4j.model.property.FreeBusy();
				Period p = new Period(new DateTime(start.getDate()),
						new DateTime(end.getDate()));
				fbics.getPeriods().add(p);
				FbType type = FbType.BUSY;
				fbics.getParameters().add(type);
				vfb.getProperties().add(fbics);
			}
		}
		return vfb;
	}
}
