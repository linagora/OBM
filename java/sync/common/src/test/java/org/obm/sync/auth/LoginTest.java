package org.obm.sync.auth;


import org.fest.assertions.api.Assertions;
import org.junit.Test;

public class LoginTest {
	private static final String DOMAIN = "exemple.com";
	private static final String LOGIN = "test";
	private static final String FULL_LOGIN = LOGIN + Login.FULL_LOGIN_SEPARATOR + DOMAIN;
	private Login login;

	@Test
	public void creationByFullLogin() {
		login = new Login(FULL_LOGIN);
		assertLogin(LOGIN, DOMAIN, FULL_LOGIN);
	}

	@Test
	public void creationByLoginParts() {
		login = new Login(LOGIN, DOMAIN);
		assertLogin(LOGIN, DOMAIN, FULL_LOGIN);
	}

	@Test
	public void nullDomain() {
		login = new Login(LOGIN, null);
		assertLogin(LOGIN, null, LOGIN);
	}
	@Test(expected=NullPointerException.class)
	public void nullFullLogin() {
		login = new Login(null);
	}

	@Test(expected=NullPointerException.class)
	public void nullLogin() {
		login = new Login(null, DOMAIN);
	}


	private void assertLogin(String shortLogin, String domain, String fullLogin) {
		Assertions.assertThat(login.getLogin()).isEqualTo(shortLogin);
		Assertions.assertThat(login.getDomain()).isEqualTo(domain);
		Assertions.assertThat(login.getFullLogin()).isEqualTo(fullLogin);
	}
}
