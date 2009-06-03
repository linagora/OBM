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

package org.obm.caldav.obmsync.service.impl;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.obmsync.provider.impl.ObmSyncProvider;
import org.obm.caldav.obmsync.service.IEventService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Event;

public class EventService implements IEventService {

	private ICalendarProvider icp;
	private AccessToken token;
	private String login;

	public EventService(String login, String password) {
		icp = ObmSyncProvider.getInstance();
		token = icp.login(login, password);
		this.login = login;
	}

	@Override
	public Event getEventFromExtId(String externalUID) throws AuthFault,
			ServerFault {
		return icp.getEventFromExtId(token, login, externalUID);
	}

	@Override
	public Event createEvent(Event event) throws AuthFault, ServerFault {
		return icp.createEvent(token, login, event);
	}

	@Override
	public List<Event> getListEventsOfDays(Date day) throws AuthFault,
			ServerFault {

		Calendar cal = new GregorianCalendar();
		cal.setTime(day);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR, 0);
		Date start = cal.getTime();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
		Date end = cal.getTime();

		return icp.getListEventsFromIntervalDate(token, login, start, end);
	}

	@Override
	public void updateParticipationState(Event event, String going)
			throws AuthFault, ServerFault {
		icp.updateParticipationState(token, login, event, going);
	}

	@Override
	public String getParticipationState(Event event) throws AuthFault,
			ServerFault {
		return icp.getParticipationState(token, login, event);
	}

	@Override
	public Event parseIcs(InputStream icsFile) throws Exception {
		/*
		 * ICSParser icsParser = new ICSParser(icsFile); return
		 * icsParser.getEvent();
		 */
		throw new Exception("Unimplemented method");
	}

	@Override
	public String getUserEmail() throws Exception {
		return icp.getUserEmail(token);
	}

	@Override
	public void logout() {
		this.icp.logout(token);
	}

	@Override
	public boolean isConnected() {
		return this.token.getSessionId() != null;
	}

	@Override
	public List<Event> getAllEvents() throws Exception {
		return icp.getAllEvents(token, login);
	}

	@Override
	public Map<String,String> getICSEvents(Set<String> listUidEvent) throws Exception {
		return icp.getICSEvents(token, login, listUidEvent);
	}
}
