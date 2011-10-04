package org.obm.caldav.server.share;

import java.util.Date;

public class CalendarComponent implements DavComponent {

	private String url;
	private Date lastUpdate;
	
	public CalendarComponent(String url, Date lastUpdate){
		this.url = url;
		this.lastUpdate = lastUpdate;
	}
	
	@Override
	public String getETag() {
		return lastUpdate.toString();
	}

	@Override
	public DavComponentType getType() {
		return DavComponentType.VCALENDAR;
	}

	@Override
	public String getURL() {
		return url;
	}

}
