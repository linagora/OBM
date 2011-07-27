package org.obm.dbcp;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDBCP {
	public Connection getConnection() throws SQLException;
	public int lastInsertId(Connection con) throws SQLException;	
}
