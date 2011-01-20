package fr.aliacom.obm.common.calendar;

import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import org.easymock.EasyMock;
import org.easymock.LogicalOperator;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.ParticipationRole;

public class EventChangeHandlerTestsTools {

	static <T, C extends Collection<T>> C compareCollections(C collection) {
		return EasyMock.cmp(collection, new Comparator<C>() {
			public int compare(C o1, C o2) {
				if (o1.size() != o2.size()) {
					return -1;
				}
				Iterator<T> it1 = o1.iterator();
				Iterator<T> it2 = o2.iterator();
				while (it1.hasNext()) {
					if (!it1.next().equals(it2.next())) {
						return -1;
					}
				}
				return 0;
			}
		}, LogicalOperator.EQUAL);
	}
	
	static Date before() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		return cal.getTime();
	}

	static Date after() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, 1);
		return cal.getTime();
	}

	static Date longAfter() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		return cal.getTime();
	}
	
	static Attendee createRequiredAttendee(String email) {
		Attendee attendee = new Attendee();
		attendee.setEmail(email);
		attendee.setRequired(ParticipationRole.REQ);
		return attendee;
	}

	
}
