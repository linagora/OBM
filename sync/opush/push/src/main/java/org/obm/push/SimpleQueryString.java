package org.obm.push;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.obm.push.impl.ActiveSyncRequest;

public class SimpleQueryString implements ActiveSyncRequest{

	private HttpServletRequest request;
	
	public SimpleQueryString(HttpServletRequest r) {
		this.request = r;
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

}
