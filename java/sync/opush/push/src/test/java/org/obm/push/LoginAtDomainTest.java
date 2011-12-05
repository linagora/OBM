package org.obm.push;

import java.security.InvalidParameterException;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.obm.push.bean.LoginAtDomain;

public class LoginAtDomainTest {

	@Test
	public void testGetLoginAtDomainWithArrobase() {
		String text = "login@domain";
		LoginAtDomain loginAtDomain = new LoginAtDomain(text);
		String result = loginAtDomain.getLoginAtDomain();
		Assertions.assertThat(result).isEqualTo(text);
	}

	@Test(expected=InvalidParameterException.class)
	public void testGetLoginAtDomainWithTwoArrobases() {
		String text = "login@domain@error";
		LoginAtDomain loginAtDomain = new LoginAtDomain(text);
		loginAtDomain.getLoginAtDomain();
	}
	
	@Test(expected=InvalidParameterException.class)
	public void testGetLoginAtDomainWithNoDomain() {
		String text = "login";
		LoginAtDomain loginAtDomain = new LoginAtDomain(text);
		loginAtDomain.getLoginAtDomain();
	}
	
	@Test
	public void testGetLoginAtDomainWithSlashes() {
		String text = "domain\\login";
		LoginAtDomain loginAtDomain = new LoginAtDomain(text);
		String result = loginAtDomain.getLoginAtDomain();
		Assertions.assertThat(result).isEqualTo("login@domain");
	}
	
	@Test(expected=InvalidParameterException.class)
	public void testGetLoginAtDomainWithSlashesAndArrobase() {
		String text = "domain\\login@domain2";
		LoginAtDomain loginAtDomain = new LoginAtDomain(text);
		loginAtDomain.getLoginAtDomain();
	}
	
	@Test(expected=InvalidParameterException.class)
	public void testGetLoginAtDomainWithArrobaseAndSlashes() {
		String text = "doma@in\\login";
		LoginAtDomain loginAtDomain = new LoginAtDomain(text);
		loginAtDomain.getLoginAtDomain();
	}
}
