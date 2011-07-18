package org.obm.push;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.obm.push.backend.BackendSession;
import org.obm.push.impl.ActiveSyncRequest;
import org.obm.push.impl.Credentials;
import org.obm.push.impl.HintsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SessionService {

	private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
	
	private Map<String, BackendSession> sessions;

	@Inject
	private SessionService() {
		sessions = new HashMap<String, BackendSession>();

	}
	
	public synchronized BackendSession getSession(
			Credentials credentials, String devId, ActiveSyncRequest request) {

		String sessionId = credentials.getLoginAtDomain() + "/" + devId;
		
		BackendSession session = getOrCreateSession(credentials, request, sessionId);
		session.setProtocolVersion(getProtocolVersion(request));
		return session;
	}

	private BackendSession getOrCreateSession(Credentials credentials,
			ActiveSyncRequest request, String sessionId) {
		if (sessions.containsKey(sessionId)) {
			BackendSession bs = sessions.get(sessionId);
			return updateSession(credentials, request, bs);
		} else {
			BackendSession bs = createSession(credentials, request, sessionId);
			sessions.put(sessionId, bs);
			return bs;
		}
	}

	private BackendSession updateSession(Credentials credentials, ActiveSyncRequest r,
			BackendSession bs) {
		bs.setCredentials(credentials);
		logger.info("Existing session = {} | {} ", bs, bs.getLastMonitored());
		bs.setCommand(r.p("Cmd"));
		return bs;
	}

	private BackendSession createSession(Credentials credentials,
			ActiveSyncRequest r, String sessionId) {
		BackendSession bs = new BackendSession(credentials, r.p("DeviceId"),
				r.extractDeviceType(), r.p("Cmd"));
		HintsLoader.addHintsToSession(r, bs);
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
