package org.obm.caldav.server;

import org.obm.caldav.obmsync.service.IEventService;


public interface IProxy {
	
	IEventService getEventService();
	

	boolean isConnected();
}
