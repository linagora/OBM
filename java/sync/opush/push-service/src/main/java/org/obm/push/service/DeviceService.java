package org.obm.push.service;

import org.obm.push.bean.LoginAtDomain;
import org.obm.push.exception.DaoException;

public interface DeviceService {
	
	boolean initDevice(LoginAtDomain loginAtDomain, String deviceId,
			String deviceType, String userAgent);

	boolean syncAuthorized(LoginAtDomain loginAtDomain, String deviceId) throws DaoException;
}
