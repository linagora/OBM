package fr.aliacom.obm.common.user;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;

import fr.aliacom.obm.common.domain.ObmDomain;


public class ObmUserTest {

	@Test
	public void testGetEmailWithoutDomain() {
		String email = "user1";
		String domainName = "obmsync.test";
		ObmDomain domain = EasyMock.createMock(ObmDomain.class);
		EasyMock.expect(domain.getName()).andReturn(domainName).once();
		EasyMock.replay(domain);
		
		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(domain);
		obmUser.setEmail(email);
		
		String emailAtDomain = obmUser.getEmail();
		Assert.assertEquals(email + "@" +domainName, emailAtDomain);
	}
	
	@Test
	public void testGetEmailWithDomain() {
		String email = "user1@obmsync.test";
		ObmDomain domain = EasyMock.createMock(ObmDomain.class);
		EasyMock.replay(domain);
		
		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(domain);
		obmUser.setEmail(email);
		
		String emailAtDomain = obmUser.getEmail();
		Assert.assertEquals(email, emailAtDomain);
	}
	
}
