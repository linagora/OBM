/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.tnefconverter.test;

import java.io.InputStream;

import junit.framework.TestCase;
import net.freeutils.tnef.Message;
import net.freeutils.tnef.TNEFInputStream;

import org.obm.push.tnefconverter.ScheduleMeeting.ScheduleMeeting;

public class TnefTest extends TestCase {

	public void testExtract() {
		InputStream in = loadDataFile("excptRecur.tnef");
		assertNotNull(in);
		try {
			TNEFInputStream tnef = new TNEFInputStream(in);
			Message tnefMsg = new Message(tnef);
			ScheduleMeeting ics = new ScheduleMeeting(tnefMsg);
			assertNotNull(ics);
			assertNotNull(ics.getMethod());
			assertNotNull(ics.getUID());
			assertNotNull(ics.getStartDate());
			assertNotNull(ics.getEndDate());
			assertNotNull(ics.getResponseRequested());
			assertNotNull(ics.getDescription());
			assertNotNull(ics.getClazz());
			assertNotNull(ics.getLocation());
			assertNotNull(ics.isAllDay());
			assertNotNull(ics.isRecurring());
			assertNotNull(ics.getOldRecurrenceType());
			assertNotNull(ics.getInterval());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	public void testExtract2() {
		InputStream in = loadDataFile("acpInv.tnef");
		assertNotNull(in);
		try {
			TNEFInputStream tnef = new TNEFInputStream(in);
			Message tnefMsg = new Message(tnef);
			ScheduleMeeting ics = new ScheduleMeeting(tnefMsg);
			assertNotNull(ics);
			assertNotNull(ics.getMethod());
			assertNotNull(ics.getUID());
			assertNotNull(ics.getStartDate());
			assertNotNull(ics.getEndDate());
			assertNotNull(ics.getResponseRequested());
			assertNotNull(ics.getDescription());
			assertNotNull(ics.getClazz());
			assertNotNull(ics.getLocation());
			assertNotNull(ics.isAllDay());
			assertNotNull(ics.isRecurring());
			assertNotNull(ics.getOldRecurrenceType());
			assertNotNull(ics.getInterval());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	protected InputStream loadDataFile(String name) {
		return getClass().getClassLoader().getResourceAsStream(
				"data/tnef/" + name);
	}
	
}
