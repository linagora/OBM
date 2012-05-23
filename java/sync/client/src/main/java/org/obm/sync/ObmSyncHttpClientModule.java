package org.obm.sync;

import org.obm.sync.client.CalendarType;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.client.calendar.TodoClient;
import org.obm.sync.client.login.LoginClient;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.client.mailingList.MailingListClient;
import org.obm.sync.client.setting.SettingClient;
import org.obm.sync.services.IAddressBook;
import org.obm.sync.services.ICalendar;
import org.obm.sync.services.IMailingList;
import org.obm.sync.services.ISetting;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class ObmSyncHttpClientModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(IAddressBook.class).to(BookClient.class);
		bind(ICalendar.class)
			.annotatedWith(Names.named(CalendarType.CALENDAR))
			.to(CalendarClient.class);
		bind(ICalendar.class)
			.annotatedWith(Names.named(CalendarType.TODO))
			.to(TodoClient.class);
		bind(LoginService.class).to(LoginClient.class);
		bind(IMailingList.class).to(MailingListClient.class);
		bind(ISetting.class).to(SettingClient.class);
	}
	
}
