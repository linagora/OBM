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
package org.obm.sync.dao;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.assertj.core.api.Assertions.assertThat;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.easymock.IMocksControl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.GuiceRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;

@GuiceModule(TableDescriptionTest.Env.class)
@RunWith(GuiceRunner.class)
public class TableDescriptionTest {
	public static class Env extends AbstractModule {

		private final IMocksControl control = createControl();
		
		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(control);
			
			bindWithMock(ResultSetMetaData.class);
		}
		
		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(control.createMock(cls));
		}
		
	}
 	
	@Inject
	private IMocksControl mocksControl;
	
	@Inject
	private ResultSetMetaData resultSetMetaData;
	
	private static final String COLUMN = "event_description";
	private static final String COLUMN2 = "event_description2";
	private static int MAX_BYTES = 16;
	private static int MAX_BYTES2 = 32;
	
	@Test
	public void testGetMaxAllowedBytesOf() throws SQLException {
		expect(resultSetMetaData.getColumnCount()).andReturn(2).once();
		expect(resultSetMetaData.getColumnName(1)).andReturn(COLUMN).once();
		expect(resultSetMetaData.getColumnName(2)).andReturn(COLUMN2).once();
		expect(resultSetMetaData.getColumnDisplaySize(1)).andReturn(MAX_BYTES).once();
		expect(resultSetMetaData.getColumnDisplaySize(2)).andReturn(MAX_BYTES2).once();
		
		mocksControl.replay();
		TableDescription tableDescription = new TableDescription(resultSetMetaData);
		int maxBytes = tableDescription.getMaxAllowedBytesOf(COLUMN);
		mocksControl.verify();
		
		assertThat(maxBytes).isEqualTo(MAX_BYTES);
	}
	
	@Test
	public void testGetMaxAllowedBytesIsCaseInsensitive() throws SQLException {
		expect(resultSetMetaData.getColumnCount()).andReturn(1).once();
		expect(resultSetMetaData.getColumnName(1)).andReturn("Event_Description").once();
		expect(resultSetMetaData.getColumnDisplaySize(1)).andReturn(MAX_BYTES).once();
		
		mocksControl.replay();
		TableDescription tableDescription = new TableDescription(resultSetMetaData);
		int maxBytes = tableDescription.getMaxAllowedBytesOf("event_description");
		mocksControl.verify();
		
		assertThat(maxBytes).isEqualTo(MAX_BYTES);
	}
	
	@Test(expected=SQLException.class)
	public void testGetMaxAllowedBytesWithNotFoundColumn() throws SQLException {
		expect(resultSetMetaData.getColumnCount()).andReturn(2).once();
		expect(resultSetMetaData.getColumnName(1)).andReturn("").once();
		expect(resultSetMetaData.getColumnDisplaySize(1)).andReturn(MAX_BYTES).once();
		expect(resultSetMetaData.getColumnName(2)).andReturn(COLUMN2).once();
		expect(resultSetMetaData.getColumnDisplaySize(2)).andReturn(MAX_BYTES2).once();
		
		mocksControl.replay();
		TableDescription tableDescription = new TableDescription(resultSetMetaData);
		
		try {
			tableDescription.getMaxAllowedBytesOf(COLUMN);
		} catch (SQLException e) {
			throw e;
		} finally {
			mocksControl.verify();
		}
	}
}
