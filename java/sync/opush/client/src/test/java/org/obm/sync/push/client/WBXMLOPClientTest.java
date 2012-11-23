package org.obm.sync.push.client;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.bean.DeviceId;


public class WBXMLOPClientTest {
	@Test
	public void testBuildUrl() {
		String url = "url";
		String login = "login";
		DeviceId deviceId = new DeviceId("DeviceId");
		String devType = "devType";
		String cmd = "cmd";
		String buildUrl = WBXMLOPClient.buildUrl(url, login, deviceId, devType, cmd);
		String expected = "url?User=login&DeviceId=DeviceId&DeviceType=devType&Cmd=cmd";
		assertThat(buildUrl).isEqualTo(expected);
	}
	
}
