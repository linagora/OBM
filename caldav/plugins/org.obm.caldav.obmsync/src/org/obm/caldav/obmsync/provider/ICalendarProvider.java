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

import org.obm.caldav.obmsync.service.impl.CalDavInfo;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.calendar.FreeBusy;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.items.EventChanges;

/**
 * 
 * @author adrienp
 * 
 */
public interface ICalendarProvider {

	Event getEventFromExtId(CalDavInfo caldavInfo, String extId)
			throws AuthFault, ServerFault;

	Event createEvent(CalDavInfo caldavInfo, Event event)
			throws AuthFault, ServerFault;

	List<Event> getAll(CalDavInfo caldavInfo) throws ServerFault,
			AuthFault;

	List<EventTimeUpdate> getAllEventTimeUpdate(CalDavInfo caldavInfo) throws ServerFault, AuthFault;

	Map<Event, String> getICSEventsFromExtId(CalDavInfo caldavInfo,
			Set<String> listUidEvent) throws AuthFault, ServerFault;

	void remove(CalDavInfo caldavInfo, Event event)
			throws ServerFault, AuthFault;

	void updateParticipationState(CalDavInfo caldavInfo,
			Event event, String participationState) throws AuthFault,
			ServerFault;

	List<Event> createEventsFromICS(CalDavInfo caldavInfo,
			String ics, String extId) throws Exception;

	List<Event> updateEventFromICS(CalDavInfo caldavInfo, String ics,
			Event event) throws Exception;

	EventChanges getSync(CalDavInfo caldavInfo, Date lastSync)
			throws AuthFault, ServerFault;

	List<Event> getListEventsFromIntervalDate(CalDavInfo caldavInfo, Date start, Date end) throws AuthFault,
			ServerFault;

	List<EventTimeUpdate> getEventTimeUpdateFromIntervalDate(CalDavInfo caldavInfo, Date start, Date end) throws ServerFault,
			AuthFault;

	CalendarInfo getRightsOnCalendar(CalDavInfo caldavInfo)
			throws AuthFault, ServerFault;

	Date getLastUpdate(CalDavInfo caldavInfo)
			throws AuthFault, ServerFault;

	AccessToken login(String loginAtDomaine, String password);

	void logout(AccessToken token);

	public List<FreeBusy> getFreeBusy(CalDavInfo caldavInfo, FreeBusyRequest fbrs) throws ServerFault,
			AuthFault;

	FreeBusyRequest getFreeBusyRequest(CalDavInfo caldavInfo, String ics)
			throws ServerFault, AuthFault;

	String parseFreeBusyToICS(CalDavInfo caldavInfo, FreeBusy freeBusy) throws ServerFault, AuthFault;
}
