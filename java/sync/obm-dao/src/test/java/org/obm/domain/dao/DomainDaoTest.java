/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.domain.dao;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.configuration.DatabaseConfiguration;
import org.obm.dbcp.DatabaseConfigurationFixturePostgreSQL;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.sync.date.DateProvider;
import org.obm.sync.serviceproperty.ServiceProperty;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.common.domain.ObmDomain;

@GuiceModule(DomainDaoTest.Env.class)
@RunWith(SlowGuiceRunner.class)
public class DomainDaoTest {
	
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

	@Inject
	private IMocksControl mocksControl;
	@Inject
	private DatabaseConnectionProvider dbcp;
	@Inject
	private DomainDao domainDao;

	@After
	public void tearDown() {
		mocksControl.verify();
	}
	
	@Test
	public void testFindDomainByName() throws Exception {
		ObmDomain d = findDomain(null);
		
		assertThat(d).isNotNull();
		assertThat(d.getId()).isEqualTo(1);
		assertThat(d.getName()).isEqualTo("domain");
		assertThat(d.getLabel()).isEqualTo("label");
		assertThat(d.getUuid().get()).isEqualTo("uuid");
		assertThat(d.getAliases()).isEmpty();
		assertThat(d.getHosts().get(ServiceProperty.OBM_SYNC)).hasSize(1);
		assertThat(d.isGlobal()).isEqualTo(false);
	}
	
	@Test
	public void testFindDomainByNameWithOneAlias() throws Exception {
		ObmDomain d = findDomain("alias");
		
		assertThat(d).isNotNull();
		assertThat(d.getId()).isEqualTo(1);
		assertThat(d.getName()).isEqualTo("domain");
		assertThat(d.getLabel()).isEqualTo("label");
		assertThat(d.getUuid().get()).isEqualTo("uuid");
		assertThat(d.getAliases()).containsExactly("alias");
		assertThat(d.getHosts().get(ServiceProperty.OBM_SYNC)).hasSize(1);
		assertThat(d.isGlobal()).isEqualTo(false);
	}
	
	@Test
	public void testFindDomainByNameWithMultipleAliases() throws Exception {
		ObmDomain d = findDomain("alias1\r\nalias2\r\nalias3");
		
		assertThat(d).isNotNull();
		assertThat(d.getId()).isEqualTo(1);
		assertThat(d.getName()).isEqualTo("domain");
		assertThat(d.getLabel()).isEqualTo("label");
		assertThat(d.getUuid().get()).isEqualTo("uuid");
		assertThat(d.getAliases()).containsExactly("alias1", "alias2", "alias3");
		assertThat(d.getHosts().get(ServiceProperty.OBM_SYNC)).hasSize(1);
		assertThat(d.isGlobal()).isEqualTo(false);
	}
	
	private ObmDomain findDomain(String aliases) throws Exception {
		String domainName = "domain";
		Connection con = mocksControl.createMock(Connection.class);
		ResultSet rs = mocksControl.createMock(ResultSet.class);
		
		expectFindDomainCalls(con, rs, domainName, aliases);
		expectFindDomainHostsCalls(con, rs);
		mocksControl.replay();
		
		return domainDao.findDomainByName(domainName);
	}

	private void expectFindDomainHostsCalls(Connection con, ResultSet rs) throws Exception {
		PreparedStatement statement = mocksControl.createMock(PreparedStatement.class);

		expect(dbcp.getConnection()).andReturn(con);
		expect(dbcp.getIntegerCastType()).andReturn("INTEGER");
		expect(con.prepareStatement(isA(String.class))).andReturn(statement);
		statement.setInt(1, 1);
		expectLastCall();
		expect(statement.executeQuery()).andReturn(rs);
		expect(rs.next()).andReturn(true);
		expect(rs.next()).andReturn(false);

		rs.close();
		expectLastCall();
		statement.close();
		expectLastCall();
		con.close();
		expectLastCall();

		expect(rs.getString("serviceproperty_service")).andReturn("sync");
		expect(rs.getString("serviceproperty_property")).andReturn("obm_sync");
		expect(rs.getInt("host_id")).andReturn(1);
		expect(rs.getString("host_ip")).andReturn("1.2.3.4");
		expect(rs.getString("host_name")).andReturn("local");
		expect(rs.getString("host_fqdn")).andReturn("local.global.virt");
		expect(rs.getInt("host_domain_id")).andReturn(1);
	}

	private void expectFindDomainCalls(Connection con, ResultSet rs, String domainName, String aliases) throws Exception {
		PreparedStatement statement = mocksControl.createMock(PreparedStatement.class);

		expect(dbcp.getConnection()).andReturn(con);
		expect(con.prepareStatement(isA(String.class))).andReturn(statement);
		statement.setString(1, domainName);
		statement.setString(2, domainName);
		statement.setString(3, domainName + "\r\n%");
		statement.setString(4, "%\r\n" + domainName + "\r\n%");
		statement.setString(5, "%\r\n" + domainName);
		expectLastCall();
		expect(statement.executeQuery()).andReturn(rs);
		expect(rs.next()).andReturn(true);

		rs.close();
		expectLastCall();
		statement.close();
		expectLastCall();
		con.close();
		expectLastCall();

		expect(rs.getString("domain_uuid")).andReturn("uuid");
		expect(rs.getInt("domain_id")).andReturn(1);
		expect(rs.getString("domain_alias")).andReturn(aliases);
		expect(rs.getString("domain_label")).andReturn("label");
		expect(rs.getString("domain_name")).andReturn(domainName);
		expect(rs.getBoolean("domain_global")).andReturn(false);
	}

}
