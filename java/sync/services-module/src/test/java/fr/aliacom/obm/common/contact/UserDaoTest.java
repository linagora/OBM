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

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.ContactConfiguration;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.Address;
import org.obm.sync.book.Contact;
import org.obm.sync.book.ContactLabel;
import org.obm.sync.book.Phone;
import org.obm.sync.date.DateProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.ServicesToolBox;
import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.utils.ObmHelper;

@GuiceModule(UserDaoTest.Env.class)
@RunWith(SlowGuiceRunner.class)
public class UserDaoTest {

	public static class Env extends AbstractModule {
		private IMocksControl mocksControl = createControl();

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);

			bindWithMock(DatabaseConnectionProvider.class);
			bindWithMock(DateProvider.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixturePostgreSQL.class);
		}

		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}

	@Inject IMocksControl mocksControl;
	@Inject ObmHelper obmHelper;
	@Inject ContactConfiguration contactConfiguration;

	private UserDao userDao;
	
	@Before
	public void setUp() {
		userDao = createMockBuilder(UserDao.class)
					.withConstructor(ContactConfiguration.class, ObmHelper.class)
					.withArgs(contactConfiguration, obmHelper)
					.createMock(mocksControl);
	}
	
	@Test
	public void testUserAsContact() throws SQLException {
		AccessToken at = ToolBox.mockAccessToken("login", ServicesToolBox.getDefaultObmDomain(), mocksControl);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		expect(rs.getInt("userobm_id")).andReturn(8);
		expect(rs.getString("userobm_firstname")).andReturn("firstname");
		expect(rs.getString("userobm_lastname")).andReturn("lastname");
		expect(rs.getString("userobm_commonname")).andReturn("commonname");
		expect(rs.getString("userobm_company")).andReturn("company");
		expect(rs.getString("userobm_service")).andReturn("service");
		expect(rs.getString("userobm_direction")).andReturn("direction");
		expect(rs.getString("userobm_title")).andReturn("title");
		expect(rs.getString("userobm_description")).andReturn("desc");
		expect(rs.getString("userobm_email")).andReturn("email");
		expect(rs.getString("userobm_phone")).andReturn("phone");
		expect(rs.getString("userobm_phone2")).andReturn("phone2");
		expect(rs.getString("userobm_mobile")).andReturn("mobile");
		expect(rs.getString("userobm_fax")).andReturn("fax");
		expect(rs.getString("userobm_fax2")).andReturn("fax2");
		expect(rs.getString("userobm_address1")).andReturn("add1");
		expect(rs.getString("userobm_address2")).andReturn("add2");
		expect(rs.getString("userobm_address3")).andReturn("add3");
		expect(rs.getString("userobm_zipcode")).andReturn("zipcode");
		expect(rs.getString("userobm_expresspostal")).andReturn("postal");
		expect(rs.getString("userobm_town")).andReturn("town");

		mocksControl.replay();
		Contact contact = userDao.userAsContact(rs, at);
		mocksControl.verify();

		Contact expectedContact = new Contact();
		expectedContact.setUid(8);
		expectedContact.setCollected(false);
		expectedContact.setFirstname("firstname");
		expectedContact.setLastname("lastname");
		expectedContact.setCommonname("commonname");
		expectedContact.setTitle("title");
		expectedContact.setCompany("company");
		expectedContact.setService("service");
		expectedContact.setManager("direction");
		expectedContact.setComment("desc");
		expectedContact.setFolderId(-1);
		expectedContact.addPhone(ContactLabel.PHONE.getContactLabel(), new Phone("phone"));
		expectedContact.addPhone(ContactLabel.PHONE2.getContactLabel(), new Phone("phone2"));
		expectedContact.addPhone(ContactLabel.MOBILE.getContactLabel(), new Phone("mobile"));
		expectedContact.addPhone(ContactLabel.FAX.getContactLabel(), new Phone("fax"));
		expectedContact.addPhone(ContactLabel.FAX2.getContactLabel(), new Phone("fax2"));
		expectedContact.addEmail(ContactLabel.EMAIL.getContactLabel(), EmailAddress.loginAtDomain("email@test.tlse.lng"));
		expectedContact.addAddress(ContactLabel.ADDRESS.getContactLabel(), new Address(
				"add1 add2 add3", "zipcode", "postal", "town", null, null));
		
		assertThat(contact).isEqualTo(expectedContact);
	}
	
	@Test
	public void testUserAsContactEmptyEmail() throws SQLException {
		AccessToken at = ToolBox.mockAccessToken("login", ServicesToolBox.getDefaultObmDomain(), mocksControl);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		expect(rs.getInt("userobm_id")).andReturn(8);
		expect(rs.getString("userobm_firstname")).andReturn("firstname");
		expect(rs.getString("userobm_lastname")).andReturn("lastname");
		expect(rs.getString("userobm_commonname")).andReturn("commonname");
		expect(rs.getString("userobm_company")).andReturn("company");
		expect(rs.getString("userobm_service")).andReturn("service");
		expect(rs.getString("userobm_direction")).andReturn("direction");
		expect(rs.getString("userobm_title")).andReturn("title");
		expect(rs.getString("userobm_description")).andReturn("desc");
		expect(rs.getString("userobm_email")).andReturn("");
		expect(rs.getString("userobm_phone")).andReturn("phone");
		expect(rs.getString("userobm_phone2")).andReturn("phone2");
		expect(rs.getString("userobm_mobile")).andReturn("mobile");
		expect(rs.getString("userobm_fax")).andReturn("fax");
		expect(rs.getString("userobm_fax2")).andReturn("fax2");
		expect(rs.getString("userobm_address1")).andReturn("add1");
		expect(rs.getString("userobm_address2")).andReturn("add2");
		expect(rs.getString("userobm_address3")).andReturn("add3");
		expect(rs.getString("userobm_zipcode")).andReturn("zipcode");
		expect(rs.getString("userobm_expresspostal")).andReturn("postal");
		expect(rs.getString("userobm_town")).andReturn("town");

		mocksControl.replay();
		Contact contact = userDao.userAsContact(rs, at);
		mocksControl.verify();

		Contact expectedContact = new Contact();
		expectedContact.setUid(8);
		expectedContact.setCollected(false);
		expectedContact.setFirstname("firstname");
		expectedContact.setLastname("lastname");
		expectedContact.setCommonname("commonname");
		expectedContact.setTitle("title");
		expectedContact.setCompany("company");
		expectedContact.setService("service");
		expectedContact.setManager("direction");
		expectedContact.setComment("desc");
		expectedContact.setFolderId(-1);
		expectedContact.addPhone(ContactLabel.PHONE.getContactLabel(), new Phone("phone"));
		expectedContact.addPhone(ContactLabel.PHONE2.getContactLabel(), new Phone("phone2"));
		expectedContact.addPhone(ContactLabel.MOBILE.getContactLabel(), new Phone("mobile"));
		expectedContact.addPhone(ContactLabel.FAX.getContactLabel(), new Phone("fax"));
		expectedContact.addPhone(ContactLabel.FAX2.getContactLabel(), new Phone("fax2"));
		expectedContact.addAddress(ContactLabel.ADDRESS.getContactLabel(), new Address(
				"add1 add2 add3", "zipcode", "postal", "town", null, null));
		
		assertThat(contact).isEqualTo(expectedContact);
	}
	
	@Test
	public void testUserAsContactFullEmail() throws SQLException {
		AccessToken at = ToolBox.mockAccessToken("login", ServicesToolBox.getDefaultObmDomain(), mocksControl);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		expect(rs.getInt("userobm_id")).andReturn(8);
		expect(rs.getString("userobm_firstname")).andReturn("firstname");
		expect(rs.getString("userobm_lastname")).andReturn("lastname");
		expect(rs.getString("userobm_commonname")).andReturn("commonname");
		expect(rs.getString("userobm_company")).andReturn("company");
		expect(rs.getString("userobm_service")).andReturn("service");
		expect(rs.getString("userobm_direction")).andReturn("direction");
		expect(rs.getString("userobm_title")).andReturn("title");
		expect(rs.getString("userobm_description")).andReturn("desc");
		expect(rs.getString("userobm_email")).andReturn("email@domain");
		expect(rs.getString("userobm_phone")).andReturn("phone");
		expect(rs.getString("userobm_phone2")).andReturn("phone2");
		expect(rs.getString("userobm_mobile")).andReturn("mobile");
		expect(rs.getString("userobm_fax")).andReturn("fax");
		expect(rs.getString("userobm_fax2")).andReturn("fax2");
		expect(rs.getString("userobm_address1")).andReturn("add1");
		expect(rs.getString("userobm_address2")).andReturn("add2");
		expect(rs.getString("userobm_address3")).andReturn("add3");
		expect(rs.getString("userobm_zipcode")).andReturn("zipcode");
		expect(rs.getString("userobm_expresspostal")).andReturn("postal");
		expect(rs.getString("userobm_town")).andReturn("town");

		mocksControl.replay();
		Contact contact = userDao.userAsContact(rs, at);
		mocksControl.verify();

		Contact expectedContact = new Contact();
		expectedContact.setUid(8);
		expectedContact.setCollected(false);
		expectedContact.setFirstname("firstname");
		expectedContact.setLastname("lastname");
		expectedContact.setCommonname("commonname");
		expectedContact.setTitle("title");
		expectedContact.setCompany("company");
		expectedContact.setService("service");
		expectedContact.setManager("direction");
		expectedContact.setComment("desc");
		expectedContact.setFolderId(-1);
		expectedContact.addPhone(ContactLabel.PHONE.getContactLabel(), new Phone("phone"));
		expectedContact.addPhone(ContactLabel.PHONE2.getContactLabel(), new Phone("phone2"));
		expectedContact.addPhone(ContactLabel.MOBILE.getContactLabel(), new Phone("mobile"));
		expectedContact.addPhone(ContactLabel.FAX.getContactLabel(), new Phone("fax"));
		expectedContact.addPhone(ContactLabel.FAX2.getContactLabel(), new Phone("fax2"));
		expectedContact.addEmail(ContactLabel.EMAIL.getContactLabel(), EmailAddress.loginAtDomain("email@domain"));
		expectedContact.addAddress(ContactLabel.ADDRESS.getContactLabel(), new Address(
				"add1 add2 add3", "zipcode", "postal", "town", null, null));
		
		assertThat(contact).isEqualTo(expectedContact);
	}
	
}
