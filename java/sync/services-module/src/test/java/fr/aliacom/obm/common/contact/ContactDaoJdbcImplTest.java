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
import static org.easymock.EasyMock.expectLastCall;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.ContactConfiguration;
import org.obm.domain.dao.CalendarDao;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.service.solr.SolrHelper;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.dao.EntityId;
import org.obm.utils.ObmHelper;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

@GuiceModule(ContactDaoJdbcImplTest.Env.class)
@RunWith(GuiceRunner.class)
public class ContactDaoJdbcImplTest {

	@Inject
	private ContactDaoJdbcImpl contactDao;
	@Inject
	private ObmHelper mockObmHelper;

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

	@Test
	public void testLoadContactsFromDB() throws SQLException {
		String query =
				"SELECT contact_id, contact_firstname, contact_lastname, "+
						"contactentity_entity_id, contact_aka, contact_company, contact_title, "+
						"contact_service, contact_birthday_id, contact_anniversary_id, contact_middlename, " +
						"contact_suffix, contact_manager, contact_assistant, contact_spouse, " +
						"contact_addressbook_id, contact_comment, contact_commonname, now() as last_sync "+
				"FROM Contact, ContactEntity "+
				"WHERE contactentity_contact_id=contact_id AND "+
					"contact_archive != 1 AND "+
					"contact_id IN (?, ?) "+
				"ORDER BY contact_lastname";

		int limit = 3;
		Set<Integer> evtIds = Sets.newHashSet(1, 2);

		Connection mockConn = control.createMock(Connection.class);
		PreparedStatement mockStatement = control.createMock(PreparedStatement.class);
		ResultSet mockRS = control.createMock(ResultSet.class);
		expect(mockConn.prepareStatement(query)).andReturn(mockStatement).once();
		mockStatement.setInt(1, 1);
		expectLastCall();
		mockStatement.setInt(2, 2);
		expectLastCall();
		expect(mockStatement.executeQuery()).andReturn(mockRS).once();

		expect(mockRS.next()).andReturn(true).once();
		expect(mockRS.getInt("contactentity_entity_id")).andReturn(10).once();
		expect(mockRS.getInt(1)).andReturn(1).once();
		expect(mockRS.getString(2)).andReturn("John").once();
		expect(mockRS.getString(3)).andReturn("Doe").once();
		expect(mockRS.getInt(4)).andReturn(10).once();
		expect(mockRS.getString(5)).andReturn("aka john.doe").once();
		expect(mockRS.getString(6)).andReturn("John Doe Inc.").once();
		expect(mockRS.getString(7)).andReturn("Like a boss").once();
		expect(mockRS.getString(8)).andReturn("8th directorate").once();
		expect(mockRS.getInt(9)).andReturn(100).once();
		expect(mockRS.wasNull()).andReturn(false);
		expect(mockRS.getInt(10)).andReturn(200).once();
		expect(mockRS.getString(11)).andReturn("Archibald").once();
		expect(mockRS.getString(12)).andReturn("Jr.").once();
		expect(mockRS.getString(13)).andReturn("God").once();
		expect(mockRS.getString(14)).andReturn("Anabelle").once();
		expect(mockRS.getString(15)).andReturn("Michelle").once();
		expect(mockRS.getInt(16)).andReturn(300).once();
		expect(mockRS.wasNull()).andReturn(false);
		expect(mockRS.getString(17)).andReturn("Anonymous coward").once();
		expect(mockRS.getString(18)).andReturn("Bossman").once();

		expect(mockRS.next()).andReturn(true).once();
		expect(mockRS.getInt("contactentity_entity_id")).andReturn(11).once();
		expect(mockRS.getInt(1)).andReturn(2).once();
		expect(mockRS.getString(2)).andReturn("Jane").once();
		expect(mockRS.getString(3)).andReturn("Done").once();
		expect(mockRS.getInt(4)).andReturn(11).once();
		expect(mockRS.getString(5)).andReturn(null).once();
		expect(mockRS.getString(6)).andReturn(null).once();
		expect(mockRS.getString(7)).andReturn(null).once();
		expect(mockRS.getString(8)).andReturn(null).once();
		expect(mockRS.getInt(9)).andReturn(0).once();
		expect(mockRS.wasNull()).andReturn(true);
		expect(mockRS.getInt(10)).andReturn(201).once();
		expect(mockRS.getString(11)).andReturn(null).once();
		expect(mockRS.getString(12)).andReturn(null).once();
		expect(mockRS.getString(13)).andReturn(null).once();
		expect(mockRS.getString(14)).andReturn(null).once();
		expect(mockRS.getString(15)).andReturn(null).once();
		expect(mockRS.getInt(16)).andReturn(0).once();
		expect(mockRS.wasNull()).andReturn(true);
		expect(mockRS.getString(17)).andReturn(null).once();
		expect(mockRS.getString(18)).andReturn(null).once();

		expect(mockRS.next()).andReturn(true).once();
		expect(mockRS.getInt("contactentity_entity_id")).andReturn(11).once();

		expect(mockRS.next()).andReturn(false).once();

		mockRS.close();
		expectLastCall();

		mockObmHelper.cleanup(null, mockStatement, mockRS);
		expectLastCall();
		control.replay();

		Contact johnDoe = new Contact();
		johnDoe.setUid(1);
		johnDoe.setFirstname("John");
		johnDoe.setLastname("Doe");
		johnDoe.setEntityId(EntityId.valueOf(10));
		johnDoe.setAka("aka john.doe");
		johnDoe.setCompany("John Doe Inc.");
		johnDoe.setTitle("Like a boss");
		johnDoe.setService("8th directorate");
		johnDoe.setBirthdayId(new EventObmId(100));
		johnDoe.setAnniversaryId(new EventObmId(200));
		johnDoe.setMiddlename("Archibald");
		johnDoe.setSuffix("Jr.");
		johnDoe.setManager("God");
		johnDoe.setAssistant("Anabelle");
		johnDoe.setSpouse("Michelle");
		johnDoe.setFolderId(300);
		johnDoe.setComment("Anonymous coward");
		johnDoe.setCommonname("Bossman");

		Contact janeDone = new Contact();
		janeDone.setUid(2);
		janeDone.setFirstname("Jane");
		janeDone.setLastname("Done");
		janeDone.setFolderId(0);
		janeDone.setEntityId(EntityId.valueOf(11));

		ContactDaoJdbcImpl.ContactResults contactResults = contactDao.loadContactsFromDB(evtIds, mockConn,
				limit);

		assertThat(contactResults.contactList).containsExactly(johnDoe, janeDone);
		control.verify();
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
