package org.obm.push.service.impl;

import org.obm.configuration.SyncPermsConfigurationService;
import org.obm.push.bean.Device;
import org.obm.push.bean.User;
import org.obm.push.exception.DaoException;
import org.obm.push.service.DeviceService;
import org.obm.push.store.DeviceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DeviceServiceImpl implements DeviceService {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final DeviceDao deviceDao;
	private final SyncPermsConfigurationService opushSyncPermsConfigurationService;
	
	@Inject
	private DeviceServiceImpl(SyncPermsConfigurationService opushSyncPermsConfigurationService, DeviceDao deviceDao){
		this.opushSyncPermsConfigurationService = opushSyncPermsConfigurationService;
		this.deviceDao = deviceDao;
	}
	
	@Override
	public boolean initDevice(User loginAtDomain, String deviceId,
			String deviceType, String userAgent) {
		boolean ret = true;
		try {
			Device opushDeviceId = deviceDao.getDevice(loginAtDomain, deviceId, userAgent);
			if (opushDeviceId == null) {
				boolean registered = deviceDao.registerNewDevice(loginAtDomain, deviceId, deviceType);
				if (!registered) {
					logger.warn("did not insert any row in device table for device "
							+ deviceType + " of " + loginAtDomain);
					ret = false;
				}
			}
		} catch (Throwable se) {
			logger.error(se.getMessage(), se);
			ret = false;
		}
		return ret;
	}
	
	@Override
	public boolean syncAuthorized(User loginAtDomain, String deviceId) throws DaoException {
		if (userIsBlacklisted(loginAtDomain)) {
			return false;
		}
		
		final Boolean syncperm = opushSyncPermsConfigurationService.allowUnknownPdaToSync();
		if(syncperm){
			return true;
		}
		
		return deviceDao.syncAuthorized(loginAtDomain, deviceId);
	}

	private boolean userIsBlacklisted(User loginAtDomain) {
		String userList = opushSyncPermsConfigurationService.getBlackListUser();
		String blacklist = Strings.nullToEmpty(userList);
		Iterable<String> users = Splitter.on(',').trimResults()
				.split(blacklist);
		for (String user : users) {
			if (user.equalsIgnoreCase(loginAtDomain.getLoginAtDomain())) {
				return true;
			}
		}
		return false;
	}
	
}
