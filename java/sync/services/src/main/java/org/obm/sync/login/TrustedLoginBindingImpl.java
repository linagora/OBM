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
	@Transactional
	public AccessToken logUserIn(String user, String password, String origin, String clientIP, String remoteIP, String lemonLogin, String lemonDomain, boolean isPasswordHashed)
			throws ObmSyncVersionNotFoundException {

		TrustToken trustToken = trustTokenDao.getTrustToken();

		// No trust token in database. This shouldn't happen
		if (trustToken == null) {
			logger.warn("No trust token in database, denying access for user '{}'", user);
			trustTokenDao.updateTrustToken();

			return null;
		}

		// In a trusted login, the password contains the trust token to verify
		if (!trustToken.isTokenValid(password)) {
			logger.warn("Invalid trust token, denying access for user '{}'.", user);

			return null;
		}

		AccessToken accessToken = sessionManagement.trustedLogin(user, origin, clientIP, remoteIP, lemonLogin, lemonDomain);

		// Update trust token in the database if needed
		if (trustToken.isExpired(configurationService.trustTokenTimeoutInSeconds())) {
			trustTokenDao.updateTrustToken();
		}

		return accessToken;
	}

}
