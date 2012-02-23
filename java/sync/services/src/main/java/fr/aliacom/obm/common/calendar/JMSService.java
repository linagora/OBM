package fr.aliacom.obm.common.calendar;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Event;

public interface JMSService {

	void writeIcsInvitationRequest(AccessToken token, Event current);

	void writeIcsInvitationCancel(AccessToken token, Event event);

	void writeIcsInvitationReply(AccessToken token, Event event);

}
