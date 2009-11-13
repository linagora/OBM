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

package org.obm.caldav.obmsync.service.impl;

import org.obm.sync.auth.AccessToken;

public class CalDavInfo {
	private AccessToken token;
	private String calendar;
	private String calendarAtDomain;
	private String loginAtDomain;
	
	public CalDavInfo(AccessToken token, String calendar, String calendarAtDomain, String loginAtDomain) {
		this.token = token;
		this.calendar = calendar;
		this.calendarAtDomain = calendarAtDomain;
		this.loginAtDomain = loginAtDomain;
	}
	
	public AccessToken getToken() {
		return token;
	}

	public String getCalendar() {
		return calendar;
	}

	public String getLoginAtDomain() {
		return loginAtDomain;
	}
	
	public String getCalendarAtDomain() {
		return calendarAtDomain;
	}
}
