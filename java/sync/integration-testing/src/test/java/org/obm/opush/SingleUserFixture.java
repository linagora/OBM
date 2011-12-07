package org.obm.opush;

import org.obm.push.bean.User;

import com.google.inject.Inject;

public class SingleUserFixture {

	
	public static class OpushUser {
		public User user;
		public String password;
		public String deviceType;
		public String deviceId;
		public String userAgent;
	}
	
	public final OpushUser jaures;
	
	@Inject
	public SingleUserFixture(User.Factory userFactory) {
		jaures = new OpushUser();
		jaures.user = userFactory.createUser("jaures@sfio.fr", null);
		jaures.password = "jaures";
		jaures.deviceType = "BellLabsWiredPhone";
		jaures.deviceId = "blwp123";
		jaures.userAgent = "BellLabsWiredPhoneAgent";
	}
	
}