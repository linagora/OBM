/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013 Linagora
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
package org.obm.dao.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.push.utils.JDBCUtils;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class H2ConnectionProvider implements DatabaseConnectionProvider {

	private final H2InMemoryDatabase h2Instance;

	@Inject
	private H2ConnectionProvider(H2InMemoryDatabase h2Instance) {
		this.h2Instance = h2Instance;
	}

	@Override
	public Connection getConnection() throws SQLException {
		try {
			return h2Instance.getConnection();
		}
		catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	@Override
	public int lastInsertId(Connection con) throws SQLException {
		Statement stat = con.createStatement();
		ResultSet result = null;
		try {
			result = stat.executeQuery("CALL SCOPE_IDENTITY()");
			if (result.next()) {
				return result.getInt(1);
			}
			throw new IllegalStateException();
		} finally {
			JDBCUtils.cleanup(null, stat, result);
		}
	}

	@Override
	public Object getJdbcObject(String dbFieldName, String dbFieldValue) throws SQLException {
		return dbFieldValue;
	}

	public H2InMemoryDatabase geth2Instance() {
		return h2Instance;
	}

	@Override
	public String getIntegerCastType() {
		return "INTEGER";
	}
}