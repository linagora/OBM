package org.obm.caldav.obmsync;

import org.obm.caldav.obmsync.provider.impl.AbstractObmSyncProvider;
import org.obm.caldav.obmsync.service.impl.CalendarService;
import org.obm.caldav.server.ICalendarService;
import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.share.Token;
import org.obm.sync.auth.AccessToken;

public class ProxyImpl implements IProxy {

	private  AccessToken token;
	private String calendarName;
	private ICalendarService calendarService;
	
	
	public ProxyImpl(){
	}
	
	private void initService() {
		calendarService = new CalendarService(token,calendarName);
		//todoService = new TodoService(token, calendarName);
	}

	/*@Override
	public IEventService getEventService() {
		if(this.eventService == null){
			throw new RuntimeException("You must be logged");
		}
		return this.eventService;
	}
	
	@Override
	public ITodoService getTodoService() {
		if(this.eventService == null){
			throw new RuntimeException("You must be logged");
		}
		return todoService;
	}*/
	
	
	
	
	@Override
	public void login(Token token) {
		this.calendarName = token.getLoginAtDomain();
		this.token = AbstractObmSyncProvider.login(token.getLoginAtDomain(), token.getPassword());
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

}
