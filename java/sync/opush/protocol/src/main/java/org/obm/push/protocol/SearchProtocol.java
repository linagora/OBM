/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.protocol;

import org.obm.push.bean.SearchResult;
import org.obm.push.bean.SearchStatus;
import org.obm.push.bean.StoreName;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.exception.activesync.XMLValidationException;
import org.obm.push.protocol.bean.SearchRequest;
import org.obm.push.protocol.bean.SearchResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Strings;

public class SearchProtocol implements ActiveSyncProtocol<SearchRequest, SearchResponse> {

	public SearchRequest decodeRequest(Document document) throws XMLValidationException {
		if (document == null) {
			throw new NoDocumentException("Document of Search request is null.");
		}
		
		SearchRequest.Builder searchRequestBuilder = SearchRequest.builder();
		Element documentElement = document.getDocumentElement();
		StoreName st = StoreName.fromSpecificationValue(DOMUtils.getElementText(documentElement, "Name"));
		if (st == null) {
			throw new XMLValidationException();
		}
		searchRequestBuilder.storeName(st);
		searchRequestBuilder.query(getQuery(documentElement));
		
		String range = DOMUtils.getElementText(documentElement, "Range");
		if (!Strings.isNullOrEmpty(range)) {
			int index = range.indexOf("-");
			if (index < 0) {
				throw new XMLValidationException();
			}
			try {
				searchRequestBuilder.rangeLower(Integer.valueOf(range.substring(0, index)));
				searchRequestBuilder.rangeUpper(Integer.valueOf(range.substring(index + 1, range.length())));
			} catch (NumberFormatException e) {
				throw new XMLValidationException(e);
			} catch (IndexOutOfBoundsException e) {
				throw new XMLValidationException(e);
			}
		}
		
		return searchRequestBuilder
			.build();
	}

	private String getQuery(Element documentElement) {
		Element query = DOMUtils.getUniqueElement(documentElement, "Query");
		if (query != null) {
			String freeText = DOMUtils.getElementText(query, "FreeText");
			if (freeText == null) {
				freeText = DOMUtils.getElementText(documentElement, "Query");
			}
			if (freeText != null) {
				return freeText;
			}
		}
		return "";
	}

	@Override
	public SearchResponse decodeResponse(Document doc) throws NoDocumentException {
		if (doc == null) {
			throw new NoDocumentException("Document of Search response is null.");
		}

		Element root= doc.getDocumentElement();

		Element response = DOMUtils.getUniqueElement(root, "Response");
		Element store = DOMUtils.getUniqueElement(response, "Store");

		SearchResponse.Builder searchResponseBuilder = SearchResponse.builder();
		NodeList results = store.getElementsByTagName("Result");
		for (int i = 0; i < results.getLength(); i++) {
			Element result = (Element) results.item(i);
			
			Element properties = DOMUtils.getUniqueElement(result, "Properties");
			
			searchResponseBuilder.add(serializeSearchResult(properties));
		}
		
		String range = DOMUtils.getElementText(store, "Range");
		int separatorIndex = range.indexOf('-');
		searchResponseBuilder.rangeLower(Integer.valueOf(range.substring(0, separatorIndex)));
		searchResponseBuilder.rangeUpper(Integer.valueOf(range.substring(separatorIndex + 1)));
		
		return searchResponseBuilder
			.build();
	}
	
	private SearchResult serializeSearchResult(Element properties) {
		return SearchResult.builder()
				.displayName(DOMUtils.getElementText(properties, "GAL:DisplayName"))
				.alias(DOMUtils.getElementText(properties, "GAL:Alias"))
				.firstName(DOMUtils.getElementText(properties, "GAL:FirstName"))
				.lastName(DOMUtils.getElementText(properties, "GAL:LastName"))
				.emailAddress(DOMUtils.getElementText(properties, "GAL:EmailAddress"))
				.company(DOMUtils.getElementText(properties, "GAL:Company"))
				.homePhone(DOMUtils.getElementText(properties, "GAL:HomePhone"))
				.mobilePhone(DOMUtils.getElementText(properties, "GAL:MobilePhone"))
				.office(DOMUtils.getElementText(properties, "GAL:Office"))
				.phone(DOMUtils.getElementText(properties, "GAL:Phone"))
				.title(DOMUtils.getElementText(properties, "GAL:Title"))
				.build();
	}

	@Override
	public Document encodeResponse(SearchResponse response) {
		Document search = DOMUtils.createDoc(null, "Search");
		Element r = search.getDocumentElement();
		r.setAttribute("xmlns:GAL", "GAL");
		
		DOMUtils.createElementAndText(r, "Status",
				SearchStatus.SUCCESS.asSpecificationValue());
		Element resp = DOMUtils.createElement(r, "Response");
		Element store = DOMUtils.createElement(resp, "Store");
		DOMUtils.createElementAndText(store, "Status",
				SearchStatus.SUCCESS.asSpecificationValue());
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

	@Override
	public Document encodeRequest(SearchRequest searchRequest) {
		Document reply = DOMUtils.createDoc(null, "Search");
		Element root = reply.getDocumentElement();
		root.setAttribute("xmlns:GAL", "GAL");
		
		DOMUtils.createElementAndText(root, "Name", searchRequest.getStoreName().asSpecificationValue());
		
		String query = searchRequest.getQuery();
		if (!Strings.isNullOrEmpty(query)){
			Element querye = DOMUtils.createElement(root, "Query");
			DOMUtils.createElementAndText(querye, "FreeText", query);
		}
		
		String range = searchRequest.getRangeLower() + "-" + searchRequest.getRangeUpper();
		DOMUtils.createElementAndText(root, "Range", range);
		
		return reply;
	}
	
	public Document buildError(SearchStatus error) {
		Document document = DOMUtils.createDoc(null, "Search");
		Element r = document.getDocumentElement();
		DOMUtils.createElementAndText(r, "Status", SearchStatus.SUCCESS.asSpecificationValue());
		Element resp = DOMUtils.createElement(r, "Response");
		Element store = DOMUtils.createElement(resp, "Store");
		DOMUtils.createElementAndText(store, "Status", error.asSpecificationValue());
		return document;
	}
}
