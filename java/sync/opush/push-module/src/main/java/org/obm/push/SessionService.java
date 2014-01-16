/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push;

import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.exception.DaoException;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.service.DeviceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SessionService {

	private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
	private final DeviceService deviceService;
	private final UserDataRequest.Factory userDataRequestFactory;
	
	@Inject
	@VisibleForTesting SessionService(DeviceService deviceService, UserDataRequest.Factory userDataRequestFactory) {
		this.deviceService = deviceService;
		this.userDataRequestFactory = userDataRequestFactory;
	}
	
	public UserDataRequest getSession(
			Credentials credentials, DeviceId devId, ActiveSyncRequest request) throws DaoException {

		String sessionId = credentials.getUser().getLoginAtDomain() + "/" + devId;
		return createSession(credentials, request, sessionId);
	}

	private UserDataRequest createSession(Credentials credentials,
			ActiveSyncRequest r, String sessionId) throws DaoException {
		
		String userAgent = r.getUserAgent();
		DeviceId devId = r.getDeviceId();
		
		Device device = deviceService.getDevice(credentials.getUser(), devId, userAgent, getProtocolVersion(r));
		
		UserDataRequest udr = userDataRequestFactory.createUserDataRequest(credentials, 
				r.getCommand(), device);
		
		logger.debug("New session = {}", sessionId);
		return udr;
	}

	private ProtocolVersion getProtocolVersion(ActiveSyncRequest request) {
		final String proto = request.getMSASProtocolVersion();
		if (proto != null) {
			try {
				ProtocolVersion protocolVersion = ProtocolVersion.fromSpecificationValue(proto);
				logger.debug("Client supports protocol = {}", protocolVersion);
				return protocolVersion;
			} catch (IllegalArgumentException nfe) {
				logger.warn("invalid MS-ASProtocolVersion = {}", proto);
			}
		}
		return ProtocolVersion.V121;
	}

}
