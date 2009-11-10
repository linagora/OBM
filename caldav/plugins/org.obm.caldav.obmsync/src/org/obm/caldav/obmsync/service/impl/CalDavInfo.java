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
