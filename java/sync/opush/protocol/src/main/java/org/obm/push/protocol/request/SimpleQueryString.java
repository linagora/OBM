package org.obm.push.protocol.request;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;


public class SimpleQueryString extends AbstractActiveSyncRequest implements ActiveSyncRequest {

	public SimpleQueryString(HttpServletRequest r) {
		super(r);
	}

	@Override
	public String getParameter(String key) {
		return request.getParameter(key);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return request.getInputStream();
	}

	@Override
	public String getHeader(String name) {
		return request.getHeader(name);
	}

	@Override
	public HttpServletRequest getHttpServletRequest() {
		return request;
	}

	@Override
	public String getDeviceId() {
		return p("DeviceId");
	}

	@Override
	public String getDeviceType() {
		String deviceType = p("DeviceType");
		if (deviceType.startsWith("IMEI")) {
			return p("User-Agent");
		}
		return deviceType;
	}
	
	@Override
	public String getCommand() {
		return p("Cmd");
	}

}
