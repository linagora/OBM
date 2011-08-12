package org.obm.push.protocol;

import org.obm.push.bean.SearchResult;
import org.obm.push.bean.SearchStatus;
import org.obm.push.bean.StoreName;
import org.obm.push.exception.activesync.XMLValidationException;
import org.obm.push.protocol.bean.SearchRequest;
import org.obm.push.protocol.bean.SearchResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Strings;

public class SearchProtocol {

	public SearchRequest getRequest(Element documentElement) throws XMLValidationException {
		SearchRequest ret = new SearchRequest();
		StoreName st = StoreName.getValue(DOMUtils.getElementText(documentElement, "Name"));
		if (st == null) {
			throw new XMLValidationException();
		}
		ret.setStoreName(st);
		ret.setQuery(DOMUtils.getElementText(documentElement, "Query"));
		String range = DOMUtils.getElementText(documentElement, "Range");
		if (!Strings.isNullOrEmpty(range)) {
			int index = range.indexOf("-");
			if (index < 0) {
				throw new XMLValidationException();
			}
			try {
				Integer rangeLower = Integer.valueOf(range.substring(0, index));
				Integer rangeUpper = Integer.valueOf(range.substring(index + 1, range.length()));
				ret.setRangeLower(rangeLower);
				ret.setRangeUpper(rangeUpper);
			} catch (NumberFormatException e) {
				throw new XMLValidationException(e);
			} catch (IndexOutOfBoundsException e) {
				throw new XMLValidationException(e);
			}
		}
		return ret;
	}

	public Document encodeResponse(SearchResponse response) {
		Document search = DOMUtils.createDoc(null, "Search");
		Element r = search.getDocumentElement();
		DOMUtils.createElementAndText(r, "Status",
				SearchStatus.SUCCESS.asXmlValue());
		Element resp = DOMUtils.createElement(r, "Response");
		Element store = DOMUtils.createElement(resp, "Store");
		DOMUtils.createElementAndText(store, "Status",
				SearchStatus.SUCCESS.asXmlValue());
		if (response.getResults().isEmpty()) {
			DOMUtils.createElement(store, "Result");
		} else {
			for (SearchResult result: response.getResults()) {
				Element er = DOMUtils.createElement(store, "Result");
				Element properties = DOMUtils.createElement(er,	"Properties");
				appendSearchResult(properties, result);
			}
			DOMUtils.createElementAndText(
					store,
					"Range",
					response.getRangeLower()
							+ "-"
							+ (response.getResults().size() > response.getRangeUpper() ? 
									response.getRangeUpper() : response.getResults().size() - 1));
			DOMUtils.createElementAndText(store, "Total", String.valueOf(response.getResults().size()));
		}
		return search;
	}

	private void appendSearchResult(Element properties, SearchResult result) {
	
		if (!Strings.isNullOrEmpty(result.getDisplayName())) {
			DOMUtils.createElementAndText(properties, "GAL:DisplayName",
					result.getDisplayName());
		}
		if (!Strings.isNullOrEmpty(result.getAlias())) {
			DOMUtils.createElementAndText(properties, "GAL:Alias",
					result.getAlias());
		}
		if (!Strings.isNullOrEmpty(result.getFirstName())) {
			DOMUtils.createElementAndText(properties, "GAL:FirstName",
					result.getFirstName());
		}
		if (!Strings.isNullOrEmpty(result.getLastName())) {
			DOMUtils.createElementAndText(properties, "GAL:LastName",
					result.getLastName());
		}
		if (!Strings.isNullOrEmpty(result.getEmailAddress())) {
			DOMUtils.createElementAndText(properties, "GAL:EmailAddress",
					result.getEmailAddress());
		}
		if (!Strings.isNullOrEmpty(result.getCompany())) {
			DOMUtils.createElementAndText(properties, "GAL:Company",
					result.getCompany());
		}
		if (!Strings.isNullOrEmpty(result.getHomePhone())) {
			DOMUtils.createElementAndText(properties, "GAL:HomePhone",
					result.getHomePhone());
		}
		if (!Strings.isNullOrEmpty(result.getMobilePhone())) {
			DOMUtils.createElementAndText(properties, "GAL:MobilePhone",
					result.getMobilePhone());
		}
		if (!Strings.isNullOrEmpty(result.getOffice())) {
			DOMUtils.createElementAndText(properties, "GAL:Office",
					result.getOffice());
		}
		if (!Strings.isNullOrEmpty(result.getPhone())) {
			DOMUtils.createElementAndText(properties, "GAL:Phone",
					result.getPhone());
		}
		if (!Strings.isNullOrEmpty(result.getTitle())) {
			DOMUtils.createElementAndText(properties, "GAL:Title",
					result.getTitle());
		}
	}
	
	public Document buildError(SearchStatus error) {
		Document document = DOMUtils.createDoc(null, "Search");
		Element r = document.getDocumentElement();
		DOMUtils.createElementAndText(r, "Status", SearchStatus.SUCCESS.asXmlValue());
		Element resp = DOMUtils.createElement(r, "Response");
		Element store = DOMUtils.createElement(resp, "Store");
		DOMUtils.createElementAndText(store, "Status", error.asXmlValue());
		return document;
	}
	
}
