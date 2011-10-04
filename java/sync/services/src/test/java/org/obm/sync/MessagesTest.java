package org.obm.sync;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;


public class MessagesTest {

	@Test
	public void testResourceBundleFr() {
		Messages messages = new Messages(Locale.FRENCH);
		String message = messages.newEventTitle("owner", "title");
		Assert.assertEquals("Nouvel événement de owner sur OBM : title", message);
	}
	
	@Test
	public void testResourceBundleEn() {
		Messages messages = new Messages(Locale.ENGLISH);
		String message = messages.newEventTitle("owner", "title");
		Assert.assertEquals("New event created by owner on OBM: title", message);
	}
	
}
