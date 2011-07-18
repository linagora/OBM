package org.obm.push;

import java.util.HashMap;
import java.util.Map;

import org.obm.push.backend.BackendSession;
import org.obm.push.impl.ActiveSyncRequest;
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
	
	public synchronized BackendSession getSession(String loginAtDomain, String password,
			String devId, ActiveSyncRequest r) {

		String sessionId = loginAtDomain + "/" + devId;

		if (sessions.containsKey(sessionId)) {
			BackendSession bs = sessions.get(sessionId);
			return updateSession(password, r, bs);
		} else {
			BackendSession bs = createSession(loginAtDomain, password, r, sessionId);
			sessions.put(sessionId, bs);
			return bs;
		}
	}

	private BackendSession updateSession(String password, ActiveSyncRequest r,
			BackendSession bs) {
		bs.setPassword(password);
		logger.info("Existing session = {} | {} ", bs, bs.getLastMonitored());
		bs.setCommand(r.p("Cmd"));
		return bs;
	}

	private BackendSession createSession(String loginAtDomain, String password,
			ActiveSyncRequest r, String sessionId) {
		BackendSession bs = new BackendSession(loginAtDomain, password, r.p("DeviceId"),
				r.extractDeviceType(), r.p("Cmd"));
		HintsLoader.addHintsToSession(r, bs);
		logger.info("New session = {}", sessionId);
		return bs;
	}

}
