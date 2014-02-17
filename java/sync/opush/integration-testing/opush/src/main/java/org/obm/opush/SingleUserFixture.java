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
package org.obm.opush;

import org.obm.push.ProtocolVersion;
import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.DeviceId;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.resource.AccessTokenResource;
import org.obm.push.resource.ResourceCloseOrder;
import org.obm.sync.auth.AccessToken;

import com.google.inject.Inject;

public class SingleUserFixture {

	
	public static class OpushUser {
		public User user;
		public String password;
		public String deviceType;
		public DeviceId deviceId;
		public ProtocolVersion deviceProtocolVersion;
		public String userAgent;
		public AccessToken accessToken;
		public Credentials credentials;
		public Device device;
		public UserDataRequest userDataRequest;
		public String rootCollectionPath;
	}
	
	private final Factory userFactory;
	private final AccessTokenResource.Factory accessTokenResourceFactory;

	public final OpushUser jaures;
	
	@Inject
	public SingleUserFixture(User.Factory userFactory, AccessTokenResource.Factory accessTokenResourceFactory) {
		this.userFactory = userFactory;
		this.accessTokenResourceFactory = accessTokenResourceFactory;
		jaures = buildUser("jaures");
	}

	public OpushUser buildUser(String password) {
		OpushUser user = new OpushUser();
		user.user = userFactory.createUser("jaures@sfio.fr", "jaures@sfio.fr", "Jean Jaures");
		user.password = password;
		user.deviceType = "BellLabsWiredPhone";
		user.deviceId = new DeviceId("blwp123");
		user.deviceProtocolVersion = ProtocolVersion.V121;
		user.userAgent = "BellLabsWiredPhoneAgent";
		user.accessToken = new AccessToken(1, "o-push");
		user.accessToken.setUserDisplayName(user.user.getDisplayName());
		user.accessToken.setUserEmail(user.user.getEmail());
		user.credentials = new Credentials(user.user, user.password);
		user.device = new Device.Factory().create(1, user.deviceType, user.userAgent, user.deviceId, user.deviceProtocolVersion);
		user.userDataRequest = new UserDataRequest(user.credentials, null, user.device);
		user.userDataRequest.putResource(ResourceCloseOrder.ACCESS_TOKEN.name(), 
				accessTokenResourceFactory.create(null, user.accessToken));
		user.rootCollectionPath = "obm:\\\\" + user.user.getLoginAtDomain();
		return user;
	}
}