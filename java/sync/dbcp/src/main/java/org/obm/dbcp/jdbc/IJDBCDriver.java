package org.obm.dbcp.jdbc;

import java.util.Map;


public interface IJDBCDriver {

	String getSupportedDbType();

	String getJDBCUrl(String host, String dbName);
	
	String getLastInsertIdQuery();

	String setGMTTimezoneQuery();
	
	String getDataSourceClassName();
	
	String getUniqueName();

	Map<String,String> getDriverProperties(String login, String password, String dbName, String dbHost);
}
