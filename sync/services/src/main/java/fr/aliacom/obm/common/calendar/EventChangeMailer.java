package fr.aliacom.obm.common.calendar;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.obm.sync.Messages;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import fr.aliacom.obm.services.constant.ConstantService;
import fr.aliacom.obm.utils.Ical4jHelper;
import freemarker.template.Configuration;
import freemarker.template.SimpleDate;
import freemarker.template.Template;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateException;

public class EventChangeMailer {

	private final String baseUrl;
	private final Configuration cfg;
	private final Session session;
	private MailService mailService;
	
	public static class NotificationException extends RuntimeException {
		private static final long serialVersionUID = -7984056189522385977L;

		public NotificationException(Exception e) {
			super(e);
		}
	}
	
	@Inject
	/* package */ EventChangeMailer(MailService mailService, ConstantService constantService) {
		this.mailService = mailService;
		this.baseUrl = constantService.getObmUIBaseUrl();
		cfg = new Configuration();
		cfg.setClassForTemplateLoading(getClass(), "template");
		session = Session.getDefaultInstance(new Properties());
	}
	
	public void notifyNewUsers(Collection<Attendee> attendee, Event event, Locale locale) throws NotificationException {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(event), 
						event.getAttendees(), 
						newUserTitle(event.getOwner(), event.getTitle(), locale), 
						newUserBodyTxt(event, locale),
						newUserBodyHtml(event, locale),
						newUserIcs(event), "REQUEST");
			sendNotificationMessageToAttendee(attendee, mail);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}

	public void notifyRemovedUsers(Collection<Attendee> attendees, Event event, Locale locale) throws NotificationException {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(event),
						event.getAttendees(), 
						removedUserTitle(event.getOwner(), event.getTitle(), locale), 
						removedUserBodyTxt(event, locale),
						removedUserBodyHtml(event, locale), 
						removedUserIcs(event), "CANCEL");
			sendNotificationMessageToAttendee(attendees, mail);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}

	public void notifyUpdateUsers(Collection<Attendee> attendees, Event previous, Event current, Locale locale) throws NotificationException {
		try {
			EventMail mail = 
				new EventMail(
						extractSenderAddress(current),
						current.getAttendees(), 
						updateUserTitle(current.getOwner(), current.getTitle(), locale), 
						updateUserBodyTxt(previous, current, locale),
						updateUserBodyHtml(previous, current, locale), 
						updateUserIcs(current), "REQUEST");
			sendNotificationMessageToAttendee(attendees, mail);
		} catch (UnsupportedEncodingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		} catch (TemplateException e) {
			throw new NotificationException(e);
		}
	}
	
	private InternetAddress extractSenderAddress(Event event)
			throws UnsupportedEncodingException {
		return new InternetAddress(event.getOwnerEmail(), event.getOwner());
	}

	private List<InternetAddress> convertAttendeesToAddresses(Collection<Attendee> attendees) throws UnsupportedEncodingException {
		List<InternetAddress> internetAddresses = Lists.newArrayList();
		for (Attendee at: attendees) {
			internetAddresses.add(new InternetAddress(at.getEmail(), at.getDisplayName()));
		}
		return internetAddresses;
	}
	
	private void sendNotificationMessageToAttendee(Collection<Attendee> attendees, EventMail mail) throws NotificationException {
		try {
			MimeMessage mimeMail = mail.buildMimeMail(session);
			mailService.sendMessage(session, convertAttendeesToAddresses(attendees), mimeMail);
		} catch (MessagingException e) {
			throw new NotificationException(e);
		} catch (IOException e) {
			throw new NotificationException(e);
		}
	}

	private String newUserTitle(String owner, String title, Locale locale) {
		return new Messages(locale).newEventTitle(owner, title);
	}
	
	private String newUserBodyTxt(Event event, Locale locale) throws IOException, TemplateException {
		return applyEventOnTemplate("EventInvitationPlain.tpl", event, locale);
	}
	
	private String newUserBodyHtml(Event event, Locale locale) throws IOException, TemplateException {
		return applyEventOnTemplate("EventInvitationHtml.tpl", event, locale);
	}

	private String removedUserBodyTxt(Event event, Locale locale) throws IOException, TemplateException {
		return applyEventOnTemplate("EventCancelPlain.tpl", event, locale);
	}
	
	private String removedUserBodyHtml(Event event, Locale locale) throws IOException, TemplateException {
		return applyEventOnTemplate("EventCancelHtml.tpl", event, locale);
	}
	
	private String updateUserBodyTxt(Event oldEvent, Event newEvent, Locale locale) throws IOException, TemplateException {
		return applyEventUpdateOnTemplate("EventUpdatePlain.tpl", oldEvent, newEvent, locale);
	}
	
	private String updateUserBodyHtml(Event oldEvent, Event newEvent, Locale locale) throws IOException, TemplateException {
		return applyEventUpdateOnTemplate("EventUpdateHtml.tpl", oldEvent, newEvent, locale);
	}
	
	private String applyEventUpdateOnTemplate(String templateName, Event oldEvent, Event newEvent, Locale locale) 
		throws TemplateException, IOException {
		
		Builder<Object, Object> builder = buildEventUpdateDatamodel(oldEvent, newEvent);
		ImmutableMap<Object, Object> datamodel = defineTechnicalData(builder, newEvent).build();
		Template template = cfg.getTemplate(templateName, locale);
		return applyTemplate(datamodel, template);
	}

	private String applyEventOnTemplate(String templateName, Event event, Locale locale)
			throws IOException, TemplateException {
		Builder<Object, Object> builder = buildEventDatamodel(event);
		ImmutableMap<Object, Object> datamodel = defineTechnicalData(builder, event).build();
		Template template = cfg.getTemplate(templateName, locale);
		return applyTemplate(datamodel, template);
	}

	private Builder<Object, Object> defineTechnicalData(Builder<Object, Object> builder, Event event) {
		return builder
			.put("host", this.baseUrl)
			.put("calendarId", event.getUid());
	}
	
	private String applyTemplate(ImmutableMap<Object, Object> datamodel, Template template) 
		throws TemplateException, IOException {
		
		StringWriter stringWriter = new StringWriter();
		template.process(datamodel, stringWriter);
		stringWriter.flush();
		return stringWriter.toString();
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
	
	private String newUserIcs(Event event) {
		return Ical4jHelper.buildIcsInvitationRequest(event);
	}

	private String removedUserIcs(Event event) {
		return Ical4jHelper.buildIcsInvitationCancel(event);
	}

	private String removedUserTitle(String owner, String title, Locale locale) {
		return new Messages(locale).canceledEventTitle(owner, title);
	}

	private String updateUserTitle(String owner, String title, Locale locale) {
		return new Messages(locale).updatedEventTitle(owner, title);
	}

	private String updateUserIcs(Event current) {
		return Ical4jHelper.buildIcsInvitationRequest(current);
	}

	/* package */ void setMailService(MailService mailService) {
		this.mailService = mailService;
	}
	
}
