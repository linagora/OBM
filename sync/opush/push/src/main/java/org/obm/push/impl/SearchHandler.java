package org.obm.push.impl;

import java.util.LinkedList;
import java.util.List;

import org.obm.annotations.transactional.Transactional;
import org.obm.push.backend.BackendSession;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IContinuation;
import org.obm.push.data.EncoderFactory;
import org.obm.push.exception.XMLValidationException;
import org.obm.push.search.ISearchSource;
import org.obm.push.search.ObmSearchContact;
import org.obm.push.search.SearchItem;
import org.obm.push.search.SearchResult;
import org.obm.push.search.StoreName;
import org.obm.push.search.ldap.BookSource;
import org.obm.push.state.StateMachine;
import org.obm.push.store.ISyncStorage;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.collect.ImmutableMultimap;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SearchHandler extends WbxmlRequestHandler {

	private final ImmutableMultimap<StoreName, ISearchSource> sources;
	
	@Inject
	protected SearchHandler(IBackend backend, EncoderFactory encoderFactory,
			BookSource bookSource, ObmSearchContact obmSearchContact,
			IContentsImporter contentsImporter, ISyncStorage storage,
			IContentsExporter contentsExporter, StateMachine stMachine) {
		
		super(backend, encoderFactory, contentsImporter, storage,
				contentsExporter, stMachine);
		
		this.sources = ImmutableMultimap.of(
				bookSource.getStoreName(), bookSource, 
				obmSearchContact.getStoreName(), obmSearchContact);
	}
	
	@Override
	@Transactional
	public void process(IContinuation continuation, BackendSession bs,
			Document doc, ActiveSyncRequest request, Responder responder) {
		logger.info("process(" + bs.getLoginAtDomain() + "/" + bs.getDevType()
				+ ")");
		try {
			SearchItem searchItem = processSearch(doc.getDocumentElement());

			List<SearchResult> results = search(bs, searchItem.getStoreName(),
					searchItem.getQuery(), 1000);

			Document search = DOMUtils.createDoc(null, "Search");
			Element r = search.getDocumentElement();
			DOMUtils.createElementAndText(r, "Status",
					SearchStatus.SUCCESS.asXmlValue());
			Element resp = DOMUtils.createElement(r, "Response");
			Element store = DOMUtils.createElement(resp, "Store");
			DOMUtils.createElementAndText(store, "Status",
					SearchStatus.SUCCESS.asXmlValue());
			if (results.size() > 0) {
				for (int i = searchItem.getRangeLower(); i <= searchItem
						.getRangeUpper() && i < results.size(); i++) {
					SearchResult result = results.get(i);
					Element er = DOMUtils.createElement(store, "Result");
					Element properties = DOMUtils.createElement(er,
							"Properties");
					appendSearchResult(properties, result);
				}
				DOMUtils.createElementAndText(
						store,
						"Range",
						searchItem.getRangeLower()
								+ "-"
								+ (results.size() > searchItem.getRangeUpper() ? searchItem
										.getRangeUpper() : results.size() - 1));
				DOMUtils.createElementAndText(store, "Total",
						"" + results.size());
			} else {
				DOMUtils.createElement(store, "Result");
			}
			responder.sendResponse("Search", search);

		} catch (XMLValidationException e) {
			sendError(responder, SearchStatus.PROTOCOL_VIOLATION);
		} catch (Exception e) {
			logger.error("Error creating search response", e);
		}
	}

	public void sendError(Responder responder, SearchStatus error) {
		Document search = DOMUtils.createDoc(null, "Search");
		Element r = search.getDocumentElement();
		DOMUtils.createElementAndText(r, "Status",
				SearchStatus.SUCCESS.asXmlValue());
		Element resp = DOMUtils.createElement(r, "Response");
		Element store = DOMUtils.createElement(resp, "Store");
		DOMUtils.createElementAndText(store, "Status", error.asXmlValue());

		try {
			responder.sendResponse("Search", search);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void appendSearchResult(Element properties, SearchResult result) {
		if (ne(result.getDisplayName())) {
			DOMUtils.createElementAndText(properties, "GAL:DisplayName",
					result.getDisplayName());
		}
		if (ne(result.getAlias())) {
			DOMUtils.createElementAndText(properties, "GAL:Alias",
					result.getAlias());
		}
		if (ne(result.getFirstName())) {
			DOMUtils.createElementAndText(properties, "GAL:FirstName",
					result.getFirstName());
		}
		if (ne(result.getLastName())) {
			DOMUtils.createElementAndText(properties, "GAL:LastName",
					result.getLastName());
		}
		if (ne(result.getEmailAddress())) {
			DOMUtils.createElementAndText(properties, "GAL:EmailAddress",
					result.getEmailAddress());
		}

		if (ne(result.getCompany())) {
			DOMUtils.createElementAndText(properties, "GAL:Company",
					result.getCompany());
		}

		if (ne(result.getHomePhone())) {
			DOMUtils.createElementAndText(properties, "GAL:HomePhone",
					result.getHomePhone());
		}

		if (ne(result.getMobilePhone())) {
			DOMUtils.createElementAndText(properties, "GAL:MobilePhone",
					result.getMobilePhone());
		}

		if (ne(result.getOffice())) {
			DOMUtils.createElementAndText(properties, "GAL:Office",
					result.getOffice());
		}

		if (ne(result.getPhone())) {
			DOMUtils.createElementAndText(properties, "GAL:Phone",
					result.getPhone());
		}

		if (ne(result.getTitle())) {
			DOMUtils.createElementAndText(properties, "GAL:Title",
					result.getTitle());
		}

	}

	private SearchItem processSearch(Element documentElement)
			throws XMLValidationException {
		SearchItem ret = new SearchItem();
		StoreName st = StoreName.getValue(DOMUtils.getElementText(
				documentElement, "Name"));
		if (st == null) {
			throw new XMLValidationException();
		}
		ret.setStoreName(st);
		ret.setQuery(DOMUtils.getElementText(documentElement, "Query"));
		String range = DOMUtils.getElementText(documentElement, "Range");
		if (range != null && !"".equals(range)) {
			int index = range.indexOf("-");
			if (index < 0) {
				throw new XMLValidationException();
			}
			try {
				Integer rangeLower = Integer
						.parseInt(range.substring(0, index));
				Integer rangeUpper = Integer.parseInt(range.substring(
						index + 1, range.length()));
				ret.setRangeLower(rangeLower);
				ret.setRangeUpper(rangeUpper);
			} catch (Exception e) {
				throw new XMLValidationException();
			}
		}
		return ret;
	}

	private boolean ne(String value) {
		return value != null && !"".equals(value);
	}

	public List<SearchResult> search(BackendSession bs, StoreName store,
			String query, Integer limit) {
		
		final List<SearchResult> ret = new LinkedList<SearchResult>();
		for (final ISearchSource source: sources.get(store)) {
			ret.addAll(source.search(bs, query, limit));
		}
		return ret;
	}

}
