/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.obmsync.provider;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.caldav.server.exception.AuthorizationException;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.items.EventChanges;

/**
 * 
 * @author adrienp
 * 
 */
public interface ICalendarProvider {

	CalendarInfo getMyCalendar(AccessToken token, String userId)
			throws ServerFault, AuthFault;

	Event getEventFromExtId(AccessToken token, String userId, String extId)
			throws AuthFault, ServerFault;

	Event createEvent(AccessToken token, String userId, Event event)
			throws AuthFault, ServerFault;

	String getUserEmail(AccessToken token) throws AuthFault, ServerFault;

	Set<CalendarInfo> getListCalendars(AccessToken token) throws ServerFault,
			AuthFault;

	List<Event> getAll(AccessToken token, String calendar) throws ServerFault,
			AuthFault;

	List<EventTimeUpdate> getAllEventTimeUpdate(AccessToken token,
			String calendar) throws ServerFault, AuthFault;

	Map<Event, String> getICSEventsFromExtId(AccessToken token, String userId,
			Set<String> listUidEvent) throws AuthFault, ServerFault;

	void remove(AccessToken token, String login, Event event)
			throws ServerFault, AuthFault, AuthorizationException;

	void updateParticipationState(AccessToken token, String userId,
			Event event, String participationState) throws AuthFault,
			ServerFault;

	List<Event> createEventsFromICS(AccessToken token, String login,
			String ics, String extId) throws Exception;

	List<Event> updateEventFromICS(AccessToken token, String login, String ics,
			Event event) throws Exception;

	EventChanges getSync(AccessToken token, String calendar, Date lastSync)
			throws AuthFault, ServerFault;

	List<Event> getListEventsFromIntervalDate(AccessToken token,
			String calendar, Date start, Date end) throws AuthFault,
			ServerFault;
}
