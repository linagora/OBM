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

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Map;

import org.obm.configuration.DatabaseConfiguration;
import org.obm.configuration.DatabaseFlavour;

import com.google.common.collect.ImmutableMap;

public class PostgresDriverConfiguration implements DatabaseDriverConfiguration {

	@Override
	public String getLastInsertIdQuery() {
		return "SELECT lastval()";
	}

	@Override
	public String getDataSourceClassName() {
		return "org.postgresql.xa.PGXADataSource";
	}

	@Override
	public String getNonXADataSourceClassName() {
		return "org.postgresql.ds.PGSimpleDataSource";
	}

	@Override
	public DatabaseFlavour getFlavour() {
		return DatabaseFlavour.PGSQL;
	}

	@Override
	public Map<String, String> getDriverProperties(DatabaseConfiguration conf) {
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		builder.put("user", conf.getDatabaseLogin());
		builder.put("password", conf.getDatabasePassword());
		builder.put("databaseName", conf.getDatabaseName());
		builder.put("serverName", conf.getDatabaseHost());
		builder.put("ssl", String.valueOf(conf.isPostgresSSLEnabled()));
		if (conf.isPostgresSSLNonValidating()) {
			builder.put("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
		}
		return builder.build();
	}

	@Override
	public boolean readOnlySupported() {
		return true;
	}

	@Override
	public String getGMTTimezoneQuery() {
		return "SET LOCAL TIME ZONE 'GMT'";
	}

	@Override
	public String getIntegerCastType() {
		return "INTEGER";
	}
	
	@Override
	public Object getJDBCObject(String type, String value) throws SQLException {
		try {
			Object o = Class.forName("org.postgresql.util.PGobject").newInstance();
			Method setType = o.getClass().getMethod("setType", String.class);
			Method setValue = o.getClass().getMethod("setValue", String.class);
			setType.invoke(o, type);
			setValue.invoke(o, value);
			return o;
		} catch (Throwable e) {
			throw new SQLException(e.getMessage(), e);
		}
	}
}
