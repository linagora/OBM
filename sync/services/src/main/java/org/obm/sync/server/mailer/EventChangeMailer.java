package org.obm.sync.server.mailer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.obm.sync.Messages;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.server.template.ITemplateLoader;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import fr.aliacom.obm.common.MailService;
import fr.aliacom.obm.common.calendar.EventMail;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.services.constant.ConstantService;
import fr.aliacom.obm.utils.Ical4jHelper;
import freemarker.template.SimpleDate;
import freemarker.template.Template;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;

public class EventChangeMailer extends AbstractMailer{

	private final String baseUrl;
	
	@Inject
	/* package */ EventChangeMailer(MailService mailService, ConstantService constantService, ITemplateLoader templateLoader) {
		super(mailService, constantService, templateLoader);
		this.baseUrl = constantService.getObmUIBaseUrl();
	}
	
	public void notifyNeedActionNewUsers(AccessToken at, Collection<Attendee> attendee, Event event, Locale locale) throws NotificationException {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(event), 
						event.getAttendees(), 
						newUserTitle(event.getOwner(), event.getTitle(), locale), 
						inviteNewUserBodyTxt(event, locale),
						inviteNewUserBodyHtml(event, locale),
						newUserIcs(at, event), "REQUEST");
			sendNotificationMessageToAttendee(attendee, mail);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}
	
	public void notifyAcceptedNewUsers(Collection<Attendee> attendee, Event event, Locale locale) throws NotificationException {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(event), 
						event.getAttendees(), 
						newUserTitle(event.getOwner(), event.getTitle(), locale),
						notifyNewUserBodyTxt(event, locale),
						notifyNewUserBodyHtml(event, locale));
			sendNotificationMessageToAttendee(attendee, mail);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}

	public void notifyRemovedUsers(AccessToken at, Collection<Attendee> attendees, Event event, Locale locale) throws NotificationException {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(event),
						event.getAttendees(), 
						removedUserTitle(event.getOwner(), event.getTitle(), locale), 
						removedUserBodyTxt(event, locale),
						removedUserBodyHtml(event, locale), 
						removedUserIcs(at, event), "CANCEL");
			sendNotificationMessageToAttendee(attendees, mail);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}

	public void notifyNeedActionUpdateUsers(AccessToken at, Collection<Attendee> attendees, Event previous, Event current, Locale locale) throws NotificationException {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(current),
						current.getAttendees(), 
						updateUserTitle(current.getOwner(), current.getTitle(), locale), 
						inviteUpdateUserBodyTxt(previous, current, locale),
						inviteUpdateUserBodyHtml(previous, current, locale), 
						updateUserIcs(at, current), "REQUEST");
			sendNotificationMessageToAttendee(attendees, mail);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}
	
	public void notifyAcceptedUpdateUsers(Collection<Attendee> attendees, Event previous, Event current, Locale locale) throws NotificationException {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(current),
						current.getAttendees(), 
						updateUserTitle(current.getOwner(), current.getTitle(), locale), 
						notifyUpdateUserBodyTxt(previous, current, locale),
						notifyUpdateUserBodyHtml(previous, current, locale));
			sendNotificationMessageToAttendee(attendees, mail);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}
	
	public void notifyUpdateParticipationState(Event event, Attendee organizer, ObmUser attendeeUpdated, ParticipationState newState, Locale locale) {
		try {
			
			EventMail mail = 
				new EventMail(
						extractSenderAddress(attendeeUpdated),
						event.getAttendees(), 
						updateParticipationStateTitle(event.getTitle(), locale), 
						updateParticipationStateBodyTxt(event, attendeeUpdated, newState, locale),
						updateParticipationStateBodyHtml(event, attendeeUpdated, newState, locale));
			sendNotificationMessageToOrganizer(organizer, mail);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}

	private InternetAddress extractSenderAddress(ObmUser user)
	throws UnsupportedEncodingException {
		return new InternetAddress(user.getEmailAtDomain(), user.getDisplayName());
	}
	
	private InternetAddress extractSenderAddress(Event event)
			throws UnsupportedEncodingException {
		return new InternetAddress(event.getOwnerEmail(), event.getOwner());
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
	
	
	
	private void sendNotificationMessageToAttendee(Collection<Attendee> attendees, EventMail mail) throws NotificationException {
		try {
			List<InternetAddress> adds = convertAttendeesToAddresses(attendees);
			sendNotificationMessage(mail, adds);
		} catch (MessagingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		}
	}
	
	private void sendNotificationMessageToOrganizer(Attendee organizer, EventMail mail) throws NotificationException {
		try {
			InternetAddress adds = convertAttendeeToAddresse(organizer);
			sendNotificationMessage(mail, ImmutableList.of(adds));
		} catch (MessagingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		}
	}

	private void sendNotificationMessage(EventMail mail, List<InternetAddress>  addresses) throws MessagingException, IOException{
		MimeMessage mimeMail = mail.buildMimeMail(session);
		mailService.sendMessage(session, addresses, mimeMail);
	}
	
	private String participationState(ParticipationState state, Locale locale){
		if(ParticipationState.ACCEPTED.equals(state)){
			return new Messages(locale).participationStateAccepted();
		} else {
			return new Messages(locale).participationStateDeclined();
		}
	}

	private String newUserTitle(String owner, String title, Locale locale) {
		return new Messages(locale).newEventTitle(owner, title);
	}
	
	private String updateParticipationStateTitle(String title, Locale locale) {
		return new Messages(locale).updateParticipationStateTitle(title);
	}
	
	private String notifyNewUserBodyTxt(Event event, Locale locale) throws IOException, TemplateException {
		return applyEventOnTemplate("EventNoticePlain.tpl", event, locale);
	}
	
	private String notifyNewUserBodyHtml(Event event, Locale locale) throws IOException, TemplateException {
		return applyEventOnTemplate("EventNoticeHtml.tpl", event, locale);
	}
	
	private String inviteNewUserBodyTxt(Event event, Locale locale) throws IOException, TemplateException {
		return applyEventOnTemplate("EventInvitationPlain.tpl", event, locale);
	}
	
	private String inviteNewUserBodyHtml(Event event, Locale locale) throws IOException, TemplateException {
		return applyEventOnTemplate("EventInvitationHtml.tpl", event, locale);
	}
	
	private String updateParticipationStateBodyTxt(Event event,
			ObmUser attendeeUpdated, ParticipationState newState, Locale locale) throws IOException, TemplateException {
		return applyUpdateParticipationStateOnTemplate("ParticipationStateChangePlain.tpl", event, attendeeUpdated, newState, locale);
	}
	
	private String updateParticipationStateBodyHtml(Event event,
			ObmUser attendeeUpdated, ParticipationState newState, Locale locale) throws IOException, TemplateException {
		return applyUpdateParticipationStateOnTemplate("ParticipationStateChangeHtml.tpl", event, attendeeUpdated, newState, locale);
	}
	

	private String removedUserBodyTxt(Event event, Locale locale) throws IOException, TemplateException {
		return applyEventOnTemplate("EventCancelPlain.tpl", event, locale);
	}
	
	private String removedUserBodyHtml(Event event, Locale locale) throws IOException, TemplateException {
		return applyEventOnTemplate("EventCancelHtml.tpl", event, locale);
	}
	
	private String inviteUpdateUserBodyTxt(Event oldEvent, Event newEvent, Locale locale) throws IOException, TemplateException {
		return applyEventUpdateOnTemplate("EventUpdateInvitationPlain.tpl", oldEvent, newEvent, locale);
	}
	
	private String inviteUpdateUserBodyHtml(Event oldEvent, Event newEvent, Locale locale) throws IOException, TemplateException {
		return applyEventUpdateOnTemplate("EventUpdateInvitationHtml.tpl", oldEvent, newEvent, locale);
	}
	
	private String notifyUpdateUserBodyTxt(Event oldEvent, Event newEvent, Locale locale) throws IOException, TemplateException {
		return applyEventUpdateOnTemplate("EventUpdateNoticePlain.tpl", oldEvent, newEvent, locale);
	}
	
	private String notifyUpdateUserBodyHtml(Event oldEvent, Event newEvent, Locale locale) throws IOException, TemplateException {
		return applyEventUpdateOnTemplate("EventUpdateNoticeHtml.tpl", oldEvent, newEvent, locale);
	}
	
	private String applyEventUpdateOnTemplate(String templateName, Event oldEvent, Event newEvent, Locale locale) 
		throws TemplateException, IOException {
		
		Builder<Object, Object> builder = buildEventUpdateDatamodel(oldEvent, newEvent);
		ImmutableMap<Object, Object> datamodel = defineTechnicalData(builder, newEvent).build();
		Template template = templateLoader.getTemplate(templateName, locale);
		return applyTemplate(datamodel, template);
	}

	private String applyEventOnTemplate(String templateName, Event event, Locale locale)
			throws IOException, TemplateException {
		Builder<Object, Object> builder = buildEventDatamodel(event);
		ImmutableMap<Object, Object> datamodel = defineTechnicalData(builder, event).build();
		Template template = templateLoader.getTemplate(templateName, locale);
		return applyTemplate(datamodel, template);
	}
	
	private String applyUpdateParticipationStateOnTemplate(String templateName,
			Event event, ObmUser attendeeUpdated, ParticipationState newState,  Locale locale) throws IOException, TemplateException {
		Builder<Object, Object> builder = buildUpdateParticipationStateDatamodel(event, attendeeUpdated, participationState(newState, locale));
		Template template = templateLoader.getTemplate(templateName, locale);
		return applyTemplate(builder.build(), template);
	}

	private Builder<Object, Object> defineTechnicalData(Builder<Object, Object> builder, Event event) {
		return builder
			.put("host", this.baseUrl)
			.put("calendarId", event.getUid());
	}

	private Builder<Object, Object> buildEventDatamodel(Event event) {
		Builder<Object, Object> datamodel = ImmutableMap.builder()
			.put("start", new SimpleDate(event.getDate(), TemplateDateModel.DATETIME))
			.put("end", new SimpleDate(event.getEndDate(), TemplateDateModel.DATETIME))
			.put("subject", Strings.nullToEmpty(event.getTitle()))
			.put("location", Strings.nullToEmpty(event.getLocation()))
			.put("author", Strings.nullToEmpty(event.getOwner()));
		return datamodel;
	}
	
	private Builder<Object, Object> buildEventUpdateDatamodel(Event oldEvent, Event newEvent) {
		Builder<Object, Object> datamodel = ImmutableMap.builder()
			.put("old", buildEventDatamodel(oldEvent).build())
			.put("new", buildEventDatamodel(newEvent).build());
		return datamodel;
	}
	
	private Builder<Object, Object> buildUpdateParticipationStateDatamodel(
			Event event, ObmUser attendeeUpdated, String state) {
				Builder<Object, Object> datamodel = ImmutableMap.builder()
			.put("user", attendeeUpdated.getDisplayName())
			.put("participationState", state)
			.put("subject", Strings.nullToEmpty(event.getTitle()))
			.put("start", new SimpleDate(event.getDate(), TemplateDateModel.DATETIME));
		return datamodel;
	}
	
	private String newUserIcs(AccessToken at, Event event) {
		return Ical4jHelper.buildIcsInvitationRequest(at, event);
	}

	private String removedUserIcs(AccessToken at, Event event) {
		return Ical4jHelper.buildIcsInvitationCancel(at, event);
	}

	private String removedUserTitle(String owner, String title, Locale locale) {
		return new Messages(locale).canceledEventTitle(owner, title);
	}

	private String updateUserTitle(String owner, String title, Locale locale) {
		return new Messages(locale).updatedEventTitle(owner, title);
	}

	private String updateUserIcs(AccessToken at, Event current) {
		return Ical4jHelper.buildIcsInvitationRequest(at, current);
	}

	/* package */ void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

}
