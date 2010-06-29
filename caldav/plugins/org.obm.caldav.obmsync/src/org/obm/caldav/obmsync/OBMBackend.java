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
 *   obm.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.obmsync;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.obmsync.service.impl.CalendarService;
import org.obm.caldav.server.ICalendarService;
import org.obm.caldav.server.IBackend;
import org.obm.caldav.server.exception.AuthenticationException;
import org.obm.caldav.server.share.CalDavRigth;
import org.obm.caldav.server.share.CalDavToken;

public class OBMBackend implements IBackend {

	@SuppressWarnings("unused")
	private Log logger = LogFactory.getLog(getClass());
	private ICalendarService calendarService;

	public OBMBackend(CalDavToken davToken) throws Exception {
		initService();
		boolean isValid = validateToken(davToken);
		if(!isValid){
			throw new AuthenticationException();
		}
	}

	private void initService() {
		calendarService = new CalendarService();
	}

	@Override
	public ICalendarService getCalendarService() {
		if (this.calendarService == null) {
			throw new RuntimeException("You must be logged");
		}
		return calendarService;
	}

	public Boolean validateToken(CalDavToken t) throws Exception {
		if (t == null) {
			return false;
		}
		CalDavRigth rigth = calendarService.hasRightsOnCalendar(t);
		return rigth.isReadable();
		
	}

	@Override
	public String getETag(CalDavToken t) throws Exception {
		return ""+calendarService.getLastUpdate(t).getTime();
	}
}
