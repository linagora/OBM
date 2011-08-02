package org.obm.push.store;

import java.sql.SQLException;

public interface HearbeatDao {

	long findLastHearbeat(String loginAtDomain, String deviceId) throws SQLException;

	void updateLastHearbeat(String loginAtDomain, String deviceId, long hearbeat) throws SQLException;

}
