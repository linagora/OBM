package org.obm.sync;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.obm.configuration.ObmConfigurationService;


public class MessagesTest {

	@Test
	public void testResourceBundleFr() {
		Messages messages = new Messages(new ObmConfigurationService(), Locale.FRENCH);
		String message = messages.newEventTitle("owner", "title");
		Assert.assertEquals("Nouvel événement de owner sur OBM : title", message);
	}
	
	@Test
	public void testResourceBundleEn() {
		Messages messages = new Messages(new ObmConfigurationService(), Locale.ENGLISH);
		String message = messages.newEventTitle("owner", "title");
		Assert.assertEquals("New event created by owner on OBM: title", message);
	}
	
	@Test
	public void testResourceBundleZhNotExist() {
		Messages messages = new Messages(new ObmConfigurationService(), Locale.CHINESE);
		String message = messages.newEventTitle("owner", "title");
		Assert.assertEquals("Nouvel événement de owner sur OBM : title", message);
	}
	
}
