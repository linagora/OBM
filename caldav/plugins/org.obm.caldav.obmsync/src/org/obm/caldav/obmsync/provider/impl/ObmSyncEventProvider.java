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

package org.obm.caldav.obmsync.provider.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream.GetField;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.security.auth.login.FailedLoginException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.obmsync.ObmSyncConfIni;
import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.server.AuthorizationException;
import org.obm.caldav.utils.CalDavUtils;
import org.obm.caldav.utils.FileUtils;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.ParticipationRole;
import org.obm.sync.calendar.ParticipationState;
import org.obm.sync.client.calendar.AbstractEventSyncClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.locators.CalendarLocator;

@SuppressWarnings("unused")
public class ObmSyncEventProvider extends AbstractObmSyncProvider implements ICalendarProvider {


	private static AbstractObmSyncProvider instance;
	
	public static AbstractObmSyncProvider getInstance(){
		if(instance == null){
			instance = new ObmSyncEventProvider();
		}
		return instance;
	}
	
	
	protected ObmSyncEventProvider() {
		super(); 
	}
	
	@Override
	protected AbstractEventSyncClient initObmSyncProvider(String url) {
		return new CalendarClient(url);
	}

}
