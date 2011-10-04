package org.obm.push.store;

import org.obm.push.bean.Device;
import org.obm.push.exception.DaoException;

public interface DeviceDao {
	
	/**
	 * Returns <code>true</code> if the device is authorized to synchronize.
	 */
	boolean syncAuthorized(String loginAtDomain, String deviceId) throws DaoException;

	public Device getDevice(String loginAtDomain, String deviceId, String userAgent)
			throws DaoException;

	public boolean registerNewDevice(String loginAtDomain, String deviceId,
			String deviceType) throws DaoException;

}
