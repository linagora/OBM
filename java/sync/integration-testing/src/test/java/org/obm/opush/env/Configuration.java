package org.obm.opush.env;

import java.util.Locale;
import java.util.ResourceBundle;

public class Configuration {
	
	public static class SyncPerms {
		public String blacklist = "";
		public boolean allowUnkwownDevice = true;
	}
	
	public static class Mail {
		public boolean activateTls = false;
		public boolean loginWithDomain = true;
	}
	
	public ResourceBundle bundle = ResourceBundle.getBundle("Messages", Locale.FRANCE);
	public SyncPerms syncPerms = new SyncPerms();
	public Mail mail = new Mail();
	
}