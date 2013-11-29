package org.obm.utils;

import java.sql.Connection;
import java.sql.ResultSet;
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
			} catch (Throwable t) {
				logger.warn("Error closing ResultSet", t);
			}
		}
		if (stat != null) {
			try {
				stat.close();
			} catch (Throwable t) {
				logger.warn("Error closing Statement", t);
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (Throwable t) {
				logger.warn("Error closing Connection", t);
			}
		}
	}
}
