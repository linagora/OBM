package org.obm.push.store;

import java.sql.SQLException;

public interface DeviceDao {
	
	/**
	 * Returns <code>true</code> if the device is authorized to synchronize.
	 */
	boolean syncAuthorized(String loginAtDomain, String deviceId);

	public Integer findDevice(String loginAtDomain, String deviceId)
			throws SQLException;

	public boolean registerNewDevice(String loginAtDomain, String deviceId,
			String deviceType) throws SQLException;

}
