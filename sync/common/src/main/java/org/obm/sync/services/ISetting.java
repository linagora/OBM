package org.obm.sync.services;

import java.util.Map;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.VacationSettings;

public interface ISetting {

	Map<String, String> getSettings(AccessToken token) throws AuthFault,
			ServerFault;

	VacationSettings getVacationSettings(AccessToken token) throws AuthFault,
			ServerFault;

	void setVacationSettings(AccessToken token, VacationSettings vs)
			throws AuthFault, ServerFault;

	ForwardingSettings getEmailForwarding(AccessToken token) throws AuthFault,
			ServerFault;

	void setEmailForwarding(AccessToken token, ForwardingSettings fs)
			throws AuthFault, ServerFault;

}
