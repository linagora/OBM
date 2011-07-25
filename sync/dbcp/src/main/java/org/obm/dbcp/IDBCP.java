package org.obm.dbcp;

import java.sql.Connection;
import java.sql.SQLException;

public interface IDBCP {
	public DataSource getDataSource();
	public int lastInsertId(Connection con) throws SQLException;	
}
