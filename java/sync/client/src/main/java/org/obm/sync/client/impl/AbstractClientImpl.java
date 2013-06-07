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
package org.obm.sync.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.transform.TransformerException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.obm.locator.LocatorClientException;
import org.obm.push.utils.DOMUtils;
import org.obm.sync.XTrustProvider;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.client.exception.ObmSyncClientException;
import org.obm.sync.client.exception.SIDNotFoundException;
import org.obm.sync.locators.Locator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public abstract class AbstractClientImpl {

	static {
		XTrustProvider.install();
	}
	
	private static final int MAX_CONNECTIONS = 8;
	protected final Logger logger = LoggerFactory.getLogger(getClass());
	private final Logger obmSyncLogger;
	protected final SyncClientException exceptionFactory;
	protected HttpClient hc;
	
	protected abstract Locator getLocator();

	protected static HttpClient createHttpClient() {
		PoolingClientConnectionManager mtManager = new PoolingClientConnectionManager();
		mtManager.setMaxTotal(MAX_CONNECTIONS);
		mtManager.setDefaultMaxPerRoute(MAX_CONNECTIONS);
		return new DefaultHttpClient(mtManager);
	}

	public AbstractClientImpl(SyncClientException exceptionFactory, Logger obmSyncLogger) {
		super();
		this.exceptionFactory = exceptionFactory;
		this.obmSyncLogger = obmSyncLogger;
		this.hc = createHttpClient();
	}

	protected Document execute(AccessToken token, String action, Multimap<String, String> parameters) {
		HttpPost request = null;
		try {
			request = getPostMethod(token, action);
			logRequest(action, parameters);
			InputStream is = executePostAndGetResultStream(request, parameters);
			if (is != null) {
				Document document = DOMUtils.parse(is);
				logResponse(document);
				return document;
			} else {
				throw new ObmSyncClientException("An error occurs: cannot get the request result stream");
			}
		} catch (LocatorClientException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (SAXException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (FactoryConfigurationError e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} finally {
			releaseConnection(request);
		}
	}

	private void logRequest(String action, Multimap<String, String> parameters) {
		obmSyncLogger.debug("action {}, request {}", action, parameters);
	}

	private void logResponse(Document document) {
		if (obmSyncLogger.isDebugEnabled()) {
			try {
				obmSyncLogger.debug("response {}", DOMUtils.prettySerialize(document));
			} catch (TransformerException e) {
				obmSyncLogger.debug("unparsable response");
			}
		}
	}

	protected void setToken(Multimap<String, String> parameters, AccessToken token) throws SIDNotFoundException {
		if (token != null) {
			if (token.getSessionId() != null) {
				parameters.put("sid", token.getSessionId());
			} else {
				throw new SIDNotFoundException(token);
			}
		}
	}

	protected Multimap<String, String> initParams(AccessToken at) {
		Multimap<String, String> m = ArrayListMultimap.create();
		try {
			setToken(m, at);
		} catch (SIDNotFoundException e) {
			logger.warn(e.getMessage(), e);
		}
		return m;
	}

	private InputStream executePostAndGetResultStream(HttpPost request, Multimap<String, String> parameters) throws IOException {
		InputStream is = null;
		setPostMethodParameters(request, parameters);
		HttpResponse response = hc.execute(request);
		int httpResultStatus = response.getStatusLine().getStatusCode();
		if (isHttpStatusOK(httpResultStatus)) {
			is = response.getEntity().getContent();
		} else {
			logger.error("method failed:\n" + response.getStatusLine() + "\n"
					+ response.getEntity().getContent());
		}
		return is;
	}

	private boolean isHttpStatusOK(int httpResultStatus) {
		return httpResultStatus == HttpStatus.SC_OK;
	}

	private void setPostMethodParameters(HttpPost request, Multimap<String, String> parameters) throws UnsupportedEncodingException {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		for (Entry<String, String> entry: parameters.entries()) {
			if (entry.getKey() != null && entry.getValue() != null) {
				nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
		}
		request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	}

	protected void executeVoid(AccessToken at, String action, Multimap<String, String> parameters) {
		HttpPost request = null; 
		try {
			request = getPostMethod(at, action);
			executePostAndGetResultStream(request, parameters);
		} catch (LocatorClientException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ObmSyncClientException(e.getMessage(), e);
		} finally {
			releaseConnection(request);
		}
	}

	private String getBackendUrl(String loginAtDomain) throws LocatorClientException {
		Locator locator = getLocator();
		return locator.backendUrl(loginAtDomain);
	}

	private HttpPost getPostMethod(AccessToken at, String action) throws LocatorClientException {
		String backendUrl = getBackendUrl(at.getUserWithDomain());
		HttpPost request = new HttpPost(backendUrl + action);
		request.setHeaders(new Header[] { new BasicHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8") });
		return request;
	}

	private void releaseConnection(HttpPost request) {
		if (request != null) {
			request.releaseConnection();
		}
	}

}
