/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.obm.sync.base.ObmDbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.services.constant.ConstantService;

/**
 * Helper functions for hibernate / datasource management
 */
@Singleton
public class ObmHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(ObmHelper.class);
	private static final String DATA_SOURCE = "java:comp/env/jdbc/ObmDS";

	private DataSource ds;
	private ObmDbType type = ObmDbType.PGSQL;

	@Inject
	private ObmHelper(ConstantService constantService) throws NamingException {
		InitialContext context;
		try {
			context = new InitialContext();
			ds = (DataSource) context.lookup(DATA_SOURCE);
		} catch (NamingException e) {
			logger.error("Cannot locate datasource at " + "jdbc/ObmDS", e);
			throw e;
		} catch (RuntimeException t) {
			logger.error("Unexpected error while initializing ObmHelper", t);
			throw t;
		}
		type = ObmDbType.valueOf(constantService.getStringValue("dbtype").trim());
	}

	/**
	 * Close every JDBC resources, checking that objects are not null
	 */
	public void cleanup(Connection con, Statement st, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				logger.debug("error closing ResultSet", e);
			}
		}
		if (st != null) {
			try {
				st.close();
			} catch (SQLException e) {
				logger.debug("error closing Statement", e);
			}
		}
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				logger.debug("error closing Connection", e);
			}
		}
	}

	/**
	 * Get a connection from connection pool. We always get the same connection
	 * for a given transaction.
	 */
	public Connection getConnection() throws SQLException {
		Connection con = ds.getConnection();
		if (getType() == ObmDbType.MYSQL) {
			Statement st = con.createStatement();
			st.execute("set time_zone='+00:00'");
			st.close();
		} else if (getType() == ObmDbType.PGSQL) {
			Statement st = con.createStatement();
			st.execute("SET TIME ZONE 'GMT'");
			st.close();
		}
		return con;
	}

	public int lastInsertId(Connection con) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = con.createStatement();
			if (type == ObmDbType.PGSQL) {
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
			return rs.getTimestamp(1);
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

	public ObmDbType getType() {
		return type;
	}
}