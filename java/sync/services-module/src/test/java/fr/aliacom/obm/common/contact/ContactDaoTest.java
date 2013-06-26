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
package fr.aliacom.obm.common.contact;

import static org.easymock.EasyMock.*;
import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.fest.assertions.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.ContactConfiguration;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.solr.SolrHelper.Factory;
import org.obm.utils.ObmHelper;

import com.google.common.collect.ImmutableMap;

import fr.aliacom.obm.common.calendar.CalendarDao;

@RunWith(SlowFilterRunner.class)
public class ContactDaoTest {

	private ContactDao contactDao;

	@Before
	public void setUp() {
		ContactConfiguration contactConfiguration = null;
		CalendarDao calendarDao = null;
		Factory solrHelperFactory = null;
		ObmHelper obmHelper = null;
		EventExtId.Factory eventExtIdFactory = null;
		contactDao = new ContactDao(contactConfiguration, calendarDao,
				solrHelperFactory, obmHelper, eventExtIdFactory);
	}

	@Test
	public void testLoadEmailsWhenRegularEmail() throws SQLException {
		Contact contact1 = new Contact();
		Contact contact2 = new Contact();
		Contact contact3 = new Contact();
		Map<Integer, Contact> contacts = ImmutableMap.of(
				1, contact1, 
				2, contact2,
				3, contact3);

		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getInt(1)).andReturn(2);
		expect(rs.getString(2)).andReturn("INTERNET;X-OBM-Ref1");
		expect(rs.getString(3)).andReturn("user@domain");

		replay(rs);
		contactDao.loadEmailInContact(contacts, rs);
		verify(rs);
		
		assertThat(contact2.getEmails()).contains(
				MapEntry.entry("INTERNET;X-OBM-Ref1", EmailAddress.loginAtDomain("user@domain")));
	}

	@Test
	public void testLoadEmailsWhenAppendRegularEmail() throws SQLException {
		Contact contact1 = new Contact();
		Contact contact2 = new Contact();
		contact2.addEmail("INTERNET;X-OBM-Ref2", EmailAddress.loginAtDomain("user2@domain"));
		Contact contact3 = new Contact();
		Map<Integer, Contact> contacts = ImmutableMap.of(
				1, contact1, 
				2, contact2,
				3, contact3);

		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getInt(1)).andReturn(2);
		expect(rs.getString(2)).andReturn("INTERNET;X-OBM-Ref1");
		expect(rs.getString(3)).andReturn("user@domain");

		replay(rs);
		contactDao.loadEmailInContact(contacts, rs);
		verify(rs);
		
		assertThat(contact2.getEmails()).contains(
				MapEntry.entry("INTERNET;X-OBM-Ref1", EmailAddress.loginAtDomain("user@domain")),
				MapEntry.entry("INTERNET;X-OBM-Ref2", EmailAddress.loginAtDomain("user2@domain")));
	}

	@Test
	public void testLoadEmailsWhenOverrideRegularEmail() throws SQLException {
		Contact contact1 = new Contact();
		Contact contact2 = new Contact();
		contact2.addEmail("INTERNET;X-OBM-Ref1", EmailAddress.loginAtDomain("user1@domain"));
		Contact contact3 = new Contact();
		Map<Integer, Contact> contacts = ImmutableMap.of(
				1, contact1, 
				2, contact2,
				3, contact3);

		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getInt(1)).andReturn(2);
		expect(rs.getString(2)).andReturn("INTERNET;X-OBM-Ref1");
		expect(rs.getString(3)).andReturn("user2@domain");

		replay(rs);
		contactDao.loadEmailInContact(contacts, rs);
		verify(rs);
		
		assertThat(contact2.getEmails()).contains(
				MapEntry.entry("INTERNET;X-OBM-Ref1", EmailAddress.loginAtDomain("user2@domain")));
	}

	@Test
	public void testLoadEmailsWhenNullEmail() throws SQLException {
		Contact contact1 = new Contact();
		Contact contact2 = new Contact();
		Contact contact3 = new Contact();
		Map<Integer, Contact> contacts = ImmutableMap.of(
				1, contact1, 
				2, contact2,
				3, contact3);

		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getInt(1)).andReturn(2);
		expect(rs.getString(2)).andReturn("INTERNET;X-OBM-Ref1");
		expect(rs.getString(3)).andReturn(null);

		replay(rs);
		contactDao.loadEmailInContact(contacts, rs);
		verify(rs);
		
		assertThat(contact2.getEmails()).isEmpty();
	}

	@Test
	public void testLoadEmailsWhenEmptyEmail() throws SQLException {
		Contact contact1 = new Contact();
		Contact contact2 = new Contact();
		Contact contact3 = new Contact();
		Map<Integer, Contact> contacts = ImmutableMap.of(
				1, contact1, 
				2, contact2,
				3, contact3);

		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getInt(1)).andReturn(2);
		expect(rs.getString(2)).andReturn("INTERNET;X-OBM-Ref1");
		expect(rs.getString(3)).andReturn("");

		replay(rs);
		contactDao.loadEmailInContact(contacts, rs);
		verify(rs);
		
		assertThat(contact2.getEmails()).isEmpty();
	}

	@Test
	public void testLoadEmailsWhenNotAnEmail() throws SQLException {
		Contact contact1 = new Contact();
		Contact contact2 = new Contact();
		Contact contact3 = new Contact();
		Map<Integer, Contact> contacts = ImmutableMap.of(
				1, contact1, 
				2, contact2,
				3, contact3);

		ResultSet rs = createMock(ResultSet.class);
		expect(rs.getInt(1)).andReturn(2);
		expect(rs.getString(2)).andReturn("INTERNET;X-OBM-Ref1");
		expect(rs.getString(3)).andReturn("loginonly");

		replay(rs);
		contactDao.loadEmailInContact(contacts, rs);
		verify(rs);
		
		assertThat(contact2.getEmails()).isEmpty();
	}

}
