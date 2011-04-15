package fr.aliacom.obm.common.setting;

import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserSettings;

public interface SettingsService {

	public UserSettings getSettings(ObmUser user);
	
}
