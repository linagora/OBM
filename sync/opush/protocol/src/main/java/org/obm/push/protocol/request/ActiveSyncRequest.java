package org.obm.push.protocol.request;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

public interface ActiveSyncRequest {
	String getParameter(String key);

	InputStream getInputStream() throws IOException;

	String getHeader(String name);

	HttpServletRequest getHttpServletRequest();
	
	public String getDeviceId();
	public String getDeviceType();
	public String getUserAgent();
	
	public String getCommand();
	
	public String getMsPolicyKey();
	public String getMSASProtocolVersion();
	
}
