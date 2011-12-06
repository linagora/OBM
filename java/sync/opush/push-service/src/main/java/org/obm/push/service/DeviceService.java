package org.obm.push.service;

import org.obm.push.bean.User;
import org.obm.push.exception.DaoException;

public interface DeviceService {
	
	boolean initDevice(User loginAtDomain, String deviceId,
			String deviceType, String userAgent);

	boolean syncAuthorized(User loginAtDomain, String deviceId) throws DaoException;
}
