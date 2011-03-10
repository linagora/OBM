package fr.aliacom.obm.utils;

import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;

import com.google.common.base.Objects;

/**
 * RFC2445 standard
 * Internet Calendaring and Scheduling Core Object Specification 
 * 						(iCalendar)
 * 
 * this functions returns default value of RFC2445
 */
public class RFC2445 {

	public static int getPriorityOrDefault(Integer priority) {
		return Objects.firstNonNull(priority, 0);
	}

	public static ParticipationState getParticipationStateOrDefault(ParticipationState pStat) {
		return Objects.firstNonNull(pStat, ParticipationState.NEEDSACTION);
	}

	public static ParticipationRole getParticipationRoleOrDefault(ParticipationRole pRole) {
		return  Objects.firstNonNull(pRole, ParticipationRole.REQ);
	}

}
