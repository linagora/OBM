package fr.aliacom.obm.common.calendar;


import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;

public class EventUtils {

	public static boolean isInternalEvent(Event evInDb) {
		if (evInDb == null) {
			return false;
		}
		int nbObmUser = 0;
		for (Attendee att : evInDb.getAttendees()) {
			if(att.isObmUser()){
				nbObmUser++;
			}
			if (att.isOrganizer()) {
				return att.isObmUser();
			}
		}
		return nbObmUser > 1;
	}
}
