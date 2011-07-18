package org.obm.push.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

public interface ActiveSyncRequest {
	String getParameter(String key);

	InputStream getInputStream() throws IOException;

	String getHeader(String name);

	HttpServletRequest getHttpServletRequest();
	
	/**
	 * Parameters can be in query string or in header, whether a base64 query
	 * string is used.
	 */
	String p(String name);
	
	public String extractDeviceType();
}
