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
package fr.aliacom.obm.common.calendar;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyInt;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.obm.DateUtils.date;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.easymock.IMocksControl;
import org.junit.Rule;
import org.junit.Test;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.locator.store.LocatorService;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventObmId;
import org.obm.sync.date.DateProvider;
import org.obm.sync.solr.SolrManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserDao;


public class CalendarDaoJdbcImplTest {

	private static class Env extends AbstractModule {
		private IMocksControl mocksControl = createControl();
		
		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);
			
			bindWithMock(UserDao.class);
			bindWithMock(LocatorService.class);
			bindWithMock(CalendarDao.class);
			bindWithMock(DatabaseConnectionProvider.class);
			bindWithMock(DateProvider.class);
			bindWithMock(SolrManager.class);
			bind(DatabaseConfiguration.class).to(DatabaseConfigurationFixturePostgreSQL.class);
		}
		
		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}
	
	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(Env.class);
	
	@Inject
	private IMocksControl mocksControl;
	@Inject
	private CalendarDaoJdbcImpl calendarDaoJdbcImpl;
	@Inject
	private UserDao userDao;
	@Inject
	private DatabaseConnectionProvider dbcp;
	
	@Test
	public void touchParentOfDeclinedRecurrentEventsMustNotIncludeDuplicates() {
		Date date = date("2004-12-14T22:00:00");
		
		Event firstEventException = new Event();
		firstEventException.setStartDate(date("2012-12-18T15:19:00"));
		firstEventException.setDuration(3600);

		Event secondEventException = new Event();
		secondEventException.setStartDate(date("2012-12-19T15:19:00"));
		secondEventException.setDuration(3600);
		
		Event firstRecurrentEvent = new Event();
		firstRecurrentEvent.setTitle("firstRecurrentEvent");
		firstRecurrentEvent.setStartDate(date("2012-12-17T15:19:00"));
		firstRecurrentEvent.addEventException(firstEventException);
		firstRecurrentEvent.addEventException(secondEventException);

		Event secondRecurrentEvent = new Event();
		secondRecurrentEvent.setTitle("firstRecurrentEvent");
		secondRecurrentEvent.setStartDate(date("2012-12-17T15:19:00"));
		secondRecurrentEvent.addEventException(secondEventException);
		secondRecurrentEvent.addEventException(firstEventException);

		HashSet<Event> parentOfDeclinedRecurrentEvent = Sets.<Event>newHashSet();
		parentOfDeclinedRecurrentEvent.add(firstRecurrentEvent);
		parentOfDeclinedRecurrentEvent.add(secondRecurrentEvent);
		
		ArrayList<Event> result = Lists.<Event>newArrayList();
		calendarDaoJdbcImpl.touchParentOfDeclinedRecurrentEvents(parentOfDeclinedRecurrentEvent, result, date);
		
		assertThat(result).containsOnly(firstRecurrentEvent);
	}

	@Test
	public void testInsertAttendeesWithSameUserTwice() throws Exception {
		Integer userEntity = 1;
		Event event = new Event();
		String calendar = "calendar";
		ObmUser user = ToolBox.getDefaultObmUser();
		int domainId = user.getDomain().getId();
		Connection connection = mocksControl.createMock(Connection.class);
		PreparedStatement ps = mocksControl.createMock(PreparedStatement.class);
		AccessToken token = ToolBox.mockAccessToken(user.getLogin(), user.getDomain(), mocksControl);
		List<Attendee> attendees = ImmutableList.of(getAttendee("user1@test.com"), getAttendee("user1alias@test.com"));
		
		event.setUid(new EventObmId(1));
		
		expect(userDao.userEntityIdFromEmail(connection, calendar, domainId)).andReturn(0);
		expect(userDao.userEntityIdFromEmail(connection, "user1@test.com", domainId)).andReturn(userEntity);
		expect(userDao.userEntityIdFromEmail(connection, "user1alias@test.com", domainId)).andReturn(userEntity);
		
		ps.addBatch();
		expectLastCall().times(1); // Because UserDao returns the same entity_id for the two email addresses
		
		expect(ps.executeBatch()).andReturn(new int[0]);
		
		expectgetJDBCObjects();
		expectPreparedStatementDatabaseObjectsCalls(connection, ps);
		
		mocksControl.replay();
		
		calendarDaoJdbcImpl.insertAttendees(token, calendar, event, connection, attendees, true);
		
		mocksControl.verify();
	}
	
	private Attendee getAttendee(String email) {
		return Attendee.builder().email(email).build();
	}

	private void expectgetJDBCObjects() throws Exception {
		expect(dbcp.getJdbcObject(isA(String.class), isA(String.class))).andReturn("").anyTimes();
	}

	private void expectPreparedStatementDatabaseObjectsCalls(Connection connection, PreparedStatement ps) throws Exception {
		expect(connection.prepareStatement(isA(String.class))).andReturn(ps).anyTimes();
		
		ps.setInt(anyInt(), anyInt());
		expectLastCall().anyTimes();
		
		ps.setString(anyInt(), isA(String.class));
		expectLastCall().anyTimes();
		
		ps.setObject(anyInt(), anyObject());
		expectLastCall().anyTimes();
		
		ps.setBoolean(anyInt(), anyBoolean());
		expectLastCall().anyTimes();
		
		ps.close();
		expectLastCall().anyTimes();
		
		connection.close();
		expectLastCall().anyTimes();
	}
}
