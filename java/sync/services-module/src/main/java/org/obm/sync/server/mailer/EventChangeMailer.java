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
package org.obm.sync.server.mailer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.obm.configuration.module.LoggerModule;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.RecurrenceDay;
import org.obm.sync.calendar.RecurrenceKind;
import org.obm.sync.server.template.ITemplateLoader;
import org.slf4j.Logger;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import fr.aliacom.obm.common.MailService;
import fr.aliacom.obm.common.calendar.CalendarEncoding;
import fr.aliacom.obm.common.calendar.EventMail;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import freemarker.template.SimpleDate;
import freemarker.template.Template;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;

public class EventChangeMailer extends AbstractMailer {

	private final String baseUrl;
	private final CalendarEncoding calendarEncoding;
	
	@Inject
	/* package */ EventChangeMailer(MailService mailService, ObmSyncConfigurationService constantService, ITemplateLoader templateLoader,
			@Named(LoggerModule.CONFIGURATION)Logger configurationLogger) {
		super(mailService, constantService, templateLoader);
		this.baseUrl = constantService.getObmUIBaseUrl();
		this.calendarEncoding = constantService.getEmailCalendarEncoding();
		configurationLogger.info("OBM web interface URL : {}", baseUrl);
	}
	
	public void notifyNeedActionNewUsers(final ObmUser user, Collection<Attendee> attendee, Event event, 
			Locale locale, TimeZone timezone, String ics, AccessToken token) throws NotificationException {
		
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(user), 
						event.getAttendees(), 
						newUserTitle(event.getOwnerDisplayName(), event.getTitle(), locale, event), 
						inviteNewUserBodyTxt(event, locale, timezone),
						inviteNewUserBodyHtml(event, locale, timezone),
						ics, "REQUEST", calendarEncoding);
			sendNotificationMessageToAttendees(attendee, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}
	
	public void notifyAcceptedNewUsers(final ObmUser synchronizer, Collection<Attendee> attendee, Event event, Locale locale, TimeZone timezone, AccessToken token) throws NotificationException {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(synchronizer), 
						event.getAttendees(), 
						newUserTitle(event.getOwnerDisplayName(), event.getTitle(), locale, event),
						notifyNewUserBodyTxt(event, locale, timezone),
						notifyNewUserBodyHtml(event, locale, timezone));
			sendNotificationMessageToAttendees(attendee, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}

	public void notifyRemovedUsers(final ObmUser synchronizer, Collection<Attendee> attendees, Event event, 
			Locale locale, final TimeZone timezone, String ics, AccessToken token) throws NotificationException {
		
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(synchronizer),
						event.getAttendees(), 
						removedUserTitle(event.getOwnerDisplayName(), event.getTitle(), locale, event), 
						removedUserBodyTxt(event, locale, timezone),
						removedUserBodyHtml(event, locale, timezone), 
						ics, "CANCEL", calendarEncoding);
			sendNotificationMessageToAttendees(attendees, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}

	public void notifyNeedActionUpdateUsers(final ObmUser synchronizer, Collection<Attendee> attendees, 
			Event previous, Event current, Locale locale,
			TimeZone timezone, String ics, AccessToken token) throws NotificationException {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(synchronizer),
						current.getAttendees(), 
						updateUserTitle(current.getOwnerDisplayName(), current.getTitle(), locale, current), 
						inviteUpdateUserBodyTxt(previous, current, locale, timezone),
						inviteUpdateUserBodyHtml(previous, current, locale, timezone), 
						ics, "REQUEST", calendarEncoding);
			sendNotificationMessageToAttendees(attendees, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}
	
	public void notifyAcceptedUpdateUsers(final ObmUser synchronizer, Collection<Attendee> attendees, Event previous, Event current, Locale locale, 
			TimeZone timezone, String ics, AccessToken token) throws NotificationException {

		try {
			EventMail mail = getNotifyUpdateUserEventMail(synchronizer, previous, current, locale, timezone, ics);
			sendNotificationMessageToAttendees(attendees, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}

	public void notifyAcceptedUpdateUsersCanWriteOnCalendar(final ObmUser synchronizer, Collection<Attendee> attendees, Event previous, Event current, 
			Locale locale, TimeZone timezone, AccessToken token) throws NotificationException {
		notifyAcceptedUpdateUsers(synchronizer, attendees, previous, current, locale, timezone, null, token);
	}
	
	private EventMail getNotifyUpdateUserEventMail(final ObmUser synchronizer, Event previous, Event current, Locale locale, TimeZone timezone, String ics) 
			throws UnsupportedEncodingException, IOException, TemplateException {
		
		EventMail eventMail = new EventMail(
				extractSenderAddress(synchronizer),
				current.getAttendees(), 
				updateUserTitle(current.getOwnerDisplayName(), current.getTitle(), locale, current), 
				notifyUpdateUserBodyTxt(previous, current, locale, timezone),
				notifyUpdateUserBodyHtml(previous, current, locale, timezone));
		if (ics != null) {
			eventMail.setIcsContent(ics);
			eventMail.setIcsMethod("REQUEST");
		}
		return eventMail;
	}

	public void notifyOwnerUpdate(final ObmUser synchronizer, Attendee owner, Event previous, Event current, Locale locale,
			TimeZone timezone, AccessToken token) {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(synchronizer),
						current.getAttendees(), 
						updateUserTitle(current.getOwnerDisplayName(), current.getTitle(), locale, previous), 
						notifyUpdateUserBodyTxt(previous, current, locale, timezone),
						notifyUpdateUserBodyHtml(previous, current, locale, timezone));
			sendNotificationMessageToAttendee(owner, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}
	
	public void notifyUpdateParticipation(final Event event, final Attendee organizer, final ObmUser attendeeUpdated,
			final Participation newParticipation, final Locale locale, final TimeZone timezone, String ics, AccessToken token) {
	
		try {
			final EventMail mail = 
				new EventMail(
						extractSenderAddress(attendeeUpdated),
						event.getAttendees(), 
						updateParticipationTitle(event.getTitle(), locale),
						updateParticipationBodyTxt(event, attendeeUpdated, newParticipation, locale, timezone),
						updateParticipationBodyHtml(event, attendeeUpdated, newParticipation, locale, timezone),
						ics, "REPLY", calendarEncoding);
			sendNotificationMessageToOrganizer(organizer, mail, token);
			
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}
	

	public void notifyOwnerRemovedEvent(final ObmUser synchronizer, Attendee owner, Event event, Locale locale, TimeZone timezone, AccessToken token) {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(synchronizer), 
						event.getAttendees(), 
						removedUserTitle(event.getOwnerDisplayName(), event.getTitle(), locale, event),
						removedUserBodyTxt(event, locale, timezone),
						removedUserBodyHtml(event, locale, timezone));
			sendNotificationMessageToAttendee(owner, mail, token);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}


	private InternetAddress extractSenderAddress(final ObmUser user)
	throws UnsupportedEncodingException {
		return new InternetAddress(user.getEmail(), user.getDisplayName());
	}
	
	private List<InternetAddress> convertAttendeesToAddresses(Collection<Attendee> attendees) throws UnsupportedEncodingException {
		List<InternetAddress> internetAddresses = Lists.newArrayList();
		for (Attendee at: attendees) {
			internetAddresses.add(convertAttendeeToAddresse(at));
		}
		return internetAddresses;
	}
	
	private InternetAddress convertAttendeeToAddresse(Attendee attendee) throws UnsupportedEncodingException {
		return new InternetAddress(attendee.getEmail(), attendee.getDisplayName());
	}
	
	private void sendNotificationMessageToAttendee(Attendee attendee, EventMail mail, AccessToken token) throws NotificationException {
		try {
			InternetAddress add = convertAttendeeToAddresse(attendee);
			sendNotificationMessage(mail, add, token);
		} catch (MessagingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		}
	}
	
	private void sendNotificationMessageToAttendees(Collection<Attendee> attendees, EventMail mail, AccessToken token) throws NotificationException {
		try {
			List<InternetAddress> adds = convertAttendeesToAddresses(attendees);
			sendNotificationMessage(mail, adds, token);
		} catch (MessagingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		}
	}
	
	private void sendNotificationMessageToOrganizer(Attendee organizer, EventMail mail, AccessToken token) throws NotificationException {
		try {
			InternetAddress adds = convertAttendeeToAddresse(organizer);
			sendNotificationMessage(mail, ImmutableList.of(adds), token);
		} catch (MessagingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		}
	}

	private void sendNotificationMessage(EventMail mail, List<InternetAddress>  addresses, AccessToken token) throws MessagingException, IOException{
		MimeMessage mimeMail = mail.buildMimeMail(session);
		mailService.sendMessage(addresses, mimeMail, token);
	}
	
	private void sendNotificationMessage(EventMail mail, InternetAddress address, AccessToken token) throws MessagingException, IOException{
		MimeMessage mimeMail = mail.buildMimeMail(session);
		mailService.sendMessage(address, mimeMail, token);
	}
	
	private String participation(Participation participation, Locale locale){
		if(Participation.accepted().equals(participation)){
			return getMessages(locale).accepted();
		} else {
			return getMessages(locale).declined();
		}
	}

	private String newUserTitle(String owner, String title, Locale locale, Event event) {
		if (event.isRecurrent()) {
			return getMessages(locale).newRecurrentEventTitle(owner, title);
		} else {
			return getMessages(locale).newEventTitle(owner, title);
		}
	}
	
	private String updateParticipationTitle(String title, Locale locale) {
		return getMessages(locale).updateParticipationTitle(title);
	}
	
	private String notifyNewUserBodyTxt(Event event, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		if (event.isRecurrent()) {
			return applyEventOnTemplate("RecurrentEventNoticePlain.tpl", event, locale, timezone);
		} else {	
			return applyEventOnTemplate("EventNoticePlain.tpl", event, locale, timezone);
		}
	}
	
	private String notifyNewUserBodyHtml(Event event, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		if (event.isRecurrent()) {
			return applyEventOnTemplate("RecurrentEventNoticeHtml.tpl", event, locale, timezone);			
		} else {
			return applyEventOnTemplate("EventNoticeHtml.tpl", event, locale, timezone);
		}
	}
	
	private String inviteNewUserBodyTxt(Event event, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		if (event.isRecurrent()) {
			return applyEventOnTemplate("RecurrentEventInvitationPlain.tpl", event, locale, timezone);
		} else {
			return applyEventOnTemplate("EventInvitationPlain.tpl", event, locale, timezone);
		}
	}
	
	private String inviteNewUserBodyHtml(Event event, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		if (event.isRecurrent()) {
			return applyEventOnTemplate("RecurrentEventInvitationHtml.tpl", event, locale, timezone);
		} else {
			return applyEventOnTemplate("EventInvitationHtml.tpl", event, locale, timezone);
		}
	}

	private String updateParticipationBodyTxt(Event event,
			final ObmUser attendeeUpdated, Participation newState, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyUpdateParticipationOnTemplate("ParticipationChangePlain.tpl", event, attendeeUpdated, newState, locale, timezone);
	}
	
	private String updateParticipationBodyHtml(Event event,
			final ObmUser attendeeUpdated, Participation newParticipation, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		return applyUpdateParticipationOnTemplate("ParticipationChangeHtml.tpl", event, attendeeUpdated, newParticipation, locale, timezone);
	}
	

	private String removedUserBodyTxt(Event event, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		if (event.isRecurrent()) {
			return applyEventOnTemplate("RecurrentEventCancelPlain.tpl", event, locale, timezone);
		} else {
			return applyEventOnTemplate("EventCancelPlain.tpl", event, locale, timezone);
		}
	}
	
	private String removedUserBodyHtml(Event event, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		if (event.isRecurrent()) {
			return applyEventOnTemplate("RecurrentEventCancelHtml.tpl", event, locale, timezone);
		} else {
			return applyEventOnTemplate("EventCancelHtml.tpl", event, locale, timezone);
		}
	}
	
	private String inviteUpdateUserBodyTxt(Event oldEvent, Event newEvent, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		if (newEvent.isRecurrent()) {
			return applyEventUpdateOnTemplate("RecurrentEventUpdateInvitationPlain.tpl", oldEvent, newEvent, locale, timezone);
		} else {
			return applyEventUpdateOnTemplate("EventUpdateInvitationPlain.tpl", oldEvent, newEvent, locale, timezone);
		}
	}
	
	private String inviteUpdateUserBodyHtml(Event oldEvent, Event newEvent, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		if (newEvent.isRecurrent()) {
			return applyEventUpdateOnTemplate("RecurrentEventUpdateInvitationHtml.tpl", oldEvent, newEvent, locale, timezone);
		} else {
			return applyEventUpdateOnTemplate("EventUpdateInvitationHtml.tpl", oldEvent, newEvent, locale, timezone);
		}
	}
	
	private String notifyUpdateUserBodyTxt(Event oldEvent, Event newEvent, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		if (newEvent.isRecurrent()) {
			return applyEventUpdateOnTemplate("RecurrentEventUpdateNoticePlain.tpl", oldEvent, newEvent, locale, timezone);
		} else {
			return applyEventUpdateOnTemplate("EventUpdateNoticePlain.tpl", oldEvent, newEvent, locale, timezone);
		}
	}
	
	private String notifyUpdateUserBodyHtml(Event oldEvent, Event newEvent, Locale locale, TimeZone timezone) throws IOException, TemplateException {
		if (newEvent.isRecurrent()) {
			return applyEventUpdateOnTemplate("RecurrentEventUpdateNoticeHtml.tpl", oldEvent, newEvent, locale, timezone);		
		} else {
			return applyEventUpdateOnTemplate("EventUpdateNoticeHtml.tpl", oldEvent, newEvent, locale, timezone);
		}
	}
	
	private String applyEventUpdateOnTemplate(String templateName, Event oldEvent, Event newEvent, Locale locale, TimeZone timezone) 
		throws TemplateException, IOException {
		
		Builder<Object, Object> builder = buildEventUpdateDatamodel(oldEvent, newEvent, locale);
		ImmutableMap<Object, Object> datamodel = defineTechnicalData(builder, newEvent).build();
		Template template = templateLoader.getTemplate(templateName, locale, timezone);
		return applyTemplate(datamodel, template);
	}

	private String applyEventOnTemplate(String templateName, Event event, Locale locale, TimeZone timezone)
			throws IOException, TemplateException {
		Builder<Object, Object> builder = buildEventDatamodel(event, locale);
		ImmutableMap<Object, Object> datamodel = defineTechnicalData(builder, event).build();
		Template template = templateLoader.getTemplate(templateName, locale, timezone);
		return applyTemplate(datamodel, template);
	}
	
	private String applyUpdateParticipationOnTemplate(String templateName,
			Event event, final ObmUser attendeeUpdated, Participation newState,  Locale locale, TimeZone timezone) throws IOException, TemplateException {
		Builder<Object, Object> builder = buildUpdateParticipationDatamodel(event, attendeeUpdated, newState, locale);
		ImmutableMap<Object, Object> datamodel = defineTechnicalData(builder, event).build();
		Template template = templateLoader.getTemplate(templateName, locale, timezone);
		return applyTemplate(datamodel, template);
	}

	private Builder<Object, Object> defineTechnicalData(Builder<Object, Object> builder, Event event) {
		return builder
			.put("host", this.baseUrl)
			.put("calendarId", event.getObmId().serializeToString());
	}
	
	private Builder<Object, Object> buildDefaultEventDataModel(Event event,
			Locale locale) {
		Builder<Object, Object> datamodel = null;
		String attendees = buildAttendeesFromEvent(event.getAttendees(), locale);
		
		datamodel = ImmutableMap.builder()
				.put("subject", Strings.nullToEmpty(event.getTitle()))
				.put("location", Strings.nullToEmpty(event.getLocation()))
				.put("organizer", Strings.nullToEmpty(event.getOwnerDisplayName()))
				.put("creator", Strings.nullToEmpty(event.getCreatorDisplayName()))
				.put("attendees", attendees);
		return datamodel;
	}

	private Builder<Object, Object> buildEventDatamodel(Event event, Locale locale) {
		Builder<Object, Object> datamodel = buildDefaultEventDataModel(event,
				locale);
		
		if (event.isRecurrent()) {
			datamodel
				.put("start", new SimpleDate(event.getStartDate(), TemplateDateModel.DATETIME))
				.put("startTime", new SimpleDate(event.getStartDate(), TemplateDateModel.TIME))
				.put("startDate", new SimpleDate(event.getStartDate(), TemplateDateModel.DATE))
				.put("end", new SimpleDate(event.getEndDate(), TemplateDateModel.DATETIME))
				.put("endTime", new SimpleDate(event.getEndDate(), TemplateDateModel.TIME))
				.put("endDate", new SimpleDate(event.getEndDate(), TemplateDateModel.DATE))
				.put("recurrenceKind", describeRecurrence(locale, event.getRecurrence()))
				.put("recurrenceEnd", recurrenceEnd(locale, event.getRecurrence()));
			
		} else {
			datamodel
				.put("start", new SimpleDate(event.getStartDate(), TemplateDateModel.DATETIME))
				.put("startDate", new SimpleDate(event.getStartDate(), TemplateDateModel.DATE))
				.put("startTime", new SimpleDate(event.getStartDate(), TemplateDateModel.TIME))
				.put("end", new SimpleDate(event.getEndDate(), TemplateDateModel.DATETIME))
				.put("endDate", new SimpleDate(event.getEndDate(), TemplateDateModel.DATE))
				.put("endTime", new SimpleDate(event.getEndDate(), TemplateDateModel.TIME))
				.put("recurrenceKind", getMessages(locale).withoutRecurrence())
				.put("recurrenceEnd", new SimpleDate(event.getEndDate(), TemplateDateModel.DATE));
		}

		return datamodel;

	}

	private Object recurrenceEnd(Locale locale, EventRecurrence currentEventRecurrence) {
		Date recurrenceEnd = currentEventRecurrence.getEnd();
		if (recurrenceEnd != null) {
			return new SimpleDate(recurrenceEnd, TemplateDateModel.DATE);
		} else {
			return getMessages(locale).withoutRecurrenceEndDate();
		}
	}

	private String describeRecurrence(Locale locale, EventRecurrence currentEventRecurrence) {
		String recurrenceInfo = recurrenceInfo(locale, currentEventRecurrence);
		return recurrenceInfo + buildRepeatDaysFromEventRecurrence(currentEventRecurrence, locale);
	}

	private String recurrenceInfo(Locale locale, EventRecurrence currentEventRecurrence) {
		int frequency = currentEventRecurrence.getFrequence();
		if (frequency > 1) {
			return buildRecurrenceInfoFromEvent(currentEventRecurrence, locale, frequency);
		} else {
			return buildRecurrenceInfoFromEventWithInsignificantFrequency(currentEventRecurrence, locale);
		}
	}
	
	private String buildRecurrenceInfoFromEvent(EventRecurrence eventRecurrence, Locale locale, int frequency) {
		StringBuilder recurrenceInfo = new StringBuilder();
		
		switch (eventRecurrence.getKind()) {
			case daily: 
				recurrenceInfo.append(getMessages(locale).dailyRecurrenceInfoWithFrequency(frequency));
				break;
			case weekly:
				recurrenceInfo.append(getMessages(locale).weeklyRecurrenceInfoWithFrequency(frequency));
				break;				
			case monthlybydate:
			case monthlybyday:
				recurrenceInfo.append(getMessages(locale).monthlyRecurrenceInfoWithFrequency(frequency));
				break;
			case yearly:
			case yearlybyday:
				recurrenceInfo.append(getMessages(locale).annuallyRecurrenceInfoWithFrequency(frequency));	
				break;
			case none:
				recurrenceInfo.append(getMessages(locale).withoutRecurrence());
				break;
		}

		return recurrenceInfo.toString();
	}
	
	private String buildRecurrenceInfoFromEventWithInsignificantFrequency(EventRecurrence eventRecurrence, Locale locale) {
		StringBuilder recurrenceInfo = new StringBuilder();
		
		switch (eventRecurrence.getKind()) {
			case daily: 
				recurrenceInfo.append(getMessages(locale).dailyRecurrenceInfoWithoutFrequency());
				break;
			case weekly:
				recurrenceInfo.append(getMessages(locale).weeklyRecurrenceInfoWithoutFrequency());
				break;				
			case monthlybydate:
			case monthlybyday:
				recurrenceInfo.append(getMessages(locale).monthlyRecurrenceInfoWithoutFrequency());
				break;
			case yearly:
			case yearlybyday:
				recurrenceInfo.append(getMessages(locale).annuallyRecurrenceInfoWithoutFrequency());	
				break;
			case none:
				recurrenceInfo.append(getMessages(locale).withoutRecurrence());
				break;
		}

		return recurrenceInfo.toString();		
	}
	
	private String buildRepeatDaysFromEventRecurrence(EventRecurrence eventRecurrence, final Locale locale) {
		String repeatDaysString;
		if (eventRecurrence.getKind() != RecurrenceKind.daily) {
			Collection<RecurrenceDay> repeatDays= eventRecurrence.getDays();
			
			List<RecurrenceDay> recurrenceDays = new ArrayList<RecurrenceDay>(
					repeatDays.size());
			recurrenceDays.addAll(repeatDays);
			Collections.sort(recurrenceDays, new Comparator<RecurrenceDay>() {
				@Override
				public int compare(RecurrenceDay rd1, RecurrenceDay rd2) {
					return Ints.compare(rd1.ordinal(), rd2.ordinal());
				}
			});
			
			Collection<String> repeatDaysStrings = Collections2.transform(recurrenceDays, new Function<RecurrenceDay, String>() {

				@Override
				public String apply(RecurrenceDay day) {
					String dayString;
					switch(day) {
					case Sunday: 
						dayString = getMessages(locale).sunday();
						break;
					case Monday:
						dayString = getMessages(locale).monday();
						break;
					case Tuesday:
						dayString = getMessages(locale).tuesday();
						break;
					case Wednesday:
						dayString = getMessages(locale).wednesday();
						break;
					case Thursday:
						dayString = getMessages(locale).thursday();
						break;
					case Friday:
						dayString = getMessages(locale).friday();
						break;
					case Saturday:
						dayString = getMessages(locale).saturday();
						break;
					default:
						throw new IllegalArgumentException("Unknown week day: " + day);
				}
					return dayString;

				}
			});
			
			Joiner joiner = Joiner.on(", ");			
			repeatDaysString = joiner.join(repeatDaysStrings);
		}
		else {
			repeatDaysString = "";
		}
		return repeatDaysString.isEmpty() ? "" : " [" + repeatDaysString + "]";
	}
	
	private String buildAttendeesFromEvent(List<Attendee> attendeesList, Locale locale) {
		StringBuilder attendees = new StringBuilder();

		for (Attendee attendee : attendeesList) {
			if (attendee.getDisplayName() != null) {
				attendees.append(attendee.getDisplayName() + " - "
						+ buildParticipationOfAttendee(attendee, locale)
						+ "<br/>");
			}
		}

		return attendees.toString();
	}
	
	private String buildParticipationOfAttendee(Attendee attendee, Locale locale) {
		Participation participation = attendee.getParticipation();
		String stateString = "";
		if (participation != null) {
			switch (participation.getState()) {
			case NEEDSACTION:
				stateString = getMessages(locale).needsAction();
				break;
			case ACCEPTED:
				stateString = getMessages(locale).accepted();
				break;
			case DECLINED:
				stateString = getMessages(locale).declined();
				break;
			case COMPLETED:
			case DELEGATED:
			case INPROGRESS:
			case TENTATIVE:
				break;
			}
		}
		return stateString;
	}
	
	private Builder<Object, Object> buildEventUpdateDatamodel(Event oldEvent, Event newEvent, Locale locale) {
		Builder<Object, Object> datamodel = ImmutableMap.builder()
			.put("old", buildEventDatamodel(oldEvent, locale).build())
			.put("new", buildEventDatamodel(newEvent, locale).build());
		return datamodel;
	}
	
	@VisibleForTesting Builder<Object, Object> buildUpdateParticipationDatamodel(
			Event event, final ObmUser attendeeUpdated, Participation participation, Locale locale) {
		Builder<Object, Object> datamodel = ImmutableMap.builder()
			.put("user", attendeeUpdated.getDisplayName())
			.put("participation", participation(participation, locale))
			.put("comment", Strings.nullToEmpty(participation.getSerializedCommentToString()))
			.put("subject", Strings.nullToEmpty(event.getTitle()))
			.put("startDate", new SimpleDate(event.getStartDate(), TemplateDateModel.DATETIME));
		return datamodel;
	}
	
	private String removedUserTitle(String owner, String title, Locale locale, Event event) {
		if (event.isRecurrent()) {
			return getMessages(locale).canceledRecurrentEventTitle(owner, title);
		} else {
			return getMessages(locale).canceledEventTitle(owner, title);
		}
	}

	private String updateUserTitle(String owner, String title, Locale locale, Event event) {
		if (event.isRecurrent()) {
			return getMessages(locale).updatedRecurrentEventTitle(owner, title);
		} else {
			return getMessages(locale).updatedEventTitle(owner, title);
		}
	}

	/* package */ void setMailService(MailService mailService) {
		this.mailService = mailService;
	}
}
