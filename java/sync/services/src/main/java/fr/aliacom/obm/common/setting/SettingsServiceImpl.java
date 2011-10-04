package fr.aliacom.obm.common.setting;

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserSettings;

@Singleton
public class SettingsServiceImpl implements SettingsService {

	private final SettingDao settingDao;

	@Inject
	private SettingsServiceImpl(SettingDao settingDao) {
		this.settingDao = settingDao;
	}
	
	@Override
	public UserSettings getSettings(ObmUser user) {
		Map<String, String> rawSettings = settingDao.getSettings(user);
		return new UserSettings(rawSettings);
	}

}
