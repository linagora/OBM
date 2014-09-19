/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2014  Linagora
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
package org.obm.dbcp;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import java.util.List;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;


public class RoundRobinMultiNodeDatabaseConnectionProviderSelectorTest {

	private IMocksControl control;
	private RoundRobinMultiNodeDatabaseConnectionProviderSelector testee;

	@Before
	public void setUp() {
		control = createControl();
		testee = new RoundRobinMultiNodeDatabaseConnectionProviderSelector();
	}

	@After
	public void tearDown() {
		control.verify();
	}

	@Test
	public void testSelectShouldReturnSingleElementWhenCalledMultipleTimes() throws Exception {
		DatabaseConnectionProvider dbcp = control.createMock(DatabaseConnectionProvider.class);
		List<DatabaseConnectionProvider> list = ImmutableList.of(dbcp);

		expect(dbcp.getConnection()).andReturn(null);
		control.replay();

		selectNTimes(list, 3);
		assertThat(testee.select(list).getConnection()).isNull();
	}

	@Test
	public void testSelectShouldReturnFirstElementWhenCalledOnce() throws Exception {
		DatabaseConnectionProvider dbcp = control.createMock(DatabaseConnectionProvider.class);
		List<DatabaseConnectionProvider> list = ImmutableList.of(
				dbcp,
				control.createMock(DatabaseConnectionProvider.class));

		expect(dbcp.getConnection()).andReturn(null);
		control.replay();

		assertThat(testee.select(list).getConnection()).isNull();
	}

	@Test
	public void testSelectShouldReturnSecondElementWhenCalledTwoTimes() throws Exception {
		DatabaseConnectionProvider dbcp = control.createMock(DatabaseConnectionProvider.class);
		List<DatabaseConnectionProvider> list = ImmutableList.of(
				control.createMock(DatabaseConnectionProvider.class),
				dbcp);

		expect(dbcp.getConnection()).andReturn(null);
		control.replay();

		selectNTimes(list, 1);
		assertThat(testee.select(list).getConnection()).isNull();
	}

	@Test
	public void testSelectShouldRoundRobin() throws Exception {
		DatabaseConnectionProvider dbcp = control.createMock(DatabaseConnectionProvider.class);
		List<DatabaseConnectionProvider> list = ImmutableList.of(
				dbcp,
				control.createMock(DatabaseConnectionProvider.class),
				control.createMock(DatabaseConnectionProvider.class));

		expect(dbcp.getConnection()).andReturn(null);
		control.replay();

		selectNTimes(list, 3);
		assertThat(testee.select(list).getConnection()).isNull();
	}

	private void selectNTimes(List<DatabaseConnectionProvider> list, int n) {
		for (int i = 0; i < n; i++) {
			testee.select(list);
		}
	}

}
