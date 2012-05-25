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
package org.obm.push.handler;

import java.util.List;
import java.util.Locale;

import org.obm.configuration.ConfigurationService;
import org.obm.push.bean.UserDataRequest;
import org.obm.push.bean.autodiscover.AutodiscoverProtocolException;
import org.obm.push.bean.autodiscover.AutodiscoverRequest;
import org.obm.push.bean.autodiscover.AutodiscoverResponse;
import org.obm.push.bean.autodiscover.AutodiscoverResponseError;
import org.obm.push.bean.autodiscover.AutodiscoverResponseServer;
import org.obm.push.bean.autodiscover.AutodiscoverResponseUser;
import org.obm.push.bean.autodiscover.AutodiscoverStatus;
import org.obm.push.exception.activesync.NoDocumentException;
import org.obm.push.impl.DOMDumper;
import org.obm.push.impl.Responder;
import org.obm.push.protocol.AutodiscoverProtocol;
import org.w3c.dom.Document;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class AutodiscoverHandler extends XmlRequestHandler {
	
	private final AutodiscoverProtocol protocol;
	private final ConfigurationService configurationService;

	@Inject AutodiscoverHandler(AutodiscoverProtocol autodiscoverProtocol, 
			ConfigurationService configurationService, DOMDumper domDumper) {
		
		super(domDumper);
		this.protocol = autodiscoverProtocol;
		this.configurationService = configurationService; 
	}

	@Override
	protected void process(UserDataRequest udr, Document doc, Responder responder) {
		AutodiscoverRequest autodiscoverRequest = null;
		try {
			autodiscoverRequest = protocol.getRequest(doc);
			AutodiscoverResponse autodiscoverResponse = doTheJob(udr, autodiscoverRequest);
			Document ret = protocol.encodeResponse(autodiscoverResponse);
			sendResponse(responder, ret);
		} catch (NoDocumentException e) {
			logger.error(e.getMessage(), e);
		} catch (AutodiscoverProtocolException e) {
			sendErrorResponse(e, responder, AutodiscoverStatus.SUCCESS, autodiscoverRequest);
		}
	}

	private AutodiscoverResponse doTheJob(UserDataRequest udr, AutodiscoverRequest autodiscoverRequest) {
		String culture = formatCultureParameter( Locale.getDefault() );

		AutodiscoverResponseUser user = buildUserField(udr, autodiscoverRequest);
		List<AutodiscoverResponseServer> actionsServer = buildActionsServer(); 

		return new AutodiscoverResponse(culture, user, null, actionsServer, null, null);
	}

	/**
	 * Specifies the client culture, which is used to localize error messages.
	 *
	 * @param locale
	 * @return locale formated
	 */
	private String formatCultureParameter(Locale locale) {
		return  locale.getLanguage().toLowerCase() + ":" + 
				locale.getCountry().toLowerCase() ;
	}

	private AutodiscoverResponseUser buildUserField(UserDataRequest udr, AutodiscoverRequest autodiscoverRequest) {
		String email = autodiscoverRequest.getEmailAddress();
		String displayName = udr.getUser().getDisplayName();
		return new AutodiscoverResponseUser(email, displayName);
	}
	
	private List<AutodiscoverResponseServer> buildActionsServer() {
		String url = configurationService.getActiveSyncServletUrl();
		return Lists.newArrayList( 
				new AutodiscoverResponseServer("MobileSync", url, url, null), 
				new AutodiscoverResponseServer("CertEnroll", url, null, "CertEnrollTemplate")
				);
	}
	
	private void sendResponse(Responder responder, Document document) {
		responder.sendXMLResponse("Autodiscover", document);
	}

	private void sendErrorResponse(Exception e, Responder responder, AutodiscoverStatus errorStatus, 
			AutodiscoverRequest autodiscoverRequest) {
		
		logger.error(e.getMessage(), e);
		
		AutodiscoverResponseError autodiscoverError = new AutodiscoverResponseError(
				errorStatus, e.getMessage(), e.getMessage(), null);
		
		if (autodiscoverRequest != null) {
			Document document = protocol.encodeErrorResponse(
					autodiscoverError, 
					autodiscoverRequest.getEmailAddress(), 
					formatCultureParameter(Locale.getDefault()));
			
			responder.sendXMLResponse("Autodiscover", document);
		}
	}

}
