package org.obm.sync.login;

import org.obm.annotations.transactional.Transactional;
import org.obm.configuration.ConfigurationService;
import org.obm.sync.auth.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import fr.aliacom.obm.common.ObmSyncVersionNotFoundException;
import fr.aliacom.obm.common.session.SessionManagement;
import fr.aliacom.obm.common.trust.TrustToken;
import fr.aliacom.obm.common.trust.TrustTokenDao;

public class TrustedLoginBindingImpl extends LoginBindingImpl {
	private final TrustTokenDao trustTokenDao;
	private final ConfigurationService configurationService;
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Inject
	public TrustedLoginBindingImpl(SessionManagement sessionManagement, TrustTokenDao trustTokenDao, ConfigurationService configurationService) {
		super(sessionManagement);

		this.trustTokenDao = trustTokenDao;
		this.configurationService = configurationService;
	}

	@Override
	@Transactional(readOnly = true)
	public AccessToken logUserIn(String user, String token, String origin,
			String clientIP, String remoteIP, String lemonLogin,
			String lemonDomain, boolean isPasswordHashed) throws ObmSyncVersionNotFoundException {

		TrustToken trustToken = null;
		
		try {
			trustToken = trustTokenDao.getTrustToken(user);
		}
		catch (Exception e) {
			logger.error("Failed to locate trust token in database.", e);
		}
		
		if (trustToken == null) {
			return null;
		}

		if (!trustToken.isTokenValid(token)) {
			logger.warn("Invalid trust token, denying access for user '{}'.", user);

			return null;
		}

		if (trustToken.isExpired(configurationService.trustTokenTimeoutInSeconds())) {
			logger.warn("Trust token is expired, denying access for user '{}'.", user);
			
			return null;
		}

		return sessionManagement.trustedLogin(user, origin, clientIP, remoteIP, lemonLogin, lemonDomain);
	}

}
