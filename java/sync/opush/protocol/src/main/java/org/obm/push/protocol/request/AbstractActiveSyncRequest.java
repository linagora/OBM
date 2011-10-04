package org.obm.push.protocol.request;

import javax.servlet.http.HttpServletRequest;


public abstract class AbstractActiveSyncRequest implements ActiveSyncRequest {
	
	protected final HttpServletRequest request;
	
	protected AbstractActiveSyncRequest(HttpServletRequest request) {
		this.request = request;
	}
	
	protected String p(String name) {
		String ret = getParameter(name);
		if (ret == null) {
			ret = getHeader(name);
		}
		return ret;
	}
	
	@Override
	public String getUserAgent() {
		return request.getHeader("User-Agent");
	}

	@Override
	public String getMsPolicyKey() {
		return request.getHeader("X-Ms-PolicyKey");
	}
	
	@Override
	public String getMSASProtocolVersion() {
		return request.getHeader("MS-ASProtocolVersion");
	}
}
