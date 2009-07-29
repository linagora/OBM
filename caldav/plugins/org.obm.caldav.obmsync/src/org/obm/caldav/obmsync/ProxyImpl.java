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

import org.obm.caldav.obmsync.provider.impl.AbstractObmSyncProvider;
import org.obm.caldav.obmsync.service.impl.CalendarService;
import org.obm.caldav.server.ICalendarService;
import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.share.Token;
import org.obm.sync.auth.AccessToken;

public class ProxyImpl implements IProxy {

	private AccessToken token;
	private String userId;
	private String calendar;
	private ICalendarService calendarService;
	
	
	public ProxyImpl(){
	}
	
	private void initService() {
		calendarService = new CalendarService(token,calendar,userId);
	}
	
	@Override
	public void login(Token token) {
		this.userId = token.getLoginAtDomain();
		this.calendar = token.getLogin();
		this.token = AbstractObmSyncProvider.login(userId, token.getPassword());
		this.initService();
	}

	@Override
	public void logout() {
		AbstractObmSyncProvider.logout(token);
	}

	@Override
	public ICalendarService getCalendarService() {
		if(this.calendarService == null){
			throw new RuntimeException("You must be logged");
		}
		return calendarService;
	}

	@Override
	public boolean validateToken(Token token) {
		if(token == null){
			return false;
		}
		AccessToken at = AbstractObmSyncProvider.login(token.getLoginAtDomain(), token.getPassword());
		if(at == null || at.getSessionId() == null || "".equals(at.getSessionId())){
			return false;
		} else {
			AbstractObmSyncProvider.logout(at);
			return true;
		}
	}

}
