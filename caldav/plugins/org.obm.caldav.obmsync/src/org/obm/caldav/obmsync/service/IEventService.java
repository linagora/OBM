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

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.obm.sync.calendar.Event;


public interface IEventService {
	
	final static String PARTICIPATION_STATE_ACCEPTED = "yes";
	final static String PARTICIPATION_STATE_DECLINED = "no";
	final static String PARTICIPATION_STATE_NEEDSACTION = "maybe";
	
	void logout();
	
	boolean isConnected();
	Event createEvent(Event calendar) throws Exception;
	Event parseIcs(InputStream icsFile) throws Exception;
	Event getEventFromExtId(String externalUrl) throws Exception;
	List<Event> getListEventsOfDays(Date day) throws Exception ;
	String getParticipationState(Event event) throws Exception ;
	void updateParticipationState(Event event, String going) throws Exception ;
	String getUserEmail() throws Exception;
	Set<String> getAllEvent(String calendar) throws Exception;
}

