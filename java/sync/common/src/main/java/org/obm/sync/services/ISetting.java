package org.obm.sync.services;

import java.util.Map;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.VacationSettings;

public interface ISetting {

	Map<String, String> getSettings(AccessToken token) throws ServerFault;

	VacationSettings getVacationSettings(AccessToken token) throws ServerFault;

	void setVacationSettings(AccessToken token, VacationSettings vs)
			throws ServerFault;

	ForwardingSettings getEmailForwarding(AccessToken token) throws ServerFault;

	void setEmailForwarding(AccessToken token, ForwardingSettings fs)
			throws ServerFault;

}
