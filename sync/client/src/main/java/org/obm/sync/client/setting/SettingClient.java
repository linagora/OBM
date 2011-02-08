package org.obm.sync.client.setting;

import java.util.Map;

import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.client.impl.AbstractClientImpl;
import org.obm.sync.services.ISetting;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.SettingItemsParser;
import org.obm.sync.setting.VacationSettings;
import org.w3c.dom.Document;

public class SettingClient extends AbstractClientImpl implements ISetting {

	private SettingItemsParser respParser;

	public SettingClient(String backendUrl) {
		super(backendUrl);
		respParser = new SettingItemsParser();
	}

	@Override
	public Map<String, String> getSettings(AccessToken token) throws AuthFault,
			ServerFault {
		Map<String, String> params = initParams(token);
		Document doc = execute("/setting/getSettings", params);
		checkServerError(doc);
		return respParser.parseListSettings(doc);
	}

	@Override
	public void setVacationSettings(AccessToken token, VacationSettings vs)
			throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("enabled", "" + vs.isEnabled());
		if (vs.isEnabled()) {
			if (vs.getStart() != null) {
				params.put("start", "" + vs.getStart().getTime());
			}
			if (vs.getEnd() != null) {
				params.put("end", "" + vs.getEnd().getTime());
			}
			params.put("text", "" + vs.getText());
		}
		Document doc = execute("/setting/setVacationSettings", params);
		checkServerError(doc);
	}

	@Override
	public void setEmailForwarding(AccessToken token, ForwardingSettings fs)
			throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		params.put("enabled", "" + fs.isEnabled());
		if (fs.getEmail() != null && fs.isEnabled()) {
			params.put("email", fs.getEmail());
		}
		params.put("localCopy", "" + fs.isLocalCopy());
		Document doc = execute("/setting/setEmailForwarding", params);
		checkServerError(doc);
	}

	@Override
	public ForwardingSettings getEmailForwarding(AccessToken token)
			throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		Document doc = execute("/setting/getEmailForwarding", params);
		checkServerError(doc);
		return respParser.parseForwarding(doc);
	}

	@Override
	public VacationSettings getVacationSettings(AccessToken token)
			throws AuthFault, ServerFault {
		Map<String, String> params = initParams(token);
		Document doc = execute("/setting/getVacationSettings", params);
		checkServerError(doc);
		return respParser.parseVacation(doc);
	}
}
