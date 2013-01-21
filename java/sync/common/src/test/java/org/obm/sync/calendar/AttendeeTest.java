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
package org.obm.sync.calendar;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class AttendeeTest {

	@Test
	public void testEqualAttendee() {
		Attendee att1 = UserAttendee.builder().email("test@test.tlse.lng").build();
		Attendee att2 = UserAttendee.builder().email("test@test.tlse.lng").build();
		
		Assert.assertTrue(att1.equals(att2));
	}
	
	@Test
	public void testNotEqual() {
		Attendee att1 = UserAttendee.builder().email("test1@test.tlse.lng").build();
		Attendee att2 = UserAttendee.builder().email("test2@test.tlse.lng").build();
		
		Assert.assertFalse(att1.equals(att2));
	}
	
	
	@Test
	public void testNotEqualNullEmail() {
		Attendee att1 = UserAttendee.builder().build();
		Attendee att2 = UserAttendee.builder().email("test@test.tlse.lng").build();
		
		Assert.assertFalse(att1.equals(att2));
		Assert.assertFalse(att2.equals(att1));
	}
	
	@Test
	public void testEqualCase() {
		Attendee att1 = UserAttendee.builder().email("test@test.tlse.lng").build();
		Attendee att2 = UserAttendee.builder().email("TeSt@tEsT.TlsE.lNg").build();
		
		Assert.assertTrue(att1.equals(att2));
		Assert.assertTrue(att2.equals(att1));
	}

	@Test
	public void testHashCodeCase() {
		Attendee att1 = UserAttendee.builder().email("test@test.tlse.lng").build();
		Attendee att2 = UserAttendee.builder().email("TeSt@tEsT.TlsE.lNg").build();
		
		Assert.assertEquals(att1.hashCode(), att2.hashCode());
	}
	
	@Test
	public void testHashCode(){
		Attendee att1 = UserAttendee
				.builder()
				.displayName("test")
				.email("test@obm.lng.org")
				.percent(0)
				.participation(Participation.accepted()).build();
		
		Attendee att2 = UserAttendee
				.builder()
				.displayName("test2")
				.email("test@obm.lng.org")
				.asOrganizer()
				.percent(1)
				.participation(Participation.needsAction()).build();
		
		Assert.assertEquals(att1.hashCode(), att2.hashCode());
	}
	
}
