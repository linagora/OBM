package org.obm.push.store;

import org.obm.push.bean.Device;
import org.obm.push.bean.LoginAtDomain;
import org.obm.push.exception.DaoException;

public interface DeviceDao {
	
	/**
	 * Returns <code>true</code> if the device is authorized to synchronize.
	 */
	boolean syncAuthorized(LoginAtDomain loginAtDomain, String deviceId) throws DaoException;

	public Device getDevice(LoginAtDomain loginAtDomain, String deviceId, String userAgent)
			throws DaoException;

	public boolean registerNewDevice(LoginAtDomain loginAtDomain, String deviceId,
			String deviceType) throws DaoException;

}
