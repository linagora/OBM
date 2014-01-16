/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.impl;

import org.obm.configuration.SyncPermsConfigurationService;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.User;
import org.obm.push.exception.DaoException;
import org.obm.push.service.DeviceService;
import org.obm.push.store.DeviceDao;
import org.obm.push.store.DeviceDao.PolicyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DeviceServiceImpl implements DeviceService {

	protected final Logger logger = LoggerFactory.getLogger(getClass());
	
	private final DeviceDao deviceDao;
	private final SyncPermsConfigurationService syncPermsConfigurationService;
	
	@Inject
	private DeviceServiceImpl(SyncPermsConfigurationService syncPermsConfigurationService, DeviceDao deviceDao){
		this.syncPermsConfigurationService = syncPermsConfigurationService;
		this.deviceDao = deviceDao;
	}
	
	@Override
	public void initDevice(User loginAtDomain, DeviceId deviceId, 
			String deviceType, String userAgent, ProtocolVersion protocolVersion) {
		try {
			Device opushDeviceId = deviceDao.getDevice(loginAtDomain, deviceId, userAgent, protocolVersion);
			if (opushDeviceId == null) {
				deviceDao.registerNewDevice(loginAtDomain, deviceId, deviceType);
			}
		} catch (DaoException e) {
			Throwables.propagate(e);
		}
		
	}
	
	@Override
	public boolean syncAuthorized(User loginAtDomain, DeviceId deviceId) throws DaoException {
		if (userIsBlacklisted(loginAtDomain)) {
			return false;
		}
		
		final Boolean syncperm = syncPermsConfigurationService.allowUnknownPdaToSync();
		if(syncperm){
			return true;
		}
		
		return deviceDao.syncAuthorized(loginAtDomain, deviceId);
	}

	private boolean userIsBlacklisted(User loginAtDomain) {
		String userList = syncPermsConfigurationService.getBlackListUser();
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

	@Override
	public Device getDevice(User user, DeviceId deviceId, String userAgent, ProtocolVersion protocolVersion) throws DaoException {
		return deviceDao.getDevice(user, deviceId, userAgent, protocolVersion);
	}

	@Override
	public Long getPolicyKey(User user, DeviceId deviceId) throws DaoException {
		return deviceDao.getPolicyKey(user, deviceId, PolicyStatus.ACCEPTED);
	}
	
}
