package org.obm.push;

import java.math.BigDecimal;

import org.obm.push.Device.Factory;
import org.obm.push.backend.BackendSession;
import org.obm.push.impl.ActiveSyncRequest;
import org.obm.push.impl.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SessionService {

	private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
	private final Factory deviceFactory;
	
	@Inject
	private SessionService(Device.Factory deviceFactory) {
		this.deviceFactory = deviceFactory;
	}
	
	public BackendSession getSession(
			Credentials credentials, String devId, ActiveSyncRequest request) {

		String sessionId = credentials.getLoginAtDomain() + "/" + devId;
		return createSession(credentials, request, sessionId);
	}

	private BackendSession createSession(Credentials credentials,
			ActiveSyncRequest r, String sessionId) {
		
		String userAgent = r.getHeader("User-Agent");
		String devType = r.extractDeviceType();
		BackendSession bs = new BackendSession(credentials, r.p("DeviceId"),
				r.p("Cmd"), deviceFactory.create(devType, userAgent), getProtocolVersion(r));
		
		logger.info("New session = {}", sessionId);
		return bs;
	}

	private BigDecimal getProtocolVersion(ActiveSyncRequest request) {
		final String proto = request.p("MS-ASProtocolVersion");
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
