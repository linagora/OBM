package org.obm.push.mail;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.minig.imap.IdleClient;
import org.minig.imap.StoreClient;
import org.obm.configuration.EmailConfiguration;
import org.obm.locator.LocatorClientException;
import org.obm.locator.store.LocatorService;
import org.obm.push.bean.BackendSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.mail.imap.IMAPStore;

public class ImapClientProviderImpl implements ImapClientProvider {

	private static final Logger logger = LoggerFactory.getLogger(ImapClientProviderImpl.class);
	
	private final LocatorService locatorService;
	private final boolean loginWithDomain;
	private final int imapPort;

	
	@Inject
	private ImapClientProviderImpl(EmailConfiguration emailConfiguration, 
			LocatorService locatorService) {
		this.locatorService = locatorService;
		this.loginWithDomain = emailConfiguration.loginWithDomain();
		this.imapPort = emailConfiguration.imapPort();
	}

	
	@Override
	public String locateImap(BackendSession bs) throws LocatorClientException {
		String locateImap = locatorService.
				getServiceLocation("mail/imap_frontend", bs.getUser().getLoginAtDomain());
		logger.info("Using {} as imap host.", locateImap);
		return locateImap;
	}

	@Override
	public StoreClient getImapClient(BackendSession bs) throws LocatorClientException {
		final String imapHost = locateImap(bs);
		final String login = getLogin(bs);
		StoreClient storeClient = new StoreClient(imapHost, imapPort, login, bs.getPassword()); 
		
		logger.debug("Creating storeClient with login {} : " +
				"loginWithDomain = {}", 
				new Object[]{login, loginWithDomain});
		
		return storeClient; 
	}

	private String getLogin(BackendSession bs) {
		String login = bs.getUser().getLoginAtDomain();
		if (!loginWithDomain) {
			int at = login.indexOf("@");
			if (at > 0) {
				login = login.substring(0, at);
			}
		}
		return login;
	}


	@Override
	public IdleClient getImapIdleClient(BackendSession bs)
			throws LocatorClientException {
		String login = getLogin(bs);
		logger.debug("Creating idleClient with login: {}, (useDomain {})", login, loginWithDomain);
		return new IdleClient(locateImap(bs), 143, login, bs.getPassword());
	}


	@Override
	public IMAPStore getJavaxMailImapClient(BackendSession bs) throws MailException {
		try {
			Properties properties = new Properties();
			Session session = Session.getDefaultInstance(properties);
			Store imapStore = session.getStore("imap");
			final String imapHost = locateImap(bs);
			final String login = getLogin(bs);
			imapStore.connect(imapHost, imapPort, login, bs.getPassword());
			return (IMAPStore) imapStore;
		} catch (MessagingException e) {
			throw new MailException(e);
		}
	}
	
}
