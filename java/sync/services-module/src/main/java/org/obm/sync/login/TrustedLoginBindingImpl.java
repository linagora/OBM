/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.login;

import org.obm.annotations.transactional.Transactional;
import org.obm.provisioning.dao.exceptions.DomainNotFoundException;
import org.obm.domain.dao.TrustTokenDao;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.Login;
import org.obm.sync.auth.AuthFault;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.inject.Inject;

import fr.aliacom.obm.common.ObmSyncVersionNotFoundException;
import fr.aliacom.obm.common.session.SessionManagement;
import fr.aliacom.obm.common.trust.TrustToken;
import fr.aliacom.obm.common.user.UserPassword;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;

public class TrustedLoginBindingImpl extends AbstractLoginBackend implements LoginBackend {
	private final TrustTokenDao trustTokenDao;
	private final ObmSyncConfigurationService configurationService;
	

	@Inject
	@VisibleForTesting TrustedLoginBindingImpl(SessionManagement sessionManagement, TrustTokenDao trustTokenDao, ObmSyncConfigurationService configurationService) {
		super(sessionManagement);

		this.trustTokenDao = trustTokenDao;
		this.configurationService = configurationService;
	}

	@Override
	@Transactional(readOnly = true)
	public AccessToken logUserIn(String user, UserPassword token, String origin,
			String clientIP, String remoteIP, String lemonLogin,
			String lemonDomain, boolean isPasswordHashed) throws ObmSyncVersionNotFoundException, DomainNotFoundException, AuthFault {

		TrustToken trustToken = null;
		Login login = Login.builder().login(user).build();
		try {
			trustToken = trustTokenDao.getTrustToken(login.getLogin());
		}
		catch (Exception e) {
			throw Throwables.propagate(e);
		}
		
		if (trustToken == null) {
			throw new AuthFault("No trust token found in database for user '" + user  + "'.");
		}

		if (!trustToken.isTokenValid(token)) {
			throw new AuthFault("Invalid trust token, denying access for user '" + user  + "'.");
		}

		if (trustToken.isExpired(configurationService.trustTokenTimeoutInSeconds())) {
			throw new AuthFault("Trust token is expired, denying access for user '" + user  + "'.");
		}

		return sessionManagement.trustedLogin(user, origin, clientIP, remoteIP, lemonLogin, lemonDomain);
	}

}
