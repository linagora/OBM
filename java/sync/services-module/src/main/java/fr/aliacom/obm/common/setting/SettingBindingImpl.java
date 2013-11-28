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

	private final SettingDao settingDao;

	private final UserService userService;

	@Inject
	protected SettingBindingImpl(SettingDao settingDao, UserService userService) {
		this.settingDao = settingDao;
		this.userService = userService;
	}

	@Override
	@Transactional(readOnly=true)
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
	@Transactional(readOnly=true)
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
	@Transactional(readOnly=true)
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
