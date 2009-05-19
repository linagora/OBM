package org.obm.caldav.obmsync.service.impl;

import java.util.Set;

import org.obm.caldav.obmsync.provider.ICalendarProvider;
import org.obm.caldav.obmsync.provider.impl.ObmSyncProvider;
import org.obm.caldav.obmsync.service.ICalendarService;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.CalendarInfo;
import org.obm.sync.calendar.Event;


public class CalendarService implements ICalendarService{

	private ICalendarProvider icp;
	private AccessToken token;
	private String login;
	
	public CalendarService(String login, String password) {
		icp = ObmSyncProvider.getInstance();
		token = icp.login(login, password);
		this.login = login;
	}
	
	@Override
	public Set<CalendarInfo> getListCalendars(Event calendar) throws Exception {
		return icp.getListCalendars(token);
	}
	
}
