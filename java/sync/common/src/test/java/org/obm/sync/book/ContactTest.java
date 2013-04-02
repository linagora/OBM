package org.obm.sync.book;

import static org.fest.assertions.api.Assertions.assertThat;
import org.obm.sync.base.EmailAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class ContactTest {
	private Contact aContact;

	@Before
	public void setUp() {
		aContact = new Contact();
	}

	@Test
	public void testHasInvalidEmail() {
		aContact.addEmail("test", EmailAddress.loginAtDomain("emai\"l@foo.fr"));
		aContact.addEmail("test2", EmailAddress.loginAtDomain("bar@foo.fr"));
		assertThat(aContact.hasInvalidEmail()).isTrue();
	}

	@Test
	public void testHasOnlyValidEmail() {
		aContact.addEmail("test", EmailAddress.loginAtDomain("email@foo.fr"));
		assertThat(aContact.hasInvalidEmail()).isFalse();
	}
}
