package org.obm.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBUtils {
	private static final Logger logger = LoggerFactory.getLogger(DBUtils.class);

	public static void cleanup(ResultSet rs) {
		cleanup(null, null, rs);
	}

	public static void cleanup(Statement stat, ResultSet rs) {
		cleanup(null, stat, rs);
	}
	
	public static void cleanup(Connection conn, Statement stat, ResultSet rs) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
				logger.warn("Error closing ResultSet", e);
			}
		}
		if (stat != null) {
			try {
				stat.close();
			} catch (SQLException e) {
				logger.warn("Error closing Statement", e);
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.warn("Error closing Connection", e);
			}
		}
	}
}
