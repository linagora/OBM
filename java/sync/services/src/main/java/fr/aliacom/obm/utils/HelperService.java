package fr.aliacom.obm.utils;

import java.util.List;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;

public interface HelperService {

	boolean canWriteOnCalendar(AccessToken writer, String targetCalendar);
	boolean canReadCalendar(AccessToken writer, String targetCalendar);
	String constructEmailFromList(String listofmail, String domain);
	boolean attendeesContainsUser(List<Attendee> attendees, AccessToken token);
	String getMD5Diggest(String plaintext);

}
