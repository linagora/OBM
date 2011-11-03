package org.obm.sync;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import org.obm.configuration.ObmConfigurationService;

import com.google.common.base.Charsets;

public class Messages {

	private ResourceBundle bundle;

	public Messages(ObmConfigurationService configurationservice, Locale locale) {
		bundle = configurationservice.getResourceBundle(locale);
	}
	
	public String newEventTitle(String owner, String title) {
		return getString("NewEventTitle", owner, title);
	}

	private String getString(String key, Object... arguments) {
		String isoEncodedString = bundle.getString(key);
		String string = new String(isoEncodedString.getBytes(Charsets.ISO_8859_1), Charsets.UTF_8);
		MessageFormat format = new MessageFormat(string, bundle.getLocale());
		return format.format(arguments);
	}

	public String canceledEventTitle(String owner, String title) {
		return getString("CanceledEventTitle", owner, title);
	}

	public String updatedEventTitle(String owner, String title) {
		return getString("UpdatedEventTitle", owner, title);
	}

	public String updateParticipationStateTitle(String title) {
		return getString("UpdateParticipationStateTitle", title);
	}

	public String participationStateAccepted() {
		return getString("ParticipationStateAccepted");
	}
	
	public String participationStateDeclined() {
		return getString("ParticipationStateDeclined");
	}

	public String connectorVersionErrorTitle() {
		return getString("ConnectorVersionErrorTitle");
	}

	
}
