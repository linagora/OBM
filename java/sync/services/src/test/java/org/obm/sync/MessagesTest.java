package org.obm.sync;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.obm.configuration.ConfigurationServiceImpl;


public class MessagesTest {

	@Before
	public void setLocale() {
		Locale.setDefault(Locale.US);
	}

	@Test
	public void testResourceBundleFr() {
		Messages messages = new Messages(new ConfigurationServiceImpl(), Locale.FRENCH);
		String message = messages.newEventTitle("owner", "title");
		Assert.assertEquals("Nouvel événement de owner : title", message);
	}
	
	@Test
	public void testResourceBundleEn() {
		Messages messages = new Messages(new ConfigurationServiceImpl(), Locale.ENGLISH);
		String message = messages.newEventTitle("owner", "title");
		Assert.assertEquals("New event from owner: title", message);
	}
	
	@Test
	public void testResourceBundleZhNotExist() {
		Messages messages = new Messages(new ConfigurationServiceImpl(), Locale.CHINESE);
		String expectedMessage = "New event from owner: title";
		String message = messages.newEventTitle("owner", "title");
		Assert.assertEquals(expectedMessage, message);
	}
}
