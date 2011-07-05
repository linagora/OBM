package org.obm.push.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

public interface ActiveSyncRequest {
	String getParameter(String key);

	InputStream getInputStream() throws IOException;

	String getHeader(String name);

	HttpServletRequest getHttpServletRequest();
}
