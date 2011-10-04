package fr.aliacom.obm.common.user;

import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.google.common.base.Objects;

public class UserSettings {

	private Map<String, String> rawSettings;
	
	public UserSettings(Map<String, String> rawSettings) {
		this.rawSettings = rawSettings;
	}
	
	public Locale locale() {
		String localeAsString = Objects.firstNonNull(rawSettings.get("set_lang"), "en");
		return new Locale(localeAsString);
	}
	
	public boolean expectParticipationEmailNotification() {
		String value = 
			Objects.firstNonNull(rawSettings.get("set_mail_participation"), "yes");
		if (value.equalsIgnoreCase("yes")) {
			return true;
		}
		return false;
	}

	public TimeZone timezone() {
		String timezoneAsString = Objects.firstNonNull(rawSettings.get("set_timezone"), "GMT");
		return TimeZone.getTimeZone(timezoneAsString);
	}
	
}