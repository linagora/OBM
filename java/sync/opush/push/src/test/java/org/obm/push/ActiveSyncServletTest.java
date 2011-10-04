package org.obm.push;

import java.security.InvalidParameterException;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class ActiveSyncServletTest {

	private ActiveSyncServlet createActiveSyncServlet() {
		return new ActiveSyncServlet(null, null, null, null, null);
	}
	
	@Test
	public void testGetLoginAtDomainWithArrobase() {
		ActiveSyncServlet activeSyncServlet = createActiveSyncServlet();
		String text = "login@domain";
		String result = activeSyncServlet.getLoginAtDomain(text);
		Assertions.assertThat(result).isEqualTo(text);
	}

	@Test(expected=InvalidParameterException.class)
	public void testGetLoginAtDomainWithTwoArrobases() {
		ActiveSyncServlet activeSyncServlet = createActiveSyncServlet();
		String text = "login@domain@error";
		activeSyncServlet.getLoginAtDomain(text);
	}
	
	@Test(expected=InvalidParameterException.class)
	public void testGetLoginAtDomainWithNoDomain() {
		ActiveSyncServlet activeSyncServlet = createActiveSyncServlet();
		String text = "login";
		activeSyncServlet.getLoginAtDomain(text);
	}
	
	@Test
	public void testGetLoginAtDomainWithSlashes() {
		ActiveSyncServlet activeSyncServlet = createActiveSyncServlet();
		String text = "domain\\login";
		String result = activeSyncServlet.getLoginAtDomain(text);
		Assertions.assertThat(result).isEqualTo("login@domain");
	}
	
	@Test(expected=InvalidParameterException.class)
	public void testGetLoginAtDomainWithSlashesAndArrobase() {
		ActiveSyncServlet activeSyncServlet = createActiveSyncServlet();
		String text = "domain\\login@domain2";
		activeSyncServlet.getLoginAtDomain(text);
	}
	
	@Test(expected=InvalidParameterException.class)
	public void testGetLoginAtDomainWithArrobaseAndSlashes() {
		ActiveSyncServlet activeSyncServlet = createActiveSyncServlet();
		String text = "doma@in\\login";
		activeSyncServlet.getLoginAtDomain(text);
	}
}
