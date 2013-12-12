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
package fr.aliacom.obm.common.contact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.ContactConfiguration;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.dao.EntityId;
import org.obm.sync.solr.SolrHelper;
import org.obm.utils.ObmHelper;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.calendar.CalendarDao;

@GuiceModule(ContactDaoTest.Env.class)
@RunWith(SlowGuiceRunner.class)
public class ContactDaoTest {

	@Inject
	private ContactDao contactDao;

	@Inject
	private IMocksControl control;

	@Before
	public void setUp() {
		control.reset();
	}

	@Test
	public void testLoadEmailsWhenRegularEmail() throws SQLException {
		Contact contact1 = new Contact();
		Contact contact2 = new Contact();
		Contact contact3 = new Contact();
		Map<EntityId, Contact> contacts = ImmutableMap.of(
				EntityId.valueOf(1), contact1, 
				EntityId.valueOf(2), contact2,
				EntityId.valueOf(3), contact3);

		ResultSet rs = control.createMock(ResultSet.class);
		expect(rs.getInt(1)).andReturn(2);
		expect(rs.getString(2)).andReturn("INTERNET;X-OBM-Ref1");
		expect(rs.getString(3)).andReturn("user@domain");

		control.replay();
		contactDao.loadEmailInContact(contacts, rs);
		control.verify();
		
		assertThat(contact2.getEmails())
			.containsEntry("INTERNET;X-OBM-Ref1", EmailAddress.loginAtDomain("user@domain"));
	}

	@Test
	public void testLoadEmailsWhenAppendRegularEmail() throws SQLException {
		Contact contact1 = new Contact();
		Contact contact2 = new Contact();
		contact2.addEmail("INTERNET;X-OBM-Ref2", EmailAddress.loginAtDomain("user2@domain"));
		Contact contact3 = new Contact();
		Map<EntityId, Contact> contacts = ImmutableMap.of(
				EntityId.valueOf(1), contact1, 
				EntityId.valueOf(2), contact2,
				EntityId.valueOf(3), contact3);

		ResultSet rs = control.createMock(ResultSet.class);
		expect(rs.getInt(1)).andReturn(2);
		expect(rs.getString(2)).andReturn("INTERNET;X-OBM-Ref1");
		expect(rs.getString(3)).andReturn("user@domain");

		control.replay();
		contactDao.loadEmailInContact(contacts, rs);
		control.verify();
		
		assertThat(contact2.getEmails())
			.containsEntry("INTERNET;X-OBM-Ref1", EmailAddress.loginAtDomain("user@domain"))
			.containsEntry("INTERNET;X-OBM-Ref2", EmailAddress.loginAtDomain("user2@domain"));
	}

	@Test
	public void testLoadEmailsWhenOverrideRegularEmail() throws SQLException {
		Contact contact1 = new Contact();
		Contact contact2 = new Contact();
		contact2.addEmail("INTERNET;X-OBM-Ref1", EmailAddress.loginAtDomain("user1@domain"));
		Contact contact3 = new Contact();
		Map<EntityId, Contact> contacts = ImmutableMap.of(
				EntityId.valueOf(1), contact1, 
				EntityId.valueOf(2), contact2,
				EntityId.valueOf(3), contact3);

		ResultSet rs = control.createMock(ResultSet.class);
		expect(rs.getInt(1)).andReturn(2);
		expect(rs.getString(2)).andReturn("INTERNET;X-OBM-Ref1");
		expect(rs.getString(3)).andReturn("user2@domain");

		control.replay();
		contactDao.loadEmailInContact(contacts, rs);
		control.verify();
		
		assertThat(contact2.getEmails())
			.containsEntry("INTERNET;X-OBM-Ref1", EmailAddress.loginAtDomain("user2@domain"));
	}

	@Test
	public void testLoadEmailsWhenNullEmail() throws SQLException {
		Contact contact1 = new Contact();
		Contact contact2 = new Contact();
		Contact contact3 = new Contact();
		Map<EntityId, Contact> contacts = ImmutableMap.of(
				EntityId.valueOf(1), contact1, 
				EntityId.valueOf(2), contact2,
				EntityId.valueOf(3), contact3);

		ResultSet rs = control.createMock(ResultSet.class);
		expect(rs.getInt(1)).andReturn(2);
		expect(rs.getString(2)).andReturn("INTERNET;X-OBM-Ref1");
		expect(rs.getString(3)).andReturn(null);

		control.replay();
		contactDao.loadEmailInContact(contacts, rs);
		control.verify();
		
		assertThat(contact2.getEmails()).isEmpty();
	}

	@Test
	public void testLoadEmailsWhenEmptyEmail() throws SQLException {
		Contact contact1 = new Contact();
		Contact contact2 = new Contact();
		Contact contact3 = new Contact();
		Map<EntityId, Contact> contacts = ImmutableMap.of(
				EntityId.valueOf(1), contact1, 
				EntityId.valueOf(2), contact2,
				EntityId.valueOf(3), contact3);

		ResultSet rs = control.createMock(ResultSet.class);
		expect(rs.getInt(1)).andReturn(2);
		expect(rs.getString(2)).andReturn("INTERNET;X-OBM-Ref1");
		expect(rs.getString(3)).andReturn("");

		control.replay();
		contactDao.loadEmailInContact(contacts, rs);
		control.verify();
		
		assertThat(contact2.getEmails()).isEmpty();
	}

	@Test
	public void testLoadEmailsWhenNotAnEmail() throws SQLException {
		Contact contact1 = new Contact();
		Contact contact2 = new Contact();
		Contact contact3 = new Contact();
		Map<EntityId, Contact> contacts = ImmutableMap.of(
				EntityId.valueOf(1), contact1, 
				EntityId.valueOf(2), contact2,
				EntityId.valueOf(3), contact3);

		ResultSet rs = control.createMock(ResultSet.class);
		expect(rs.getInt(1)).andReturn(2);
		expect(rs.getString(2)).andReturn("INTERNET;X-OBM-Ref1");
		expect(rs.getString(3)).andReturn("loginonly");

		control.replay();
		contactDao.loadEmailInContact(contacts, rs);
		control.verify();
		
		assertThat(contact2.getEmails()).isEmpty();
	}

	public static class Env extends AbstractModule {
		private final IMocksControl control = createControl();

		private <T> T bindMock(Class<T> cls) {
			T mock = control.createMock(cls);

			bind(cls).toInstance(mock);

			return mock;
		}

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(control);
			bindMock(ContactConfiguration.class);
			bindMock(CalendarDao.class);
			bindMock(SolrHelper.Factory.class);
			bindMock(ObmHelper.class);
			bindMock(EventExtId.Factory.class);
		}
	}

}
