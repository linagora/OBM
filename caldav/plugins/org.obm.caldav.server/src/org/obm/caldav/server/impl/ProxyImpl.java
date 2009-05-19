package org.obm.caldav.server.impl;

import org.obm.caldav.obmsync.service.IEventService;
import org.obm.caldav.obmsync.service.impl.EventService;
import org.obm.caldav.server.IProxy;
import org.obm.caldav.server.share.Token;

public class ProxyImpl implements IProxy {

	private Token token;
	
	private IEventService eventService;
	
	public ProxyImpl(Token token){
		this.token = token;
		initService();
	}
	
	
	private void initService() {
		eventService = new EventService(token.getLoginAtDomain(),token.getPassword());
	}

	@Override
	public IEventService getEventService() {
		return this.eventService;
	}

	@Override
	public boolean isConnected() {
		return eventService.isConnected();
	}

}
