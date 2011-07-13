package org.obm.push.search;

import java.util.LinkedList;
import java.util.List;

import javax.naming.ConfigurationException;

import org.obm.configuration.ConfigurationService;
import org.obm.locator.LocatorClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.contacts.ContactConverter;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.client.book.BookClient;
import org.obm.sync.locators.AddressBookLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ObmSearchContact implements ISearchSource {

	private final static Logger logger = LoggerFactory.getLogger(ObmSearchContact.class);
	
	private final LocatorClient locatorClient;
	
	@Inject
	private ObmSearchContact(ConfigurationService configurationService) throws ConfigurationException {
		super();
		locatorClient = new LocatorClient(configurationService.getLocatorUrl());
	}
	
	@Override
	public StoreName getStoreName() {
		return StoreName.GAL;
	}

	@Override
	public List<SearchResult> search(BackendSession bs, String query,
			Integer limit) {
		BookClient bc = getBookClient(bs);
		AccessToken token = bc.login(bs.getLoginAtDomain(), bs.getPassword(),
				"o-push");
		List<SearchResult> ret = new LinkedList<SearchResult>();
		ContactConverter cc = new ContactConverter();
		try {
			List<Contact> contacts = bc.searchContact(token, query, limit);
			for (Contact contact : contacts) {
				ret.add(cc.convertToSearchResult(contact));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return ret;
	}
	
	private BookClient getBookClient(BackendSession bs) {
		AddressBookLocator abl = new AddressBookLocator();
		BookClient bookCli = abl.locate("http://" + locateObmSync(bs.getLoginAtDomain())
				+ ":8080/obm-sync/services");
		return bookCli;
	}
	
	private String locateObmSync(String loginAtDomain) {
		String obmSyncHost = locatorClient.getServiceLocation("sync/obm_sync", loginAtDomain);
		logger.info("Using " + obmSyncHost + " as obm_sync host.");
		return obmSyncHost;
	}
	
}
