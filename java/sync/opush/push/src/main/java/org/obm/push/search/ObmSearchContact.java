package org.obm.push.search;

import java.util.LinkedList;
import java.util.List;

import org.obm.push.bean.BackendSession;
import org.obm.push.bean.SearchResult;
import org.obm.push.bean.StoreName;
import org.obm.push.contacts.ContactConverter;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.auth.ServerFault;
import org.obm.sync.book.Contact;
import org.obm.sync.client.book.BookClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ObmSearchContact implements ISearchSource {

	private final static Logger logger = LoggerFactory.getLogger(ObmSearchContact.class);
	private final BookClient bookClient;
	
	@Inject
	private ObmSearchContact(BookClient bookClient) {
		super();
		this.bookClient = bookClient;
	}
	
	@Override
	public StoreName getStoreName() {
		return StoreName.GAL;
	}

	@Override
	public List<SearchResult> search(BackendSession bs, String query, Integer limit) {
		BookClient bc = getBookClient();
		AccessToken token = bc.login(bs.getLoginAtDomain().getLoginAtDomain(), bs.getPassword(), "o-push");
		List<SearchResult> ret = new LinkedList<SearchResult>();
		ContactConverter cc = new ContactConverter();
		try {
			List<Contact> contacts = bc.searchContact(token, query, limit);
			for (Contact contact: contacts) {
				ret.add(cc.convertToSearchResult(contact));
			}
		} catch (ServerFault e) {
			logger.error(e.getMessage(), e);
		} finally {
			bc.logout(token);
		}
		return ret;
	}
	
	private BookClient getBookClient() {
		return bookClient;
	}
	
}
