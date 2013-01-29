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
package org.obm.sync;

import nl.jqno.equalsverifier.Warning;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.auth.Login;
import org.obm.sync.bean.EqualsVerifierUtils;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.ContactAttendee;
import org.obm.sync.calendar.DeletedEvent;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.calendar.EventRecurrence;
import org.obm.sync.calendar.RecurrenceDays;
import org.obm.sync.calendar.RecurrenceId;
import org.obm.sync.calendar.ResourceAttendee;
import org.obm.sync.calendar.SyncRange;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.items.EventChanges;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.resource.Resource;
import fr.aliacom.obm.common.trust.TrustToken;

@RunWith(SlowFilterRunner.class)
public class BeansTest {

	private EqualsVerifierUtils equalsVerifierUtilsTest;
	
	@Before
	public void init() {
		equalsVerifierUtilsTest = new EqualsVerifierUtils();
	}
	
	@Test
	public void test() {
		equalsVerifierUtilsTest.test(
				ObmDomain.class,
				Event.class,
				DeletedEvent.class,
				EventRecurrence.class,
				EventChanges.class,
				Contact.class,
				TrustToken.class,
				Login.class,
				SyncRange.class,
				EventExtId.class,
				EventObmId.class,
				EventRecurrence.class,
				RecurrenceId.class,
				Resource.class,
				UserAttendee.class, ContactAttendee.class, ResourceAttendee.class);
	}
	
	@Test
	public void testWhereNullableFields() {
		EqualsVerifierUtils
			.createEqualsVerifier(RecurrenceDays.class)
			.suppress(Warning.NULL_FIELDS)
			.verify();
	}
}
