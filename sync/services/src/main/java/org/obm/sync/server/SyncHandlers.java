package org.obm.sync.server;

import java.util.Map;

import org.obm.sync.server.handler.AddressBookHandler;
import org.obm.sync.server.handler.CalendarHandler;
import org.obm.sync.server.handler.ISyncHandler;
import org.obm.sync.server.handler.LoginHandler;
import org.obm.sync.server.handler.SettingHandler;
import org.obm.sync.server.handler.TodoHandler;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SyncHandlers {

	private Map<String, ISyncHandler> handlers;

	@Inject
	private SyncHandlers(CalendarHandler calendarHandler, 
			TodoHandler todoHandler, 
			AddressBookHandler addressBookHandler,
			SettingHandler settingHandler,
			LoginHandler loginHandler,
			MailingListHandler mailingListHandler) {
		handlers = new ImmutableMap.Builder<String, ISyncHandler>().
		put("login", loginHandler).
		put("calendar", calendarHandler).
		put("todo", todoHandler).
		put("book", addressBookHandler).
		put("setting", settingHandler).
		put("mailingList", mailingListHandler).build();
	}

	public Map<String, ISyncHandler> getHandlers() {
		return handlers;
	}
}