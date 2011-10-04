package org.obm.caldav.server.share;

import java.util.Date;

public class CalendarResourceICS extends CalendarResource{

	private String ics;
	
	public CalendarResourceICS(String extId, String parentUrl, Date lastUpdate, DavComponentType type, String ics){
		super(extId, parentUrl, lastUpdate, type);
		this.ics = ics;
	}

	public String getIcs() {
		return ics;
	}

	public void setIcs(String ics) {
		this.ics = ics;
	}
	
}
