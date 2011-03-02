package fr.aliacom.obm.utils;

import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;

import com.google.common.base.Objects;

/**
 * RFC2245 standard
 * 
 * this functions returns default value of RFC2245
 */
public class RFC2245 {

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
