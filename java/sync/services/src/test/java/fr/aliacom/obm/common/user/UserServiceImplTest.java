package fr.aliacom.obm.common.user;

import org.easymock.EasyMock;
import static org.easymock.EasyMock.expect;
import org.junit.Test;

import fr.aliacom.obm.common.FindException;
import fr.aliacom.obm.common.domain.DomainService;
import fr.aliacom.obm.common.domain.ObmDomain;


public class UserServiceImplTest {

	@Test
	public void getUserFromCalendarTest() throws FindException {
		String userEmail = "User@domain";
		String domainName = "domain";
		ObmDomain obmDomain = new ObmDomain();
		ObmUser obmUser = new ObmUser();
		obmUser.setEmail(userEmail);

		DomainService domainService = EasyMock.createMock(DomainService.class);
		expect(domainService.findDomainByName(domainName)).andReturn(obmDomain).once();

		UserDao userDao = EasyMock.createMock(UserDao.class);
		// Validate that the login of the user extract from the email is set to lowercase
		expect(userDao.findUserByLogin("user", obmDomain)).andReturn(obmUser).once();
		
		EasyMock.replay(domainService, userDao);

		UserServiceImpl userServiceImpl = new UserServiceImpl(domainService, userDao);
		userServiceImpl.getUserFromCalendar(userEmail, domainName);
	}
}
