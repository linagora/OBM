package fr.aliacom.obm.common.user;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;

import fr.aliacom.obm.common.FindException;

public interface UserService {

	ObmUser getUserFromAccessToken(AccessToken token);
	ObmUser getUserFromLogin(String login, String domainName);
	ObmUser getUserFromCalendar(String calendar, String domainName) throws FindException;
	ObmUser getUserFromAttendee(Attendee organizer, String domainName);
	
}
