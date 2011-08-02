package org.obm.push.protocol.request;


public abstract class AbstractActiveSyncRequest implements ActiveSyncRequest {
	
	public String p(String name) {
		String ret = getParameter(name);
		if (ret == null) {
			ret = getHeader(name);
		}
		return ret;
	}
	
	public String extractDeviceType() {
		String deviceType = p("DeviceType");
		if (deviceType.startsWith("IMEI")) {
			return p("User-Agent");
		}
		return deviceType;
	}

}
