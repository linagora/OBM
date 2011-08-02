package org.obm.push.store;

import java.sql.SQLException;

import org.obm.push.bean.Device;

public interface DeviceDao {
	
	/**
	 * Returns <code>true</code> if the device is authorized to synchronize.
	 */
	boolean syncAuthorized(String loginAtDomain, String deviceId);

	public Device getDevice(String loginAtDomain, String deviceId, String userAgent)
			throws SQLException;

	public boolean registerNewDevice(String loginAtDomain, String deviceId,
			String deviceType) throws SQLException;

}
