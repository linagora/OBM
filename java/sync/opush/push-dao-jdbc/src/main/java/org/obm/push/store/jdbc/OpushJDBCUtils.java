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

package org.obm.push.store.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.transaction.UserTransaction;

import org.obm.push.technicallog.bean.KindToBeLogged;
import org.obm.push.technicallog.bean.ResourceType;
import org.obm.push.technicallog.bean.TechnicalLogging;
import org.obm.push.utils.JDBCUtils;
import org.obm.push.utils.JDBCUtils.ConnectionCloser;

public class OpushJDBCUtils {

	public static final void cleanup(Connection con, Statement ps, ResultSet rs) {
		JDBCUtils.cleanup(con, ps, rs, technicalLogConnectionCloser);
	}
	
	private static ConnectionCloser technicalLogConnectionCloser = new ConnectionCloser() {
		
		@Override
		@TechnicalLogging(kindToBeLogged=KindToBeLogged.RESOURCE, onEndOfMethod=true, resourceType=ResourceType.JDBC_CONNECTION)
		public Throwable closeConnectionThenGetFailure(Connection connection) {
			return JDBCUtils.closeConnectionThenGetFailure(connection);
		}
	};
	
	public static final void rollback(Connection con) {
		JDBCUtils.rollback(con);
	}

	public static void rollback(UserTransaction ut) {
		JDBCUtils.rollback(ut);
	}

	public static Date getDate(ResultSet rs, String fieldName) throws SQLException {
		return JDBCUtils.getDate(rs, fieldName);
	}

	public static Date getDate(ResultSet rs, Integer fieldNumber) throws SQLException {
		return JDBCUtils.getDate(rs, fieldNumber);
	}

	public static java.sql.Date getDateWithoutTime(Date lastSync) {
		return JDBCUtils.getDateWithoutTime(lastSync);
	}

	public static int convertNegativeIntegerToZero(ResultSet rs, String fieldName) throws SQLException {
		return JDBCUtils.convertNegativeIntegerToZero(rs, fieldName);
	}

	public static Integer getInteger(ResultSet rs, String fieldName) throws SQLException {
		return JDBCUtils.getInteger(rs, fieldName);
	}
}
