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

import java.util.List;

import org.obm.push.bean.autodiscover.AutodiscoverProtocolException;
import org.obm.push.bean.autodiscover.AutodiscoverRequest;
import org.obm.push.bean.autodiscover.AutodiscoverResponse;
import org.obm.push.bean.autodiscover.AutodiscoverResponseError;
import org.obm.push.bean.autodiscover.AutodiscoverResponseServer;
import org.obm.push.bean.autodiscover.AutodiscoverResponseUser;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Strings;

public class AutodiscoverProtocol implements ActiveSyncProtocol<AutodiscoverRequest, AutodiscoverResponse> {

	@Override
	public AutodiscoverRequest decodeRequest(Document document) throws NoDocumentException {
		if (document == null) {
			throw new NoDocumentException("Document of Autodiscover request is null.");
		}
		
		Element root = document.getDocumentElement();
		
		Element emailAddressElement = DOMUtils.getUniqueElement(root, "EMailAddress");
		Element acceptableResponseSchElement = DOMUtils.getUniqueElement(root, "AcceptableResponseSchema");
		
		return new AutodiscoverRequest( emailAddressElement.getTextContent(),
										acceptableResponseSchElement.getTextContent());
	}

	@Override
	public Document encodeResponse(AutodiscoverResponse autodiscoverResponse) throws AutodiscoverProtocolException {
		Document autodiscover = DOMUtils.createDoc(null, "Autodiscover");
		
		Element response = createAutodiscoverResponseElement(autodiscover);
		
		createAutodiscoverResponseCultureElement(response, autodiscoverResponse);
		createAutodiscoverResponseUserElement(response, autodiscoverResponse.getResponseUser());
		createAutodiscoverResponseActionElement(response, autodiscoverResponse);
		createAutodiscoverResponseErrorElement(response, autodiscoverResponse.getResponseError());
		
		return autodiscover;
	}

	public Document encodeErrorResponse(AutodiscoverResponseError error, String userEmail, String culture) {
		Document autodiscover = DOMUtils.createDoc(null, "Autodiscover");
		Element response = createAutodiscoverResponseElement(autodiscover);
		
		createCultureElementAndText(response, culture);
		Element userElement = DOMUtils.createElement(response, "User");
		createEMailAddressElementAndText(userElement, userEmail);
		
		Element actionElement = DOMUtils.createElement(response, "Action");
		createAutodiscoverResponseErrorActionElement(actionElement, error);
		
		return autodiscover;
	}

	private Element createAutodiscoverResponseElement(Document autodiscover) {
		return DOMUtils.createElement(autodiscover.getDocumentElement(), "Response");
	}
	
	private void createAutodiscoverResponseCultureElement(Element response, AutodiscoverResponse autodiscoverResponse) {
		String responseCulture = autodiscoverResponse.getResponseCulture();
		if (responseCulture != null) {
			createCultureElementAndText(response, responseCulture);
		}
	}
	
	
	private void createAutodiscoverResponseUserElement(Element response, AutodiscoverResponseUser user) 
			throws AutodiscoverProtocolException {
		
		if (user != null && !Strings.isNullOrEmpty(user.getEmailAddress())) {
			Element userElement = DOMUtils.createElement(response, "User");
			if (!Strings.isNullOrEmpty(user.getDisplayName())) {
				DOMUtils.createElementAndText(userElement, "DisplayName", user.getDisplayName());
			}
			createEMailAddressElementAndText(userElement, user.getEmailAddress());
		} else {
			throw new AutodiscoverProtocolException("The user email address element is a required");
		}
	}
	
	private void createAutodiscoverResponseActionElement(Element response, AutodiscoverResponse autodiscoverResponse) {
		String actionRedirect = autodiscoverResponse.getActionRedirect();
		List<AutodiscoverResponseServer> listActionServer = autodiscoverResponse.getListActionServer();
		AutodiscoverResponseError actionError = autodiscoverResponse.getActionError();

		if (actionRedirect != null || listActionServer != null || actionError != null) {
			Element actionElement = DOMUtils.createElement(response, "Action");
			createAutodiscoverResponseRedirectElement(actionElement, actionRedirect);
			createAutodiscoverResponseServerSettingsElement(actionElement, listActionServer);
			createAutodiscoverResponseErrorActionElement(actionElement, actionError);
		}
		
	}

	private void createAutodiscoverResponseRedirectElement(Element actionElement, String actionRedirect) {
		if (!Strings.isNullOrEmpty(actionRedirect)) {
			DOMUtils.createElementAndText(actionElement, "Redirect", actionRedirect);
		}
	}
	
	private void createAutodiscoverResponseServerSettingsElement(Element actionElement, List<AutodiscoverResponseServer> listActionServer) {
		if (listActionServer != null && !listActionServer.isEmpty()) {
			Element settingsElement = DOMUtils.createElement(actionElement, "Settings");
			
			for (AutodiscoverResponseServer actionServer: listActionServer) {
				Element serverElement = DOMUtils.createElement(settingsElement, "Server");
				
				DOMUtils.createElementAndText(serverElement, "Type", actionServer.getType());
				DOMUtils.createElementAndText(serverElement, "Url", actionServer.getUrl());
				if (actionServer.getName() != null) {
					DOMUtils.createElementAndText(serverElement, "Name", actionServer.getName());
				}
				if (actionServer.getServerData() != null) {
					DOMUtils.createElementAndText(serverElement, "ServerData", actionServer.getServerData());
				}
			}
		}
	}
	
	private void createAutodiscoverResponseErrorActionElement(Element actionElement, AutodiscoverResponseError actionError) {
		if (actionError != null) {
			Element errorElement = DOMUtils.createElement(actionElement, "Error");
			DOMUtils.createElementAndText(errorElement, "Status", actionError.getStatus().asXmlValue());
			DOMUtils.createElementAndText(errorElement, "Message", actionError.getMessage());
			DOMUtils.createElementAndText(errorElement, "DebugData", actionError.getDebugData());
		}
	}
	
	private void createAutodiscoverResponseErrorElement(Element response, AutodiscoverResponseError responseError) {
		if (responseError != null) {
			Element errorElement = DOMUtils.createElement(response, "Error");
			DOMUtils.createElementAndText(errorElement, "ErrorCode", String.valueOf(responseError.getErrorCode()));
			DOMUtils.createElementAndText(errorElement, "Message", responseError.getMessage());
			DOMUtils.createElementAndText(errorElement, "DebugData", responseError.getDebugData());
		}
	}

	private void createEMailAddressElementAndText(Element element, String value) {
		createElementAndText(element, "EMailAddress", value);
	}
	
	private void createCultureElementAndText(Element element, String value) {
		createElementAndText(element, "Culture", value);
	}
	
	private void createElementAndText(Element element, String elementName, String value) {
		DOMUtils.createElementAndText(element, elementName, value);
	}

}
