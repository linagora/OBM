package org.obm.configuration;



import com.google.inject.Singleton;

@Singleton
public class EmailConfiguration extends AbstractConfigurationService{
	
	public static final String IMAP_INBOX_NAME = "INBOX";
	public static final String IMAP_DRAFTS_NAME = "Drafts";
	public static final String IMAP_SENT_NAME = "Sent";
	public static final String IMAP_TRASH_NAME = "Trash";

	private static final int MESSAGE_DEFAULT_MAX_SIZE = 10485760;
	
	private static final String BACKEND_CONF_FILE = "/etc/opush/mail_conf.ini";
	private static final String BACKEND_IMAP_LOGIN_WITH_DOMAIN = "imap.loginWithDomain";
	private static final String BACKEND_IMAP_ACTIVATE_TLS = "imap.activateTLS";
	private static final String BACKEND_MESSAGE_MAX_SIZE = "message.maxSize";
	
	
	
	protected EmailConfiguration() {
		super(BACKEND_CONF_FILE);
	}	
	
	private boolean isOptionEnabled(String option) {
		String entryContent = getStringValue(option);
		return !"false".equals(entryContent);
	}
	
	public boolean activateTls() {
		return isOptionEnabled(BACKEND_IMAP_ACTIVATE_TLS);
	}
	
	public boolean loginWithDomain() {
		return isOptionEnabled(BACKEND_IMAP_LOGIN_WITH_DOMAIN);
	}
	
	public int getMessageMaxSize() {
		return getIntValue(BACKEND_MESSAGE_MAX_SIZE, MESSAGE_DEFAULT_MAX_SIZE);
	}
}
