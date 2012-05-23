package org.obm.opush.env;

import static org.easymock.EasyMock.expect;

import org.obm.push.bean.PIMDataType;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.task.TaskBackend;
import org.obm.sync.client.CalendarType;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.services.IAddressBook;
import org.obm.sync.services.ICalendar;
import org.obm.sync.services.IMailingList;
import org.obm.sync.services.ISetting;

import com.google.inject.name.Names;

public final class ObmSyncModule extends AbstractOverrideModule {

	public ObmSyncModule() {
		super();
	}

	@Override
	protected void configureImpl() {
		bindWithMock(IAddressBook.class);
		ICalendar calendar = createAndRegisterMock(ICalendar.class);
		bind(ICalendar.class)
			.annotatedWith(Names.named(CalendarType.CALENDAR))
			.toInstance(calendar);
		bind(ICalendar.class)
			.annotatedWith(Names.named(CalendarType.TODO))
			.toInstance(calendar);
		bindWithMock(LoginService.class);
		bindWithMock(IMailingList.class);
		bindWithMock(ISetting.class);
		bindWithMock(CalendarBackend.class);
		bindWithMock(ContactsBackend.class);
		bindWithMock(TaskBackend.class);
		CalendarBackend calendarBackend = getMock(CalendarBackend.class);
		expect(calendarBackend.getPIMDataType()).andReturn(PIMDataType.CALENDAR);
		ContactsBackend contactBackend = getMock(ContactsBackend.class);
		expect(contactBackend.getPIMDataType()).andReturn(PIMDataType.CONTACTS);
		TaskBackend taskBackend = getMock(TaskBackend.class);
		expect(taskBackend.getPIMDataType()).andReturn(PIMDataType.TASKS);
	}
}