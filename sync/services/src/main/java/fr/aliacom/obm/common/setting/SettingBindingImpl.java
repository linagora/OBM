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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.server.transactional.Transactional;
import org.obm.sync.services.ISetting;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.VacationSettings;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.utils.LogUtils;

@Singleton
public class SettingBindingImpl implements ISetting {

	private static final Log logger = LogFactory.getLog(SettingBindingImpl.class);

	private SettingDao settingDao;

	@Inject
	protected SettingBindingImpl(SettingDao settingDao) {
		this.settingDao = settingDao;
	}

	@Override
	@Transactional
	public Map<String, String> getSettings(AccessToken token)
			throws ServerFault, AuthFault {
		try {
			logger.info(LogUtils.prefix(token) + "Setting : getSettings()");
			return settingDao.getSettings(token);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}

	@Override
	@Transactional
	public void setVacationSettings(AccessToken token, VacationSettings vs)
			throws AuthFault, ServerFault {
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
			throws AuthFault, ServerFault {
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
			throws AuthFault, ServerFault {
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
			throws AuthFault, ServerFault {
		try {
			logger.info(LogUtils.prefix(token) + "Setting : getVacationSettings()");
			return settingDao.getVacationSettings(token);
		} catch (Throwable e) {
			logger.error(LogUtils.prefix(token) + e.getMessage(), e);
			throw new ServerFault(e.getMessage());
		}
	}
}
