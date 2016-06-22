/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.common.calendar;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMockBuilder;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.ContactConfiguration;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.domain.dao.AddressBookDao;
import org.obm.domain.dao.ObmInfoDao;
import org.obm.domain.dao.UserDao;
import org.obm.domain.dao.UserDaoJdbcImpl;
import org.obm.domain.dao.UserPatternDao;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;
import org.obm.provisioning.dao.GroupDao;
import org.obm.provisioning.dao.ProfileDao;
import org.obm.service.solr.SolrHelper;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.base.EmailAddress;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.ContactAttendee;
import org.obm.sync.calendar.EventExtId;
import org.obm.sync.calendar.ResourceAttendee;
import org.obm.sync.calendar.UserAttendee;
import org.obm.sync.dao.EntityId;
import org.obm.sync.date.DateProvider;
import org.obm.sync.services.AttendeeService;
import org.obm.utils.ObmHelper;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.contact.ContactDao;
import fr.aliacom.obm.common.contact.ContactDaoJdbcImpl;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.resource.Resource;
import fr.aliacom.obm.common.resource.ResourceDao;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserEmails;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.utils.HelperService;

@GuiceModule(AttendeeServiceJdbcImplTest.Env.class)
@RunWith(GuiceRunner.class)
public class AttendeeServiceJdbcImplTest {

	public static class Env extends AbstractModule {
		private final IMocksControl mocksControl = createControl();
		
		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);
			
			bindWithMock(SolrHelper.Factory.class);
			bindWithMock(CalendarDao.class);
			bindWithMock(DomainService.class);
			bindWithMock(UserService.class);
			bindWithMock(HelperService.class);
			bindWithMock(DatabaseConnectionProvider.class);
			bindWithMock(DateProvider.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixturePostgreSQL.class);
		}
		
		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}
	
	@Inject
	private IMocksControl mocksControl;
	@Inject
	private ObmHelper obmHelper;
	@Inject
	private ContactConfiguration contactConfiguration;
	@Inject
	private CalendarDao calendarDao;
	@Inject
	private SolrHelper.Factory solrHelperFactory;
	@Inject
	private EventExtId.Factory eventExtIdFactory;
	
	private UserDao userDao;
	private ContactDao contactDao;
	private ResourceDao resourceDao;
	private AttendeeService attendeeService;
	private ObmDomain domain;
	private ObmUser johnDoe;
	private Contact externalContact;
	private Resource resource;
	
	@Before
	public void setUp() {
		domain = ToolBox.getDefaultObmDomain();
		johnDoe = johnDoe(domain);
		externalContact = externalContact();
		resource = resource();
		userDao = createMockBuilder(UserDaoJdbcImpl.class)
				.withConstructor(ObmHelper.class, ObmInfoDao.class, AddressBookDao.class, UserPatternDao.class, GroupDao.class, ProfileDao.class)
				.withArgs(obmHelper, null, null, null, null, null)
				.addMockedMethod("findUser")
				.createMock(mocksControl);
		contactDao = createMockBuilder(ContactDaoJdbcImpl.class)
				.withConstructor(ContactConfiguration.class, CalendarDao.class, SolrHelper.Factory.class, ObmHelper.class, EventExtId.Factory.class)
				.withArgs(contactConfiguration, calendarDao, solrHelperFactory, obmHelper, eventExtIdFactory)
				.addMockedMethod("findAttendeeContactFromEmailForUser")
				.addMockedMethod("createCollectedContact")
				.createMock(mocksControl);
		resourceDao = createMockBuilder(ResourceDao.class)
				.withConstructor(ObmHelper.class)
				.withArgs(obmHelper)
				.addMockedMethod("findAttendeeResourceFromEmailForUser")
				.addMockedMethod("findAttendeeResourceFromNameForUser")
				.createMock(mocksControl);
		
		attendeeService = new AttendeeServiceJdbcImpl(userDao, contactDao, resourceDao);
	}
	
	@After
	public void tearDown() {
		mocksControl.verify();
	}
	
	private Resource resource() {
		return Resource
				.builder()
				.entityId(EntityId.valueOf(2))
				.id(1)
				.mail("res-1@test.tlse.lng")
				.name("Res")
				.build();
	}
	
	private ObmUser johnDoe(ObmDomain userDomain) {
		return ObmUser.builder()
				.uid(1)
				.entityId(EntityId.valueOf(2))
				.login(UserLogin.valueOf("johndoe@test.tlse.lng"))
				.domain(userDomain)
				.emails(UserEmails.builder()
					.addAddress("johndoe@test.tlse.lng")
					.domain(userDomain)
					.build())
				.build();
	}
	
	private Contact externalContact() {
		Contact contact = new Contact();
		
		contact.setUid(1);
		contact.addEmail("label", EmailAddress.loginAtDomain("external.to@my.domain"));
		contact.setCommonname("external");
		contact.setEntityId(EntityId.valueOf(2));
		
		return  contact;
	}
	
	@Test
	public void testFindUserAttendeeNoResult() {
		expect(userDao.findUser(eq("johndoe@test.tlse.lng"), eq(domain))).andReturn(null).once();
		mocksControl.replay();
		
		UserAttendee attendee = attendeeService.findUserAttendee("johndoe", "johndoe@test.tlse.lng", domain);
		
		assertThat(attendee).isNull();
	}
	
	@Test
	public void testFindUserAttendee() {
		expect(userDao.findUser(eq("johndoe@test.tlse.lng"), eq(domain))).andReturn(johnDoe).once();
		mocksControl.replay();
		
		UserAttendee attendee = attendeeService.findUserAttendee("johndoe", "johndoe@test.tlse.lng", domain);
		
		assertThat(attendee).isNotNull();
		assertThat(attendee.getEmail()).isEqualTo("johndoe@test.tlse.lng");
		assertThat(attendee.getEntityId()).isEqualTo(EntityId.valueOf(2));
	}
	
	@Test
	public void testFindContactAttendee() throws Exception {
		expect(contactDao.findAttendeeContactFromEmailForUser(eq("external.to@my.domain"), eq(1))).andReturn(externalContact).once();
		mocksControl.replay();
		
		ContactAttendee attendee = attendeeService.findContactAttendee("external", "external.to@my.domain", false, domain, 1);
		
		assertThat(attendee).isNotNull();
		assertThat(attendee.getEmail()).isEqualTo("external.to@my.domain");
		assertThat(attendee.getEntityId()).isEqualTo(EntityId.valueOf(2));
	}
	
	@Test
	public void testFindContactAttendeeNoResult() throws Exception {
		expect(contactDao.findAttendeeContactFromEmailForUser(eq("external.to@my.domain"), eq(1))).andReturn(null).once();
		mocksControl.replay();
		
		ContactAttendee attendee = attendeeService.findContactAttendee("external", "external.to@my.domain", false, domain, 1);
		
		assertThat(attendee).isNull();
	}
	
	@Test
	public void testFindContactAttendeeWithEmptyNameCreateCollected() throws SQLException, ServerFault {
		expect(contactDao.findAttendeeContactFromEmailForUser("external.to@my.domain", 1)).andReturn(null).once();
		expect(contactDao.createCollectedContact("external.to@my.domain", "external.to@my.domain", domain, 1)).andReturn(externalContact).once();
		
		mocksControl.replay();
		
		ContactAttendee attendee = attendeeService.findContactAttendee("", "external.to@my.domain", true, domain, 1);
		assertThat(attendee).isNotNull();
	}
	
	@Test
	public void testFindContactAttendeeWithNullNameCreateCollected() throws SQLException, ServerFault {
		expect(contactDao.findAttendeeContactFromEmailForUser("external.to@my.domain", 1)).andReturn(null).once();
		expect(contactDao.createCollectedContact("external.to@my.domain", "external.to@my.domain", domain, 1)).andReturn(externalContact).once();
		
		mocksControl.replay();
		
		ContactAttendee attendee = attendeeService.findContactAttendee(null, "external.to@my.domain", true, domain, 1);
		assertThat(attendee).isNotNull();
	}
	
	@Test
	public void testFindContactAttendeeCreatesCollectedContactIfNoResult() throws Exception {
		expect(contactDao.findAttendeeContactFromEmailForUser(eq("external.to@my.domain"), eq(1))).andReturn(null).once();
		expect(contactDao.createCollectedContact("external", "external.to@my.domain", domain, 1)).andReturn(externalContact).once();
		mocksControl.replay();
		
		ContactAttendee attendee = attendeeService.findContactAttendee("external", "external.to@my.domain", true, domain, 1);
		
		assertThat(attendee).isNotNull();
		assertThat(attendee.getEmail()).isEqualTo("external.to@my.domain");
		assertThat(attendee.getEntityId()).isEqualTo(EntityId.valueOf(2));
	}
	
	@Test
	public void testFindResourceAttendee() throws Exception {
		expect(resourceDao.findAttendeeResourceFromEmailForUser("res-1@test.tlse.lng", 1)).andReturn(resource).once();
		mocksControl.replay();
		
		ResourceAttendee attendee = attendeeService.findResourceAttendee("Res", "res-1@test.tlse.lng", domain, 1);
		
		assertThat(attendee).isNotNull();
		assertThat(attendee.getEmail()).isEqualTo("res-1@test.tlse.lng");
		assertThat(attendee.getEntityId()).isEqualTo(EntityId.valueOf(2));
	}
	
	@Test
	public void testFindResourceAttendeeNoResult() throws Exception {
		expect(resourceDao.findAttendeeResourceFromEmailForUser("res-1@test.tlse.lng", 1)).andReturn(null).once();
		expect(resourceDao.findAttendeeResourceFromNameForUser("Res", 1)).andReturn(null).once();
		mocksControl.replay();
		
		ResourceAttendee attendee = attendeeService.findResourceAttendee("Res", "res-1@test.tlse.lng", domain, 1);
		
		assertThat(attendee).isNull();
	}
	
	@Test
	public void testFindResourceAttendeeFallbackToNameSearch() throws Exception {
		expect(resourceDao.findAttendeeResourceFromEmailForUser("res-1@test.tlse.lng", 1)).andReturn(null).once();
		expect(resourceDao.findAttendeeResourceFromNameForUser("Res", 1)).andReturn(resource).once();
		mocksControl.replay();
		
		ResourceAttendee attendee = attendeeService.findResourceAttendee("Res", "res-1@test.tlse.lng", domain, 1);
		
		assertThat(attendee).isNotNull();
		assertThat(attendee.getEmail()).isEqualTo("res-1@test.tlse.lng");
		assertThat(attendee.getEntityId()).isEqualTo(EntityId.valueOf(2));
	}
	
	@Test
	public void testFindAttendeeReturnsUserIfFound() {
		expect(userDao.findUser(eq("johndoe@test.tlse.lng"), eq(domain))).andReturn(johnDoe).once();
		mocksControl.replay();
		
		Attendee attendee = attendeeService.findAttendee("johndoe", "johndoe@test.tlse.lng", true, domain, 1);
		
		assertThat(attendee).isNotNull().isInstanceOf(UserAttendee.class);
		assertThat(attendee.getEmail()).isEqualTo("johndoe@test.tlse.lng");
		assertThat(attendee.getEntityId()).isEqualTo(EntityId.valueOf(2));
	}
	
	@Test
	public void testFindAttendeeReturnsResourceIfNoUserFound() throws Exception {
		expect(userDao.findUser(eq("johndoe@test.tlse.lng"), eq(domain))).andReturn(null).once();
		expect(resourceDao.findAttendeeResourceFromEmailForUser("johndoe@test.tlse.lng", 1)).andReturn(resource).once();
		mocksControl.replay();
		
		Attendee attendee = attendeeService.findAttendee("johndoe", "johndoe@test.tlse.lng", true, domain, 1);
		
		assertThat(attendee).isNotNull().isInstanceOf(ResourceAttendee.class);
		assertThat(attendee.getEmail()).isEqualTo("res-1@test.tlse.lng");
		assertThat(attendee.getEntityId()).isEqualTo(EntityId.valueOf(2));
	}
	
	@Test
	public void testFindAttendeeReturnsContactIfNoUserAndNoResourceFound() throws Exception {
		expect(userDao.findUser(eq("johndoe@test.tlse.lng"), eq(domain))).andReturn(null).once();
		expect(resourceDao.findAttendeeResourceFromEmailForUser("johndoe@test.tlse.lng", 1)).andReturn(null).once();
		expect(resourceDao.findAttendeeResourceFromNameForUser("johndoe", 1)).andReturn(null).once();
		expect(contactDao.findAttendeeContactFromEmailForUser(eq("johndoe@test.tlse.lng"), eq(1))).andReturn(externalContact).once();
		mocksControl.replay();
		
		Attendee attendee = attendeeService.findAttendee("johndoe", "johndoe@test.tlse.lng", true, domain, 1);
		
		assertThat(attendee).isNotNull().isInstanceOf(ContactAttendee.class);
		assertThat(attendee.getEmail()).isEqualTo("external.to@my.domain");
		assertThat(attendee.getEntityId()).isEqualTo(EntityId.valueOf(2));
	}
	
	@Test
	public void testFindAttendeeCreatesCollectedContactInLastResort() throws Exception {
		expect(userDao.findUser(eq("johndoe@test.tlse.lng"), eq(domain))).andReturn(null).once();
		expect(resourceDao.findAttendeeResourceFromEmailForUser("johndoe@test.tlse.lng", 1)).andReturn(null).once();
		expect(resourceDao.findAttendeeResourceFromNameForUser("johndoe", 1)).andReturn(null).once();
		expect(contactDao.findAttendeeContactFromEmailForUser(eq("johndoe@test.tlse.lng"), eq(1))).andReturn(null).once();
		expect(contactDao.createCollectedContact("johndoe", "johndoe@test.tlse.lng", domain, 1)).andReturn(externalContact).once();
		mocksControl.replay();
		
		Attendee attendee = attendeeService.findAttendee("johndoe", "johndoe@test.tlse.lng", true, domain, 1);
		
		assertThat(attendee).isNotNull().isInstanceOf(ContactAttendee.class);
		assertThat(attendee.getEmail()).isEqualTo("external.to@my.domain");
		assertThat(attendee.getEntityId()).isEqualTo(EntityId.valueOf(2));
	}

}
