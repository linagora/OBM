package org.obm.opush;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.Collection;
import java.util.Properties;

import org.obm.opush.SingleUserFixture.OpushUser;
import org.obm.push.bean.Device;
import org.obm.push.exception.DaoException;
import org.obm.push.store.DeviceDao;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.client.login.LoginService;

public class IntegrationUserAccessUtils {

	public static void mockUsersAccess(ClassToInstanceAgregateView<Object> classToInstanceMap,
			Collection<OpushUser> users) throws DaoException {
		LoginService loginService = classToInstanceMap.get(LoginService.class);
		expectUserLoginFromOpush(loginService, users);
		
		DeviceDao deviceDao = classToInstanceMap.get(DeviceDao.class);
		expectUserDeviceAccess(deviceDao, users);
	}
	
	public static void expectUserLoginFromOpush(LoginService loginService, Collection<OpushUser> users) {
		for (OpushUser user : users) {
			expectUserLoginFromOpush(loginService, user);
		}
	}
	
	public static void expectUserLoginFromOpush(LoginService loginService, OpushUser user) {
		expect(loginService.login(user.user.getLoginAtDomain(), user.password, "o-push")).andReturn(user.accessToken).anyTimes();
		loginService.logout(user.accessToken);
		expectLastCall().anyTimes();
	}


	public static void expectUserDeviceAccess(DeviceDao deviceDao, Collection<OpushUser> users) throws DaoException {
		for (OpushUser user : users) {
			expectUserDeviceAccess(deviceDao, user);
		}
	}
	
	public static void expectUserDeviceAccess(DeviceDao deviceDao, OpushUser user) throws DaoException {
		expect(deviceDao.getDevice(user.user, 
				user.deviceId, 
				user.userAgent))
				.andReturn(
						new Device(user.hashCode(), user.deviceType, user.deviceId, new Properties()))
						.anyTimes();
	}
	
	
}
