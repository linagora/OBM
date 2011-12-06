package org.obm.push;

import java.security.InvalidParameterException;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.obm.push.bean.User;
import org.obm.push.bean.User.Factory;

public class UserTest {

	@Test
	public void testGetLoginAtDomainWithArrobase() {
		String text = "login@domain";
		User loginAtDomain = Factory.create().createUser(text, "email@domain");
		String result = loginAtDomain.getLoginAtDomain();
		Assertions.assertThat(result).isEqualTo(text);
	}

	@Test(expected=InvalidParameterException.class)
	public void testGetLoginAtDomainWithTwoArrobases() {
		String text = "login@domain@error";
		User loginAtDomain = Factory.create().createUser(text, "email@domain");
		loginAtDomain.getLoginAtDomain();
	}
	
	@Test(expected=InvalidParameterException.class)
	public void testGetLoginAtDomainWithNoDomain() {
		String text = "login";
		User loginAtDomain = Factory.create().createUser(text, "email@domain");
		loginAtDomain.getLoginAtDomain();
	}
	
	@Test
	public void testGetLoginAtDomainWithSlashes() {
		String text = "domain\\login";
		User loginAtDomain = Factory.create().createUser(text, "email@domain");
		String result = loginAtDomain.getLoginAtDomain();
		Assertions.assertThat(result).isEqualTo("login@domain");
	}
	
	@Test(expected=InvalidParameterException.class)
	public void testGetLoginAtDomainWithSlashesAndArrobase() {
		String text = "domain\\login@domain2";
		User loginAtDomain = Factory.create().createUser(text, "email@domain");
		loginAtDomain.getLoginAtDomain();
	}
	
	@Test(expected=InvalidParameterException.class)
	public void testGetLoginAtDomainWithArrobaseAndSlashes() {
		String text = "doma@in\\login";
		User loginAtDomain = Factory.create().createUser(text, "email@domain");
		loginAtDomain.getLoginAtDomain();
	}
}
