package org.obm.dbcp.jdbc;


public interface IJDBCDriver {

	String getSupportedDbType();

	String getJDBCUrl(String host, String dbName);

	String getLastInsertIdQuery();

	String setGMTTimezoneQuery();
}
