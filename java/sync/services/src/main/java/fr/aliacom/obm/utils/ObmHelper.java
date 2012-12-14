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
package fr.aliacom.obm.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.obm.configuration.DatabaseConfiguration;
import org.obm.configuration.DatabaseFlavour;
import org.obm.dbcp.DatabaseConnectionProvider;
import org.obm.push.utils.JDBCUtils;
import org.obm.sync.date.DateProvider;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Helper functions datasource management
 */
@Singleton
public class ObmHelper implements DateProvider {

	private DatabaseFlavour type = DatabaseFlavour.PGSQL;
	public static final String VCOMPONENT = "vcomponent";
	public static final String VROLE = "vrole";
	public static final String VPARTSTAT = "vpartstat";
	public static final String VOPACITY = "vopacity";

	private final DatabaseConnectionProvider dbcp;

	@Inject
	private ObmHelper(DatabaseConfiguration configuration, DatabaseConnectionProvider dbcp) {
		this.dbcp = dbcp;
		type = configuration.getDatabaseSystem();
	}

	/**
	 * Close every JDBC resources, checking that objects are not null
	 */
	public void cleanup(Connection con, Statement st, ResultSet rs) {
		DBUtils.cleanup(con, st, rs);
	}

	/**
	 * Get a connection from connection pool. We always get the same connection
	 * for a given transaction.
	 */
	public Connection getConnection() throws SQLException {
		return dbcp.getConnection();
	}

	public int lastInsertId(Connection con) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			if (type == DatabaseFlavour.PGSQL) {
				rs = st.executeQuery("SELECT lastval()");
			} else {
				rs = st.executeQuery("SELECT last_insert_id()");
			}
			rs.next();
			return rs.getInt(1);
		} finally {
			cleanup(null, st, rs);
		}
	}

	/**
	 * Allocate a new entity id in the obm 2.2 entity table
	 */
	private int allocateEntityId(Connection con) throws SQLException {
		Statement st = null;
		try {
			st = con.createStatement();
			st
					.executeUpdate("insert into Entity (entity_mailing) values (TRUE)");
		} finally {
			cleanup(null, st, null);
		}
		return lastInsertId(con);
	}

	/**
	 * call as linkEntity(con, UserEntity, user_id, X, Y) to link an entity to a
	 * user
	 */
	public LinkedEntity linkEntity(Connection con, String linkTable,
			String targetField, int targetId) throws SQLException {
		Statement st = null;

		Integer existing = fetchEntityId(con, linkTable.replace("Entity", ""),
				targetId);
		if (existing != null && existing > 0) {
			return new LinkedEntity(targetId, existing);
		}
		int eid = allocateEntityId(con);

		try {
			st = con.createStatement();
			st.executeUpdate("INSERT INTO " + linkTable + " ("
					+ linkTable.toLowerCase() + "_entity_id, "
					+ linkTable.toLowerCase() + "_" + targetField
					+ ") VALUES (" + eid + ", " + targetId + ")");
		} finally {
			cleanup(null, st, null);
		}
		return new LinkedEntity(targetId, eid);
	}

	public Date selectNow(Connection con) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT now()");
			rs.next();
			return JDBCUtils.getDate(rs, rs.getMetaData().getColumnName(1));
		} finally {
			cleanup(null, st, rs);
		}
	}

	private Integer fetchEntityId(Connection con, String tt, Integer uid)
			throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT " + tt.toLowerCase()
					+ "entity_entity_id FROM " + tt + "Entity WHERE "
					+ tt.toLowerCase() + "entity_" + tt.toLowerCase() + "_id="
					+ uid);
			if (rs.next()) {
				return rs.getInt(1);
			}
			return null;
		} finally {
			cleanup(null, st, rs);
		}
	}

	public Integer fetchEntityId(String tt, Integer uid)
			throws SQLException {
		Connection con = null;
		try {
			con = getConnection();
			return fetchEntityId(con, tt, uid);
		} finally {
			cleanup(con, null, null);
		}
	}

	public DatabaseFlavour getType() {
		return type;
	}

	public DatabaseConnectionProvider getDBCP() {
		return dbcp;
	}

	@Override
	public Date getDate() {
		Connection con = null;
		
		try {
			con = getConnection();
			
			return selectNow(con);
		}
		catch (Exception e) {
			throw Throwables.propagate(e);
		} finally {
			cleanup(con, null, null);
		}
	}
}
