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
import org.obm.caldav.server.share.CalDavRigth;
import org.obm.caldav.server.share.CalDavToken;
import org.obm.caldav.server.share.CalendarResourceICS;
import org.obm.caldav.server.share.DavComponent;
import org.obm.caldav.server.share.DavComponentType;
import org.obm.caldav.server.share.filter.CompFilter;

public interface ICalendarService {

	final static String PARTICIPATION_STATE_ACCEPTED = "yes";
	final static String PARTICIPATION_STATE_DECLINED = "no";
	final static String PARTICIPATION_STATE_NEEDSACTION = "maybe";

	DavComponent updateOrCreateEvent(CalDavToken token, String compUrl,
			String ics, String extId) throws Exception;

	List<CalendarResourceICS> getICSFromExtId(CalDavToken token,
			String compUrl, Set<String> listExtIdEvent) throws Exception;

	void remove(CalDavToken token, String extId) throws Exception,
			AuthorizationException;

	List<DavComponent> getAllLastUpdate(CalDavToken token, CompFilter cf)
			throws Exception;

	List<DavComponent> getAllLastUpdate(CalDavToken token, String componentUrl,
			DavComponentType componant) throws Exception;

	boolean getSync(CalDavToken token, Date lastSync) throws Exception;

	Date getLastUpdate(CalDavToken token) throws Exception;

	Map<String, String> getFreeBusy(CalDavToken token, String ics)
			throws Exception;

	CalDavRigth hasRightsOnCalendar(CalDavToken token) throws Exception;

}
