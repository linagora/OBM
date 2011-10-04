package org.obm.caldav.server.share;

import java.util.Date;

public class CalendarResource implements DavComponent{

	private String extId;
	private String parentUrl;
	private Date lastUpdate;
	private DavComponentType type;
	
	public CalendarResource(String extId, String parentUrl, Date lastUpdate, DavComponentType type){
		this.extId = extId;
		if(!parentUrl.endsWith("/")){
			parentUrl += "/";
		}
		this.parentUrl = parentUrl;
		this.lastUpdate = lastUpdate;
		this.type = type;
		
	}
	
	@Override
	public String getETag() {
		return "\"" + extId + "-" + lastUpdate.getTime() + "\"";
	}

	@Override
	public String getURL() {
		return parentUrl+extId+".ics";
	}

	@Override
	public DavComponentType getType() {
		return type;
	}

}
