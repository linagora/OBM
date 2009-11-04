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

package org.obm.caldav.server;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.caldav.server.exception.AuthenticationException;
import org.obm.caldav.server.exception.AuthorizationException;
import org.obm.caldav.server.share.DavComponentName;
import org.obm.caldav.server.share.filter.CompFilter;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;

public interface ICalendarService {

	final static String PARTICIPATION_STATE_ACCEPTED = "yes";
	final static String PARTICIPATION_STATE_DECLINED = "no";
	final static String PARTICIPATION_STATE_NEEDSACTION = "maybe";

	List<Event> updateOrCreateEvent(String ics, String extId) throws Exception;

	String getICSName(Event event);

	String getICSName(EventTimeUpdate etu);

	List<Event> getAll(DavComponentName componant) throws Exception;

	List<EventTimeUpdate> getAllLastUpdate(DavComponentName componant)
	throws Exception;
	
	List<EventTimeUpdate> getAllLastUpdate(CompFilter cf)
			throws Exception;

	Map<Event, String> getICSFromExtId(Set<String> listExtIdEvent)
			throws Exception;

	void removeOrUpdateParticipationState(String extId) throws Exception,
			AuthorizationException;

	boolean getSync(Date lastSync) throws Exception;

	boolean hasRightsOnCalendar() throws Exception;

	String getLastUpdate() throws Exception;

	void login(String userId, String password, String calendar) throws AuthenticationException;

	void logout();

	public Map<String,String> getFreeBuzy(String ics) throws Exception;

}
