package org.obm.sync.locators;

import org.obm.sync.client.setting.SettingClient;
import org.obm.sync.services.ISetting;

/**
 * Creates a client for the {@link ISetting} sync service
 * 
 * @author adrien
 * 
 */
public class SettingLocator {

	/**
	 * @param obmSyncServicesUrl
	 *            https://obm.buffy.kvm/obm-sync/services
	 * @return a setting client
	 */
	public static SettingClient locate(String obmSyncServicesUrl) {
		SettingClient ret = new SettingClient(obmSyncServicesUrl);
		return ret;
	}

}
