/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.opush.env;

import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.obm.guice.AbstractOverrideModule;
import org.obm.opush.TrackableResourceCloser;
import org.obm.push.bean.PIMDataType;
import org.obm.push.calendar.CalendarBackend;
import org.obm.push.contacts.ContactsBackend;
import org.obm.push.resource.ResourceCloser;
import org.obm.push.task.TaskBackend;

public class BackendsModule extends AbstractOverrideModule {

	public BackendsModule(IMocksControl mocksControl) {
		super(mocksControl);
	}

	@Override
	protected void configureImpl() {
		bindCalendarBackend();
		bindContactsBackend();
		bindTaskBackend();
		bind(ResourceCloser.class).toProvider(TrackableResourceCloser.Provider.class);
	}
	
	protected void bindCalendarBackend() {
		bindWithMock(CalendarBackend.class);
		CalendarBackend calendarBackend = getMock(CalendarBackend.class);
		expect(calendarBackend.getPIMDataType()).andReturn(PIMDataType.CALENDAR);
	}
	
	protected void bindContactsBackend() {
		bindWithMock(ContactsBackend.class);
		ContactsBackend contactBackend = getMock(ContactsBackend.class);
		expect(contactBackend.getPIMDataType()).andReturn(PIMDataType.CONTACTS);
	}
	
	protected void bindTaskBackend() {
		bindWithMock(TaskBackend.class);
		TaskBackend taskBackend = getMock(TaskBackend.class);
		expect(taskBackend.getPIMDataType()).andReturn(PIMDataType.TASKS);
	}
}