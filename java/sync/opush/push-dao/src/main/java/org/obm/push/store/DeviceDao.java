package org.obm.push.store;

import org.obm.push.bean.Device;
import org.obm.push.bean.User;
import org.obm.push.exception.DaoException;

public interface DeviceDao {
	
	/**
	 * Returns <code>true</code> if the device is authorized to synchronize.
	 */
	boolean syncAuthorized(User user, String deviceId) throws DaoException;

	public Device getDevice(User user, String deviceId, String userAgent)
			throws DaoException;

	public boolean registerNewDevice(User user, String deviceId,
			String deviceType) throws DaoException;

}
