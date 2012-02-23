package fr.aliacom.obm.common.calendar;

import javax.jms.JMSException;

import org.obm.icalendar.ICalendarFactory;
import org.obm.icalendar.Ical4jHelper;
import org.obm.icalendar.Ical4jUser;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;
import org.obm.sync.server.mailer.AbstractMailer.NotificationException;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.linagora.obm.sync.Producer;

import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;

@Singleton
public class JMSServiceImpl implements JMSService {

	private final Ical4jHelper ical4jHelper;
	private final UserService userService;
	private final ICalendarFactory calendarFactory;
	private final Producer producer;

	@Inject
	private JMSServiceImpl(UserService userService, Ical4jHelper ical4jHelper, ICalendarFactory calendarFactory,
			Producer producer) {
		
		this.userService = userService;
		this.ical4jHelper = ical4jHelper;
		this.calendarFactory = calendarFactory;
		this.producer = producer;
	}
	
	@Override
	public void writeIcsInvitationRequest(AccessToken token, Event current) {
		Ical4jUser buildIcal4jUser = getIcal4jUser(token);
		String ics = ical4jHelper.buildIcsInvitationRequest(buildIcal4jUser, current);		
		writeIcs(ics);
	}

	@Override
	public void writeIcsInvitationCancel(AccessToken token, Event event) {
		Ical4jUser buildIcal4jUser = getIcal4jUser(token);
		String ics = ical4jHelper.buildIcsInvitationCancel(buildIcal4jUser, event);
		writeIcs(ics);
	}
	
	@Override
	public void writeIcsInvitationReply(AccessToken token, Event event) {
		Ical4jUser buildIcal4jUser = getIcal4jUser(token);
		String ics = ical4jHelper.buildIcsInvitationReply(event, buildIcal4jUser);
		writeIcs(ics);
	}

	private Ical4jUser getIcal4jUser(AccessToken token) {
		ObmUser user = userService.getUserFromAccessToken(token);
		Ical4jUser buildIcal4jUser = calendarFactory.createIcal4jUserFromObmUser(user);
		return buildIcal4jUser;
	}

	private void writeIcs(String... ics)  {
		try {
			for (String s: ics) {
				producer.write(s);	
			}
		} catch (JMSException e) {
			throw new NotificationException(e);
		}
	}

}
