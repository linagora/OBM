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

package org.obm.dbcp.jdbc;

import java.sql.SQLException;
import java.util.Map;

import org.obm.configuration.DatabaseConfiguration;
import org.obm.configuration.DatabaseFlavour;

import com.google.common.collect.ImmutableMap;

public class MySQLDriverConfiguration implements DatabaseDriverConfiguration {

	private String getJDBCUrl(String host, String dbName) {
		String jdbcUrl = "jdbc:mysql://" + host + "/" + dbName + "?" + getMySQLOptions();
		return jdbcUrl;
	}

	private String getMySQLOptions() {
		StringBuilder b = new StringBuilder();
		b.append("zeroDateTimeBehavior=convertToNull");
		b.append("&relaxAutocommit=true");
		b.append("&jdbcCompliantTruncation=false");
		b.append("&interactiveClient=true");
		b.append("&useGmtMillisForDatetime=true");
		b.append("&useUnicode=true");
		b.append("&characterEncoding=utf8");
		b.append("&characterSetResults=utf8");
		b.append("&connectionCollation=utf8_general_ci");
		return b.toString();
	}

	@Override
	public String getLastInsertIdQuery() {
		return "SELECT last_insert_id()";
	}

	@Override
	public String getDataSourceClassName() {
		return "com.mysql.jdbc.jdbc2.optional.MysqlXADataSource";
	}

	@Override
	public String getNonXADataSourceClassName() {
		return "com.mysql.jdbc.jdbc2.optional.MysqlDataSource";
	}

	@Override
	public DatabaseFlavour getFlavour() {
		return DatabaseFlavour.MYSQL;
	}

	@Override
	public Map<String, String> getDriverProperties(DatabaseConfiguration configuration) {
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		builder.put("user", configuration.getDatabaseLogin());
		builder.put("password", configuration.getDatabasePassword());
		builder.put("databaseName", configuration.getDatabaseName());
		builder.put("url", getJDBCUrl(configuration.getDatabaseHost(), configuration.getDatabaseName()));
		return builder.build();
	}

	/**
	 * read-only is disabled due to an exception thrown when the transactionManager try to open a new mysql transaction
	 */
	@Override
	public boolean readOnlySupported() {
		return false;
	}

	@Override
	public String getGMTTimezoneQuery() {
		return "SET time_zone='+00:00'";
	}

	@Override
	public String getIntegerCastType() {
		return "UNSIGNED INTEGER";
	}
	
	@Override
	public Object getJDBCObject(String type, String value) throws SQLException {
		return value;
	}
}
