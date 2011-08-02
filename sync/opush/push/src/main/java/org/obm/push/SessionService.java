package org.obm.push;

import java.math.BigDecimal;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.Device.Factory;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SessionService {

	private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
	private final DeviceDao deviceDao;
	
	@Inject
	private SessionService(DeviceDao deviceDao) {
		this.deviceDao = deviceDao;
	}
	
	public BackendSession getSession(
			Credentials credentials, String devId, ActiveSyncRequest request) {

		String sessionId = credentials.getLoginAtDomain() + "/" + devId;
		return createSession(credentials, request, sessionId);
	}

	private BackendSession createSession(Credentials credentials,
			ActiveSyncRequest r, String sessionId) {
		
		String userAgent = r.getUserAgent();
		String devType = r.getDeviceType();
		String devId = r.getDeviceId();
		
		Device device = deviceDao.getDevice(credentials.getLoginAtDomain(), devId, userAgent);
		
		BackendSession bs = new BackendSession(credentials, 
				r.getCommand(), device, getProtocolVersion(r));
		
		
		logger.info("New session = {}", sessionId);
		return bs;
	}

	private BigDecimal getProtocolVersion(ActiveSyncRequest request) {
		final String proto = request.getMSASProtocolVersion();
		if (proto != null) {
			try {
				BigDecimal protocolVersion = new BigDecimal(proto);
				logger.info("Client supports protocol = {}", protocolVersion);
				return protocolVersion;
			} catch (NumberFormatException nfe) {
				logger.warn("invalid MS-ASProtocolVersion = {}", proto);
			}
		}
		return new BigDecimal("12.1");
	}

}
