package org.obm.push.mail;

import org.obm.push.utils.IniFile;

import com.google.inject.Singleton;

@Singleton
public class EmailConfiguration {

	public static final String IMAP_INBOX_NAME = "INBOX";
	public static final String IMAP_DRAFTS_NAME = "Drafts";
	public static final String IMAP_SENT_NAME = "Sent";
	public static final String IMAP_TRASH_NAME = "Trash";
	
	private static final String BACKEND_CONF_FILE = "/etc/opush/mail_conf.ini";
	private static final String BACKEND_IMAP_LOGIN_WITH_DOMAIN = "imap.loginWithDomain";
	private static final String BACKEND_IMAP_ACTIVATE_TLS = "imap.activateTLS";
	
	private final IniFile ini;
	
	public EmailConfiguration() {
		ini = new IniFile(BACKEND_CONF_FILE) {
			@Override
			public String getCategory() {
				return null;
			}
		};
	}

	private boolean isOptionEnabled(String option) {
		String entryContent = ini.getData().get(option);
		return !"false".equals(entryContent);
	}
	
	public boolean activateTls() {
		return isOptionEnabled(BACKEND_IMAP_ACTIVATE_TLS);
	}
	
	public boolean loginWithDomain() {
		return isOptionEnabled(BACKEND_IMAP_LOGIN_WITH_DOMAIN);
	}
	
}
