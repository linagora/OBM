package org.obm.caldav.server;

import org.obm.caldav.server.share.Token;



public interface IProxy {
	
	/*IEventService getEventService();
	ITodoService getTodoService();*/
	
	ICalendarService getCalendarService();
	void login(Token token);

	void logout();
}
