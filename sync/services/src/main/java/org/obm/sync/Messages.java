package org.obm.sync;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import com.google.common.base.Charsets;

public class Messages {

	private ResourceBundle bundle;
	private final Locale locale;

	public Messages(Locale locale) {
		this.locale = locale;
		bundle = ResourceBundle.getBundle("Messages", this.locale);
	}
	
	public String newEventTitle(String owner, String title) {
		return getString("NewEventTitle", owner, title);
	}

	private String getString(String key, Object... arguments) {
		String isoEncodedString = bundle.getString(key);
		String string = new String(isoEncodedString.getBytes(Charsets.ISO_8859_1), Charsets.UTF_8);
		MessageFormat format = new MessageFormat(string, locale);
		return format.format(arguments);
	}

	public String canceledEventTitle(String owner, String title) {
		return getString("CanceledEventTitle", owner, title);
	}

	public String updatedEventTitle(String owner, String title) {
		return getString("UpdatedEventTitle", owner, title);
	}
	
}
