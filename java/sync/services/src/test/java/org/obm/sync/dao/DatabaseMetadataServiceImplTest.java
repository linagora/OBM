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
package org.obm.sync.dao;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.filter.SlowFilterRunner;
import org.obm.opush.env.JUnitGuiceRule;

import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

@RunWith(SlowFilterRunner.class)
public class DatabaseMetadataServiceImplTest {

	private static class Env extends AbstractModule {

		private IMocksControl mocksControl = createControl();
		
		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(mocksControl);
			
			bindWithMock(DatabaseConnectionProvider.class);
			bindWithMock(DatabaseMetadataDao.class);
			bindWithMock(ResultSetMetaData.class);
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
	private DatabaseMetadataDao metadataDao;
	
	@Inject
	private ResultSetMetaData rsmd;
	
	private DatabaseMetadataServiceImpl dms;
	
	private static final String TABLE = "event";
	private static final String COLUMN_NAME = "event_description";
	private static int COLUMN = 1;
	private static int MAX_BYTES = 16;
	
	@After
	public void tearDown() {
		mocksControl.verify();
	}
	
	@Test
	public void testGetTableDescription() throws SQLException {
		
		expect(metadataDao.getResultSetMetadata(TABLE)).andReturn(rsmd).once();
		expect(rsmd.getColumnCount()).andReturn(COLUMN).once();
		expect(rsmd.getColumnName(COLUMN)).andReturn(COLUMN_NAME).once();
		expect(rsmd.getColumnDisplaySize(COLUMN)).andReturn(MAX_BYTES).once();
		
		mocksControl.replay();
		
		dms = new DatabaseMetadataServiceImpl(metadataDao);
		
		TableDescription tableDescription = dms.getTableDescriptionOf(TABLE);
		
		assertThat(tableDescription.getMaxAllowedBytesOf(COLUMN_NAME)).isEqualTo(16);
	}
	
	@Test
	public void testGetTableDescriptionTwiceInARow() throws SQLException {
		expect(metadataDao.getResultSetMetadata(TABLE)).andReturn(rsmd).once();
		expect(rsmd.getColumnCount()).andReturn(COLUMN).times(2);
		expect(rsmd.getColumnName(COLUMN)).andReturn(COLUMN_NAME).times(2);
		expect(rsmd.getColumnDisplaySize(COLUMN)).andReturn(MAX_BYTES).times(2);
		
		mocksControl.replay();
		
		dms = new DatabaseMetadataServiceImpl(metadataDao);
		
		TableDescription tableDescription = dms.getTableDescriptionOf(TABLE);
		TableDescription tableDescription2 = dms.getTableDescriptionOf(TABLE);
		
		assertThat(tableDescription.getMaxAllowedBytesOf(COLUMN_NAME)).isEqualTo(MAX_BYTES);
		assertThat(tableDescription2.getMaxAllowedBytesOf(COLUMN_NAME)).isEqualTo(MAX_BYTES);
	}
	
	@Test(expected=UncheckedExecutionException.class)
	public void testGetTableDescriptionFailsWithSQLException() throws SQLException {
		expect(metadataDao.getResultSetMetadata(TABLE)).andThrow(new SQLException());
		
		mocksControl.replay();
		
		dms = new DatabaseMetadataServiceImpl(metadataDao);
		dms.getTableDescriptionOf(TABLE);
	}
}
