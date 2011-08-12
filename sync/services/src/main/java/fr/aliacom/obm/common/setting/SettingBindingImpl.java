/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (c) 1997-2008 Aliasource - Groupe LINAGORA
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation; either version 2 of the
 *  License, (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 * 
 *  http://www.obm.org/                                              
 * 
 * ***** END LICENSE BLOCK ***** */
package fr.aliacom.obm.common.setting;

import java.util.Map;

import org.obm.annotations.transactional.Transactional;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.services.ISetting;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.VacationSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.utils.LogUtils;

@Singleton
public class SettingBindingImpl implements ISetting {

	private static final Logger logger = LoggerFactory
			.getLogger(SettingBindingImpl.class);

	private SettingDao settingDao;

	private final UserService userService;

	@Inject
	protected SettingBindingImpl(SettingDao settingDao, UserService userService) {
		this.settingDao = settingDao;
		this.userService = userService;
	}

	@Override
	@Transactional
	public Map<String, String> getSettings(AccessToken token)
			throws ServerFault {
		try {
			logger.info(LogUtils.prefix(token) + "Setting : getSettings()");
			ObmUser user = userService.getUserFromAccessToken(token);
			return settingDao.getSettings(user);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public void setVacationSettings(AccessToken token, VacationSettings vs)
			throws ServerFault {
		try {
			logger.info(LogUtils.prefix(token) + "Setting : setVacation("
					+ vs.isEnabled() + " " + vs.getStart() + " " + vs.getEnd()
					+ " " + vs.getText() + ")");
			settingDao.setVacationSettings(token, vs);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public void setEmailForwarding(AccessToken token, ForwardingSettings fs)
			throws ServerFault {
		try {
			logger.info(LogUtils.prefix(token) + "Setting : setForwarding("
					+ fs.isEnabled() + ", " + fs.getEmail() + ", localCopy: "
					+ fs.isLocalCopy() + ")");
			settingDao.setEmailForwarding(token, fs);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public ForwardingSettings getEmailForwarding(AccessToken token)
			throws ServerFault {
		try {
			logger.info(LogUtils.prefix(token) + "Setting : getEmailForwarding()");
			return settingDao.getEmailForwarding(token);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public VacationSettings getVacationSettings(AccessToken token)
			throws ServerFault {
		try {
			logger.info(LogUtils.prefix(token) + "Setting : getVacationSettings()");
			return settingDao.getVacationSettings(token);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
}
