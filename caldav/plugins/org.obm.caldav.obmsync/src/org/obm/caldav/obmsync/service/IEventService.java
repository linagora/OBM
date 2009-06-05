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

package org.obm.caldav.obmsync.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.obm.caldav.obmsync.exception.AuthorizationException;
import org.obm.sync.calendar.Event;


public interface IEventService {
	
	void logout();
	
	boolean isConnected();
	
	Event createEvent(Event event) throws Exception;
	Event updateOrCreateEvent(String ics, String extId) throws Exception;
	
	String getICSName(Event event) throws Exception;
	
	Event getEventFromExtId(String externalUrl) throws Exception;
	
	List<Event> getAllEvents() throws Exception;
	Map<String,String> getICSEventsFromExtId(Set<String> listExtIdEvent) throws Exception;
	
	String getUserEmail() throws Exception;

	List<Event> getListEventsOfDays(Date day) throws Exception ;

	void remove(String extId) throws Exception,AuthorizationException ;
}

