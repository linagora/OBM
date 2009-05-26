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

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;

/**
 * 
 * @author adrienp
 *
 */
public interface ICalendarProvider {

	CalendarInfo getMyCalendar(AccessToken token, String userId) throws ServerFault, AuthFault;

	Event getEventFromExtId(AccessToken token, String userId, String UID) throws AuthFault,
			ServerFault;

	Event createEvent(AccessToken token, String userId, Event event) throws AuthFault, ServerFault;

	void updateParticipationState(AccessToken token, String userId, Event event,
			String participationState) throws AuthFault, ServerFault;

	List<Event> getListEventsFromIntervalDate(AccessToken token, String userId, Date start,
			Date end) throws AuthFault, ServerFault;

	AccessToken login(String login, String password);
	void logout(AccessToken token);

	String getParticipationState(AccessToken token, String userId, Event event) throws AuthFault,
			ServerFault;
	
	String getUserEmail(AccessToken token) throws AuthFault, ServerFault;
	
	Set<CalendarInfo> getListCalendars(AccessToken token) throws ServerFault, AuthFault ;
	
	Set<String> getAllEvent(AccessToken token, String calendar) throws ServerFault, AuthFault;
}
