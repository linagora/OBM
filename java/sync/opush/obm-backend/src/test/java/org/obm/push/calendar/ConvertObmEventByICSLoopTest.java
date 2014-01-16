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
package org.obm.push.calendar;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

import java.util.Date;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obm.icalendar.Ical4jHelper;
import org.obm.push.exception.ConversionException;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventExtId.Factory;
import org.obm.sync.calendar.SimpleAttendeeService;
import org.obm.sync.date.DateProvider;
import org.obm.sync.services.AttendeeService;


public class ConvertObmEventByICSLoopTest extends ConvertObmEventToMsEventIntegrityTest {
	
	private Ical4jHelper ical4jHelper;
	private DateProvider dateProvider;
	private AttendeeService attendeeService;
	private Date now;

	@Before
	public void setUp() {
		now = new Date();
		dateProvider = createMock(DateProvider.class);
		attendeeService = new SimpleAttendeeService();
		Factory eventExtIdFactory = createMock(EventExtId.Factory.class);
		ical4jHelper = new Ical4jHelper(dateProvider, eventExtIdFactory, attendeeService);
		
		expect(dateProvider.getDate()).andReturn(now).anyTimes();		
		replay(dateProvider);
		
		super.setUp();
	}
	
	@Override
	protected ObmEventToMSEventConverter newObmEventToMSEventConverter() {
		return new ObmEventToMSEventByICSLoopConverter(ical4jHelper);
	}

	@Ignore("OBMFULL-4295")
	@Test
	@Override
	public void testTimeZoneConversion() throws ConversionException {
		super.testTimeZoneConversion();
	}
}
