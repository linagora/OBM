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
package org.obm.push.spushnik.resources;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.obm.push.ProtocolVersion;
import org.obm.push.bean.DeviceId;
import org.obm.push.spushnik.bean.CheckResult;
import org.obm.push.spushnik.bean.CheckStatus;
import org.obm.push.spushnik.bean.Credentials;
import org.obm.push.spushnik.service.CredentialsService;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.sync.push.client.HttpRequestException;
import org.obm.sync.push.client.OPClient;
import org.obm.sync.push.client.SSLContextFactory;
import org.obm.sync.push.client.WBXMLOPClient;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.inject.Inject;

public abstract class Scenario {
	
	private final static DeviceId DEVICE_ID = new DeviceId("spushnik");
	private final static String DEV_TYPE = "spushnikProbe";
	private final static String USER_AGENT = "spushnikAgent";
	
	@Inject CredentialsService credentialsService;
	
	@POST
	@Produces("application/json")
	public CheckResult run(
			@QueryParam("serviceUrl") String serviceUrl,
			Credentials credentials) {
		
		try {
			credentialsService.validate(credentials);
			
			OPClient client = new WBXMLOPClient(chooseHttpClient(credentials, serviceUrl),
				credentials.getLoginAtDomain(), credentials.getPassword(),
				DEVICE_ID, DEV_TYPE, USER_AGENT, serviceUrl, new WBXMLTools(), ProtocolVersion.V121);
		
			return scenarii(client);
		} catch (Exception e) {
			return handleException(e);
		}
	}

	@VisibleForTesting HttpClient chooseHttpClient(Credentials credentials, String serviceUrl) {
		Preconditions.checkNotNull(credentials);
		Preconditions.checkNotNull(serviceUrl);
		HttpClientBuilder httpClientBuilder = 
				HttpClientBuilder.create()
					.setMaxConnTotal(5)
					.setMaxConnPerRoute(5);
		if (serviceDoesNotNeedSSL(serviceUrl)) {
			return httpClientBuilder.build();
		}
		if (serviceNeedsClientCertificate(credentials)) {
			return httpClientBuilder
					.setSslcontext(SSLContextFactory.create(getPkcs12Stream(credentials), credentials.getPkcs12Password()))
					.build();
		}
		return httpClientBuilder
				.setSslcontext(SSLContextFactory.TRUST_ALL)
				.setHostnameVerifier(SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)
				.build();
	}

	private boolean serviceNeedsClientCertificate(Credentials credentials) {
		return credentials.getPkcs12() != null;
	}

	private boolean serviceDoesNotNeedSSL(String serviceUrl) {
		return !serviceUrl.startsWith("https");
	}

	private InputStream getPkcs12Stream(Credentials credentials) {
		return new ByteArrayInputStream(credentials.getPkcs12());
	}
	
	protected abstract CheckResult scenarii(OPClient client) throws Exception;
	
	protected CheckResult buildOK() {
		return CheckResult.builder().checkStatus(CheckStatus.OK).build();
	}
	
	protected CheckResult buildWarning(String message) {
		return CheckResult.builder()
				.checkStatus(CheckStatus.WARNING)
				.addMessage(message)
				.build();
	}
	
	protected CheckResult buildError(String message) {
		return CheckResult.builder()
				.checkStatus(CheckStatus.ERROR)
				.addMessage(message)
				.build();
	}
	
	private CheckResult handleException(Exception exception) {
		if (exception instanceof HttpRequestException) {
			return buildError("HTTP error: " + ((HttpRequestException) exception).getStatusCode());
		}
		return buildError(Throwables.getStackTraceAsString(exception));
	}
	
}
