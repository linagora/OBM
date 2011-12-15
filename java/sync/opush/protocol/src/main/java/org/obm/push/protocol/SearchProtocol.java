/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
import org.obm.push.exception.activesync.XMLValidationException;
import org.obm.push.protocol.bean.SearchRequest;
import org.obm.push.protocol.bean.SearchResponse;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Strings;

public class SearchProtocol {

	public SearchRequest getRequest(Document document) throws XMLValidationException {
		if (document == null) {
			throw new XMLValidationException("Null Request not supported by Search Command");
		}
		Element documentElement = document.getDocumentElement();
		SearchRequest ret = new SearchRequest();
		StoreName st = StoreName.getValue(DOMUtils.getElementText(documentElement, "Name"));
		if (st == null) {
			throw new XMLValidationException();
		}
		ret.setStoreName(st);
		ret.setQuery( getQuery(documentElement) );
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
