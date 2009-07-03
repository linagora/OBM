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

import org.obm.caldav.server.exception.AuthorizationException;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventTimeUpdate;
import org.obm.sync.items.EventChanges;


public interface ICalendarService {
	
	final static String PARTICIPATION_STATE_ACCEPTED = "yes";
	final static String PARTICIPATION_STATE_DECLINED = "no";
	final static String PARTICIPATION_STATE_NEEDSACTION = "maybe";
	
	Event createEvent(Event event) throws Exception;
	List<Event> updateOrCreateEvent(String ics, String extId) throws Exception;
	
	String getICSName(Event event) ;
	String getICSName(EventTimeUpdate etu) ;
	
	Event getEventFromExtId(String externalUrl) throws Exception;
	
	List<Event> getAllEvents() throws Exception;
	List<EventTimeUpdate>getAllLastUpdateEvents() throws Exception;
	
	List<Event> getAllTodos()  throws Exception;
	List<EventTimeUpdate> getAllLastUpdateTodos() throws Exception;
	
	Map<Event,String> getICSFromExtId(Set<String> listExtIdEvent) throws Exception;

	void removeOrUpdateParticipationState(String extId) throws Exception,AuthorizationException ;
	
	boolean getSync(Date lastSync) throws Exception;
	
	
}

