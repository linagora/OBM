package org.obm.push.service;

import org.obm.push.exception.DaoException;

public interface DeviceService {
	
	boolean initDevice(String loginAtDomain, String deviceId,
			String deviceType, String userAgent);

	boolean syncAuthorized(String loginAtDomain, String deviceId) throws DaoException;
}
