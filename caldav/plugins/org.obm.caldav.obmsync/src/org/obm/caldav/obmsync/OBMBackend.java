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
import org.obm.caldav.server.share.Token;

public class OBMBackend implements IBackend {

//	private AccessToken token;
//	private String userId;
//	private String calendar;
	private ICalendarService calendarService;
	private Log logger = LogFactory.getLog(getClass());

	public OBMBackend(Token davToken) throws Exception {
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
	public void login(Token davToken) throws AuthenticationException {
		calendarService.login(davToken.getLoginAtDomain(), davToken.getPassword(),davToken.getCalendarName());
	}

	@Override
	public void logout() {
		try {
			calendarService.logout();
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	@Override
	public ICalendarService getCalendarService() {
		if (this.calendarService == null) {
			throw new RuntimeException("You must be logged");
		}
		return calendarService;
	}

	private boolean validateToken(Token t) throws Exception {
		if (t == null) {
			return false;
		}
		this.login(t);
		
		boolean hasRightsOnCalendar = calendarService.hasRightsOnCalendar();
		this.logout();
		return hasRightsOnCalendar;
	}

	@Override
	public String getETag() throws Exception {
		return calendarService.getLastUpdate();
	}
}
