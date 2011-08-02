package org.obm.push.store;

import java.sql.SQLException;

public interface DeviceDao {

	public Integer findDevice(String loginAtDomain, String deviceId)
			throws SQLException;

	public boolean registerNewDevice(String loginAtDomain, String deviceId,
			String deviceType) throws SQLException;

}
