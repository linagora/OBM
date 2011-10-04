package org.obm.caldav.server.share;

public interface DavComponent {
	
	String getURL();
	String getETag();
	DavComponentType getType();
}
